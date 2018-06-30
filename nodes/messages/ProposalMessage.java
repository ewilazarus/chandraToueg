package projects.chandraToueg.nodes.messages;

import java.util.UUID;

import projects.chandraToueg.nodes.nodeImplementations.MSSNode;
import sinalgo.nodes.messages.Message;

public class ProposalMessage extends Message {
	public MSSNode origin;
	public UUID proposedValue;
	public int round;
	public int maxTimestamp;
	
	public ProposalMessage(MSSNode origin, int round, UUID proposedValue, int maxTimestamp) {
		this.origin = origin;
		this.proposedValue = proposedValue;
		this.round = round;
		this.maxTimestamp = maxTimestamp;
	}

	@Override
	public Message clone() {
		return new ProposalMessage(origin, round, proposedValue, maxTimestamp);
	}
}
