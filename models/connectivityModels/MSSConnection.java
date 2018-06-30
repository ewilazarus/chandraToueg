package projects.chandraToueg.models.connectivityModels;

import projects.chandraToueg.nodes.nodeImplementations.MSSNode;
import sinalgo.models.ConnectivityModelHelper;
import sinalgo.nodes.Node;

public class MSSConnection extends ConnectivityModelHelper {
	@Override
	protected boolean isConnected(Node from, Node to) {
		boolean isOriginMSS = from instanceof MSSNode;
		boolean isDestinationMSS = to instanceof MSSNode;
		boolean isDestinationDesconnected = to.outgoingConnections.size() == 0;
		boolean isDestinationReachable = from.getPosition().distanceTo(to.getPosition()) <= MSSNode.messageReach;
		
		return isOriginMSS && (isDestinationMSS || (isDestinationDesconnected && isDestinationReachable));
	}
}
