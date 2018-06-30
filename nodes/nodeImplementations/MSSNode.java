package projects.chandraToueg.nodes.nodeImplementations;

import java.awt.Color;
import java.util.UUID;
import java.util.Vector;

import projects.chandraToueg.nodes.messages.AckMessage;
import projects.chandraToueg.nodes.messages.DecisionMessage;
import projects.chandraToueg.nodes.messages.EstimateMessage;
import projects.chandraToueg.nodes.messages.NAckMessage;
import projects.chandraToueg.nodes.messages.ProposalMessage;
import projects.chandraToueg.nodes.timers.AckOrNAckReceivalTimer;
import projects.chandraToueg.nodes.timers.DecisionReceivalTimer;
import projects.chandraToueg.nodes.timers.EstimateReceivalTimer;
import projects.chandraToueg.nodes.timers.ProposalReceivalTimer;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.nodes.Node;
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
	
	private class CommonState {
		public static final int Undecided = 0;
		public static final int WaitingForProposal = 1;
		public static final int WaitingForDecision = 2;
		public static final int Decided = 3;
	}
	
	private class CoordinatorState {
		public static final int Fresh = 0;
		public static final int WaitingForEstimates = 1;
		public static final int WaitingForAcksOrNAcks = 2;
		public static final int SentDecision = 3;
	}
	
	// global	
	public static int roundNumber = 0;
	
	// common-nodes
	public int timestamp = 0;
	public int commonState = CommonState.Undecided;
	public UUID propsedValue  = null;
	public int proposedValueTimestamp = -1;
	public UUID decidedValue = null;
	public int decidedValueTimestamp = -1;
	boolean hasSentEstimate = false;
	
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
		commonState = CommonState.Undecided;
		propsedValue = null;
		proposedValueTimestamp = -1;
		decidedValue = null;
		decidedValueTimestamp = -1;
		hasSentEstimate = false;
		
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
			if (commonState == CommonState.WaitingForProposal && m instanceof ProposalMessage) {
				Tools.appendToOutput(Integer.toString(ID) + ": " + "Received proposal" + "\n");
				ProposalMessage pm = (ProposalMessage) m;
				if (pm.round < roundNumber) continue;
				
				propsedValue = pm.proposedValue;
				proposedValueTimestamp = pm.maxTimestamp;
				
				sendAckOrNAckMessage();
			}
			else if (commonState == CommonState.WaitingForDecision && m instanceof DecisionMessage) {
				Tools.appendToOutput(Integer.toString(ID) + ": " + "Received decision" + "\n");
				DecisionMessage dm = (DecisionMessage) m;
				if (dm.round < roundNumber) continue;
				
				decidedValue = dm.decidedValue;
				decidedValueTimestamp = dm.maxTimestamp;
				commonState = CommonState.Decided;
			}
			
			if (this == coordinator) {
				if (coordinatorState == CoordinatorState.WaitingForEstimates && m instanceof EstimateMessage) {
					Tools.appendToOutput(Integer.toString(ID) + ": " + "Received estimate" + "\n");
					estimateMessages.add((EstimateMessage) m);
				}
				else if (coordinatorState == CoordinatorState.WaitingForAcksOrNAcks) {
					if (m instanceof AckMessage) {
						Tools.appendToOutput(Integer.toString(ID) + ": " + "Received ack" + "\n");
						AckMessage am = (AckMessage) m;
						if (ackOrNAckmsgCount >= getMajorityMSSNodeCount() || am.round < roundNumber) continue;
						ackOrNAckmsgCount++;
						
						ackMessages.add(am);						
					} else if (m instanceof NAckMessage) {
						Tools.appendToOutput(Integer.toString(ID) + ": " + "Recieved nack" + "\n");
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
				// TODO: pegar mensagem com maior TS
				EstimateMessage em = estimateMessages.firstElement();
					
				chosenEstimatedValue = em.estimatedValue;
				chosenEstimatedValueTimestamp = em.timestamp;
					
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
		
		if (commonState == CommonState.Undecided && !hasSentEstimate) {
			sendEstimateMessage();
			
		}
		
		if (this == coordinator && coordinatorState == CoordinatorState.Fresh) {
			coordinatorState = CoordinatorState.WaitingForEstimates;
			new EstimateReceivalTimer(roundNumber).startRelative(40, this);
		}
	}

	@Override
	public void init() {
		reachableMSSNodes.add(this);
		setDefaultDrawingSizeInPixels(50);
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
		send(new EstimateMessage(this, roundNumber), coordinator);
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
		// TODO: implementar um fake failure-detector
		send(new AckMessage(this, roundNumber), coordinator);
				
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
		coordinatorState = CoordinatorState.SentDecision;
	}
	
	@Override
	public Color getColor() {
		return this == coordinator ? Color.blue : Color.black;
	}
}
