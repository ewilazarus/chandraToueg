package projects.chandraToueg.nodes.messages;

import java.util.UUID;

import projects.chandraToueg.nodes.nodeImplementations.MSSNode;
import sinalgo.nodes.messages.Message;

public class MHDecisionMessage extends Message {
	public MSSNode origin;
	public UUID decidedValue;
	public int round;
	public int maxTimestamp;
	
	public MHDecisionMessage(MSSNode origin, int round, UUID decidedValue, int maxTimestamp) {
		this.origin = origin;
		this.round = round;
		this.decidedValue = decidedValue;
		this.maxTimestamp = maxTimestamp;
	}

	@Override
	public Message clone() {
		return new DecisionMessage(origin, round, decidedValue, maxTimestamp);
	}
}
