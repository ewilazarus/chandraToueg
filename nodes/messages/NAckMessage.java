package projects.chandraToueg.nodes.messages;

import projects.chandraToueg.nodes.nodeImplementations.MSSNode;
import sinalgo.nodes.messages.Message;

public class NAckMessage extends Message {
	public MSSNode origin;
	public int round;
		
	public NAckMessage(MSSNode origin, int round) {
		this.origin = origin;
		this.round = round;
	}

	@Override
	public Message clone() {
		return new NAckMessage(origin, round);
	}
}
