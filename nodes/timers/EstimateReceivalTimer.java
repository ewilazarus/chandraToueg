package projects.chandraToueg.nodes.timers;

import projects.chandraToueg.nodes.nodeImplementations.MSSNode;
import sinalgo.nodes.timers.Timer;

public class EstimateReceivalTimer extends Timer {
	public int round;
	
	public EstimateReceivalTimer(int round) {
		this.round = round;
	}
	
	@Override
	public void fire() {
		((MSSNode) node).onEstimateMessagesWaitTimeout(round);
	}
}
