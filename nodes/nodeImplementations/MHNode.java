package projects.chandraToueg.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;

import projects.chandraToueg.nodes.messages.MHDecisionMessage;
import projects.chandraToueg.nodes.messages.MHEstimateMessage;
import projects.chandraToueg.nodes.timers.MHDecisionReceivalTimer;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;

public class MHNode extends Node {
	public int timestamp = 0;
	public boolean hasSentEstimate = false;
	public int state = State.Fresh;
	
	private MSSNode getCurrentMSS() {
		for (Edge e : outgoingConnections) {
			if (e.endNode instanceof MSSNode) {
				return (MSSNode) e.endNode;
			}
		}
		return null;
	}
	
	public class State {
		public static final int Fresh = 0;
		public static final int WaitingForDecision = 1;
		public static final int Decided = 2;
	}
	
	public void refresh() {
		hasSentEstimate = false;
		state = State.Fresh;
	}
			
	@Override
	public void handleMessages(Inbox inbox) {
		int roundNumber = MSSNode.roundNumber;
		
		for (Message m : inbox) {
			if (m instanceof MHDecisionMessage) {
				MHDecisionMessage dm = (MHDecisionMessage) m;
				if (dm.round < roundNumber) continue;
	
				state = State.Decided;
			}
		}
	}

	@Override
	public void preStep() {
		if (!hasSentEstimate) {
			MSSNode currentMSS = getCurrentMSS();
			if (currentMSS != null) {
				sendEstimate(currentMSS);
				hasSentEstimate = true;
			}
		}
	}
	
	public void sendEstimate(MSSNode mss) {
		int roundNumber = MSSNode.roundNumber;
		send(new MHEstimateMessage(this, roundNumber), mss);
		timestamp++;
		new MHDecisionReceivalTimer(roundNumber).startRelative(50, this);
	}
	
	public void onDecisionMessageWaitTimeout(int round) {
		int roundNumber = MSSNode.roundNumber;
		if (round == roundNumber && state == State.WaitingForDecision) {
			refresh();
		}
	}

	@Override
	public void init() {
	}

	@Override
	public void neighborhoodChange() {
	}

	@Override
	public void postStep() {
		
	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {
	}
	
	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		super.drawAsDisk(g, pt, highlight, 10);
	}
	
	@Override
	public Color getColor() {
		switch (state) {
		case State.Decided: return Color.magenta;
		default: return Color.green; 
		}
	}
}
