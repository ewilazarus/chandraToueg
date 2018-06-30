package projects.chandraToueg.nodes.timers;

import projects.chandraToueg.nodes.nodeImplementations.MSSNode;
import sinalgo.nodes.timers.Timer;

public class DecisionReceivalTimer extends Timer {
	public int round;
	
	public DecisionReceivalTimer(int round) {
		this.round = round;
	}
	
	@Override
	public void fire() {
		((MSSNode) node).onDecisionMessageWaitTimeout(round);
	}
}
