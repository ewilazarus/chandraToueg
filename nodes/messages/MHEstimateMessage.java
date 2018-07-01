package projects.chandraToueg.nodes.messages;

import java.util.UUID;

import projects.chandraToueg.nodes.nodeImplementations.MHNode;
import sinalgo.nodes.messages.Message;

public class MHEstimateMessage extends Message {
	public MHNode origin;
	public int timestamp;
	public int round;
	public UUID estimatedValue;
	
	private MHEstimateMessage(MHNode origin, int timestamp, int round, UUID estimatedValue) {
		this.origin = origin;
		this.timestamp = timestamp;
		this.round = round;
		this.estimatedValue = estimatedValue;
	}
	
	public MHEstimateMessage(MHNode origin, int round) {
		this(origin, origin.timestamp, round, UUID.randomUUID());
	}

	@Override
	public Message clone() {
		return new MHEstimateMessage(origin, timestamp, round, estimatedValue);
	}
}
