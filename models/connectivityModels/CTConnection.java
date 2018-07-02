package projects.chandraToueg.models.connectivityModels;

import projects.chandraToueg.nodes.nodeImplementations.MSSNode;
import sinalgo.models.ConnectivityModelHelper;
import sinalgo.nodes.Node;

public class CTConnection extends ConnectivityModelHelper {
	
	@Override
	protected boolean isConnected(Node from, Node to) {
		// MSS
		if (from instanceof MSSNode) {
			if (to instanceof MSSNode) {
				return true;
			}
			else {
				return from.getPosition().distanceTo(to.getPosition()) <= MSSNode.messageReach &&
						(to.outgoingConnections.size() == 0 ||
						 to.outgoingConnections.size() == 1 && to.outgoingConnections.contains(to, from));
			}
		}
		
		// MH
		else {
			if (to instanceof MSSNode) {
				return from.getPosition().distanceTo(to.getPosition()) <= MSSNode.messageReach;
			}
		}
		
		return false;
	}
}
