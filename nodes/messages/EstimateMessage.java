package projects.chandraToueg.nodes.messages;

import java.util.UUID;

import projects.chandraToueg.nodes.nodeImplementations.MSSNode;
import sinalgo.nodes.messages.Message;

public class EstimateMessage extends Message{
	public MSSNode origin;
	public int round;
	public int timestamp;
	public UUID estimatedValue;
	
	private EstimateMessage(MSSNode origin, int round, int timestamp, UUID estimatedValue) {
		this.origin = origin;
		this.round = round;
		this.timestamp = timestamp;
		this.estimatedValue = estimatedValue;
	}
	
	public EstimateMessage(MSSNode origin, int round) {
		this(origin, round, origin.timestamp, UUID.randomUUID());
	}

	@Override
	public Message clone() {
		return new EstimateMessage(origin, round, timestamp, estimatedValue);
	}
}
