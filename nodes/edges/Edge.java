package projects.chandraToueg.nodes.edges;

import java.awt.Color;

import projects.chandraToueg.nodes.nodeImplementations.MSSNode;

public class Edge extends sinalgo.nodes.edges.Edge {
	@Override
	public void initializeEdge() {
		if (isMSS2MSSConnection()) {
			defaultColor = Color.black;
		} else {
			if (startNode instanceof MSSNode) {
				MSSNode mn = (MSSNode) startNode;
				if (mn.commonState == MSSNode.CommonState.Decided)
					defaultColor = Color.magenta;
				else {
					defaultColor = Color.green;
				}
			} else if (endNode instanceof MSSNode) {
				MSSNode mn = (MSSNode) endNode;
				if (mn.commonState == MSSNode.CommonState.Decided)
					defaultColor = Color.magenta;
				else {
					defaultColor = Color.green;
				}
			}
			else {
				defaultColor = Color.green;
			}
		}
	}
	
	private boolean isMSS2MSSConnection() {
		return startNode instanceof MSSNode && endNode instanceof MSSNode;		
	}	
}
