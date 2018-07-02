package projects.chandraToueg.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.Vector;

import projects.chandraToueg.nodes.messages.AckMessage;
import projects.chandraToueg.nodes.messages.DecisionMessage;
import projects.chandraToueg.nodes.messages.EstimateMessage;
import projects.chandraToueg.nodes.messages.MHDecisionMessage;
import projects.chandraToueg.nodes.messages.MHEstimateMessage;
import projects.chandraToueg.nodes.messages.NAckMessage;
import projects.chandraToueg.nodes.messages.ProposalMessage;
import projects.chandraToueg.nodes.timers.AckOrNAckReceivalTimer;
import projects.chandraToueg.nodes.timers.DecisionReceivalTimer;
import projects.chandraToueg.nodes.timers.EstimateReceivalTimer;
import projects.chandraToueg.nodes.timers.MHEstimateReceivalTimer;
import projects.chandraToueg.nodes.timers.ProposalReceivalTimer;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

public class MSSNode extends Node {
	private static Vector<MSSNode> reachableMSSNodes = new Vector<>();
	public static int messageReach;
	static {
		try {
			messageReach = Configuration.getIntegerParameter("MSSNode/MessageReach");
		} catch (CorruptConfigurationEntryException e) {
			messageReach = 100;
		}
	}
	
	private static double getMajorityMSSNodeCount() {
		return Math.ceil((reachableMSSNodes.size() + 1) / 2.0);
	}
	
	public class CommonState {
		public static final int WaitingForMHs = -1;
		public static final int Undecided = 0;
		public static final int WaitingForProposal = 1;
		public static final int WaitingForDecision = 2;
		public static final int Decided = 3;
	}
	
	public class CoordinatorState {
		public static final int Fresh = 0;
		public static final int WaitingForEstimates = 1;
		public static final int WaitingForAcksOrNAcks = 2;
		public static final int SentDecision = 3;
	}
	
	// global	
	public static int roundNumber = 0;
	
	// common-nodes
	public int timestamp = 0;
	public int commonState = CommonState.WaitingForMHs;
	public UUID estimatedValue = null;
	public UUID propsedValue  = null;
	public int proposedValueTimestamp = -1;
	public UUID decidedValue = null;
	public int decidedValueTimestamp = -1;
	boolean hasSentEstimate = false;
	boolean hasBeenWaitingForMSs = false;
	public FailureDetector failureDetector = new FailureDetector(this);
	public Vector<MHEstimateMessage> mhEstimateMessages = new Vector<>();
	
	// coordinator-nodes
	public MSSNode coordinator;
	public int coordinatorState = CoordinatorState.Fresh;
	public UUID chosenEstimatedValue = null;
	public int chosenEstimatedValueTimestamp = -1;
	public Vector<EstimateMessage> estimateMessages = new Vector<>();
	public Vector<AckMessage> ackMessages = new Vector<>();
	public Vector<NAckMessage> nAckMessages = new Vector<>();
	public int ackOrNAckmsgCount = 0;
	
	public void refresh(MSSNode newCoordinator) {
		commonState = CommonState.WaitingForMHs;
		propsedValue = null;
		proposedValueTimestamp = -1;
		estimatedValue = null;
		decidedValue = null;
		decidedValueTimestamp = -1;
		hasSentEstimate = false;
		hasBeenWaitingForMSs = false;
		mhEstimateMessages = new Vector<>();
		
		coordinator = newCoordinator;
		coordinatorState = CoordinatorState.Fresh;
		chosenEstimatedValue = null;
		chosenEstimatedValueTimestamp = -1;
		estimateMessages = new Vector<>();
		ackMessages = new Vector<>();
		nAckMessages = new Vector<>();
		ackOrNAckmsgCount = 0;
	}
	
	@Override
	public void handleMessages(Inbox inbox) {
		for (Message m : inbox) {
			Tools.appendToOutput(">>> " + Integer.toString(ID) + " Received: " + m.getClass().getSimpleName() + "\n");
			
			// -------------- MH Estimate ---------------
			if (commonState == CommonState.WaitingForMHs && m instanceof MHEstimateMessage) {
				MHEstimateMessage mhem = (MHEstimateMessage) m;
				if (mhem.round < roundNumber) continue;
								
				mhEstimateMessages.add(mhem);
			}
			// -------------- Proposal ---------------
			else if (commonState == CommonState.WaitingForProposal && m instanceof ProposalMessage) {
				ProposalMessage pm = (ProposalMessage) m;
				if (pm.round < roundNumber) continue;
				
				propsedValue = pm.proposedValue;
				proposedValueTimestamp = pm.maxTimestamp;
				
				sendAckOrNAckMessage();
			}
			// -------------- Decision ---------------
			else if (commonState == CommonState.WaitingForDecision && m instanceof DecisionMessage) {
				DecisionMessage dm = (DecisionMessage) m;
				if (dm.round < roundNumber) continue;
				
				decidedValue = dm.decidedValue;
				decidedValueTimestamp = dm.maxTimestamp;
				commonState = CommonState.Decided;
				
				for (Edge e : outgoingConnections) {
					if (e.endNode instanceof MHNode) {
						send(new MHDecisionMessage(this, roundNumber, decidedValue, decidedValueTimestamp), e.endNode);
					}
				}
			}
			// -------------- MH Estimate when already decided ---------------
			else if (commonState == CommonState.Decided && m instanceof MHEstimateMessage) {
				MHEstimateMessage mhem = (MHEstimateMessage) m;
				if (mhem.round < roundNumber) continue;
				
				send(new MHDecisionMessage(this, roundNumber, decidedValue, decidedValueTimestamp), mhem.origin);
			}
			
			if (this == coordinator) {
				// -------------- Estimate ---------------
				if (coordinatorState == CoordinatorState.WaitingForEstimates && m instanceof EstimateMessage) {
					EstimateMessage em = (EstimateMessage) m;
					estimateMessages.add(em);
				}
				// -------------- (N)Ack ---------------
				else if (coordinatorState == CoordinatorState.WaitingForAcksOrNAcks) {
					if (m instanceof AckMessage) {
						AckMessage am = (AckMessage) m;
						if (ackOrNAckmsgCount >= getMajorityMSSNodeCount() || am.round < roundNumber) continue;
						ackOrNAckmsgCount++;
						
						ackMessages.add(am);						
					} else if (m instanceof NAckMessage) {
						NAckMessage nam = (NAckMessage) m;
						if (ackOrNAckmsgCount >= getMajorityMSSNodeCount() || nam.round < roundNumber) continue;
						ackOrNAckmsgCount++;
						
						nAckMessages.add(nam);
					}
				}
			}
		}
		
		if (this == coordinator) {
			if (coordinatorState == CoordinatorState.WaitingForEstimates && estimateMessages.size() > getMajorityMSSNodeCount()) {
				HashMap<UUID, Integer> dict = new HashMap<UUID, Integer>();
				for (EstimateMessage _m : estimateMessages) {
					for (UUID uuid : _m.estimatedValues) {
						dict.put(uuid, _m.timestamp);
					}
				}
				
				if (dict.size() <= 15) return;
				
				Random random = new Random();
				List<UUID> keys = new ArrayList<UUID>(dict.keySet());
				UUID randomUUID = keys.get(random.nextInt(keys.size()));
				int randomUUIDTimestamp = dict.get(randomUUID);
				
				chosenEstimatedValue = randomUUID;
				chosenEstimatedValueTimestamp = randomUUIDTimestamp;
					
				sendProposalMessage();
			}
			
			else if (coordinatorState == CoordinatorState.WaitingForAcksOrNAcks && ackOrNAckmsgCount >= getMajorityMSSNodeCount()) {
				if (nAckMessages.size() == 0) sendDecision();
				else roundNumber++;
			}
		}
	}

	@Override
	public void preStep() {
		MSSNode candidateCoordinator = chooseCoordinator();
		if (coordinator != candidateCoordinator) {
			refresh(candidateCoordinator);
		}
		
		if (commonState == CommonState.WaitingForMHs && !hasBeenWaitingForMSs) {
			new MHEstimateReceivalTimer(roundNumber).startRelative(20, this);
			hasBeenWaitingForMSs = true;
		} else if (commonState == CommonState.Undecided && !hasSentEstimate) {
			sendEstimateMessage();
		}
		
		if (this == coordinator && coordinatorState == CoordinatorState.Fresh) {
			coordinatorState = CoordinatorState.WaitingForEstimates;
			new EstimateReceivalTimer(roundNumber).startRelative(50, this);
		}
	}
	
	public void onMHEstimatesWaitTimeout(int round) {
		commonState = round == roundNumber 
			? CommonState.Undecided 
			: CommonState.WaitingForMHs;
	}

	@Override
	public void init() {
		reachableMSSNodes.add(this);
	}
	
	@Override
	public void neighborhoodChange() {
		// TODO Auto-generated method stub
	}

	@Override
	public void postStep() {
		// TODO Auto-generated method stub
	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {
		// TODO Auto-generated method stub
	}
	
	public MSSNode chooseCoordinator() {
		int coordinatorID = (roundNumber % reachableMSSNodes.size()) + 1;
		return reachableMSSNodes
				.stream()
				.filter(n -> n.ID == coordinatorID)
				.findFirst()	
				.get();
	}
	
	public void sendEstimateMessage() {
		Vector<UUID> v = new Vector<>();
		for (MHEstimateMessage m : mhEstimateMessages) {
			v.add(m.estimatedValue);
		}
				
		send(new EstimateMessage(this, roundNumber, timestamp, v), coordinator);
		hasSentEstimate = true;
		commonState = CommonState.WaitingForProposal;
		timestamp++;
		new ProposalReceivalTimer(roundNumber).startRelative(30, this);
	}
	
	public void onProposalMessageWaitTimeout(int round) {
		if (round == roundNumber && commonState == CommonState.WaitingForProposal) {
			commonState = CommonState.Undecided;
		}
	}
	
	public void sendAckOrNAckMessage() {
		if (failureDetector.isCoordinatorHealthy()) {
			send(new AckMessage(this, roundNumber), coordinator);	
		} else {
			send(new NAckMessage(this, roundNumber), coordinator);
		}
				
		commonState = CommonState.WaitingForDecision;
		timestamp++;
		new DecisionReceivalTimer(roundNumber).startRelative(30, this);
	}

	public void onDecisionMessageWaitTimeout(int round) {
		if (round == roundNumber && commonState == CommonState.WaitingForDecision) {
			commonState = CommonState.Undecided;
		}
	}
	
	public void onEstimateMessagesWaitTimeout(int round) {
		if (round == roundNumber && coordinatorState == CoordinatorState.WaitingForEstimates)
			roundNumber++;
	}
	
	public void sendProposalMessage() {
		for (MSSNode mssNode : reachableMSSNodes) {
			send(new ProposalMessage(this, roundNumber, chosenEstimatedValue, chosenEstimatedValueTimestamp), mssNode);	
		}
		coordinatorState = CoordinatorState.WaitingForAcksOrNAcks;
		timestamp++;
		new AckOrNAckReceivalTimer(roundNumber).startRelative(40, this);
	}
			
	public void onAckOrNAckMessageWaitTimeout(int round) {
		if (round == roundNumber && coordinatorState == CoordinatorState.WaitingForAcksOrNAcks)
			roundNumber++;
	}
	
	public void sendDecision() {
		for (MSSNode mssNode : reachableMSSNodes) {
			send(new DecisionMessage(this, roundNumber, chosenEstimatedValue, chosenEstimatedValueTimestamp), mssNode);	
		}
		timestamp++;
		commonState = CommonState.Decided;
		coordinatorState = CoordinatorState.SentDecision;
	}
	
	@Override
	public Color getColor() {
		switch (commonState) {
		case CommonState.WaitingForProposal: return Color.blue;
		case CommonState.WaitingForDecision: return Color.orange;
		case CommonState.Decided: return Color.magenta;
		default: return Color.black;
		}
	}
	
	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		Color backupColor = g.getColor();
		drawingSizeInPixels = 30;
		pt.translateToGUIPosition(super.getPosition());
		int x = pt.guiX - (drawingSizeInPixels >> 1);
		int y = pt.guiY - (drawingSizeInPixels >> 1);
		Color color = getColor();
		if (this == coordinator) {
			// a highlighted node is surrounded by a red square
			g.setColor(Color.RED);
			g.fillRect(x-2, y-2, drawingSizeInPixels+4, drawingSizeInPixels+4);
		}
		g.setColor(color);
		g.fillRect(x, y, drawingSizeInPixels, drawingSizeInPixels);
		g.setColor(backupColor);
	}
	
	class FailureDetector {
		MSSNode node;
		
		public FailureDetector(MSSNode node) {
			this.node = node;
		}
		
		// TODO: implementar um fake failure-detector
		public boolean isCoordinatorHealthy() {
			return true;
		}
	}
}
