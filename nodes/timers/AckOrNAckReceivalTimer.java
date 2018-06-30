package projects.chandraToueg.nodes.timers;

import projects.chandraToueg.nodes.nodeImplementations.MSSNode;
import sinalgo.nodes.timers.Timer;

public class AckOrNAckReceivalTimer extends Timer {
	public int round;
	
	public AckOrNAckReceivalTimer(int round) {
		this.round = round;
	}
	
	@Override
	public void fire() {
		((MSSNode) node).onAckOrNAckMessageWaitTimeout(round);
	}
}
