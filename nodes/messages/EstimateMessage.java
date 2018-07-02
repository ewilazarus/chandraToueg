package projects.chandraToueg.nodes.messages;

import java.util.UUID;
import java.util.Vector;

import projects.chandraToueg.nodes.nodeImplementations.MSSNode;
import sinalgo.nodes.messages.Message;

public class EstimateMessage extends Message{
	public MSSNode origin;
	public int round;
	public int timestamp;
	public Vector<UUID> estimatedValues;
	
	public EstimateMessage(MSSNode origin, int round, int timestamp, Vector<UUID> estimatedValues) {
		this.origin = origin;
		this.round = round;
		this.timestamp = timestamp;
		this.estimatedValues = estimatedValues;
	}

	@Override
	public Message clone() {
		return new EstimateMessage(origin, round, timestamp, estimatedValues);
	}
}
