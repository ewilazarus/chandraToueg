package projects.chandraToueg.nodes.messages;

import projects.chandraToueg.nodes.nodeImplementations.MSSNode;
import sinalgo.nodes.messages.Message;

public class AckMessage extends Message {
	public MSSNode origin;
	public int round;
		
	public AckMessage(MSSNode origin, int round) {
		this.origin = origin;
		this.round = round;
	}

	@Override
	public Message clone() {
		return new AckMessage(origin, round);
	}
}
