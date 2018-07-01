package projects.chandraToueg.nodes.timers;

import projects.chandraToueg.nodes.nodeImplementations.MSSNode;
import sinalgo.nodes.timers.Timer;

public class MHEstimateReceivalTimer extends Timer {
	public int round;
	
	public MHEstimateReceivalTimer(int round) {
		this.round = round;
	}
	
	@Override
	public void fire() {
		((MSSNode) node).onMHEstimatesWaitTimeout(round);
	}
}
