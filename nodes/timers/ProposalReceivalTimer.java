package projects.chandraToueg.nodes.timers;

import projects.chandraToueg.nodes.nodeImplementations.MSSNode;
import sinalgo.nodes.timers.Timer;

public class ProposalReceivalTimer extends Timer {
	public int round;
	
	public ProposalReceivalTimer(int round) {
		this.round = round;
	}
	
	@Override
	public void fire() {
		((MSSNode) node).onProposalMessageWaitTimeout(round);
	}
}
