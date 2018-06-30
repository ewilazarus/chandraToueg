package projects.chandraToueg.models.connectivityModels;

import projects.chandraToueg.nodes.nodeImplementations.MHNode;
import projects.chandraToueg.nodes.nodeImplementations.MSSNode;
import sinalgo.models.ConnectivityModelHelper;
import sinalgo.nodes.Node;

public class MHConnection extends ConnectivityModelHelper {
	@Override
	protected boolean isConnected(Node from, Node to) {
		boolean isOriginMH = from instanceof MHNode;
		boolean isOriginDesconnected = from.outgoingConnections.size() == 0;
		boolean isDestinationMSS = to instanceof MSSNode;
		boolean isDestinationReachable = from.getPosition().distanceTo(to.getPosition()) <= MSSNode.messageReach;
				
		return isOriginMH && isOriginDesconnected && isDestinationMSS && isDestinationReachable;
	}
}
