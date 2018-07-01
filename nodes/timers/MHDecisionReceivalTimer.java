package projects.chandraToueg.nodes.timers;

import projects.chandraToueg.nodes.nodeImplementations.MHNode;
import sinalgo.nodes.timers.Timer;

public class MHDecisionReceivalTimer extends Timer {
	public int round;
	
	public MHDecisionReceivalTimer(int round) {
		this.round = round;
	}
	
	@Override
	public void fire() {
		((MHNode) node).onDecisionMessageWaitTimeout(round);
	}
}
