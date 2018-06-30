package projects.chandraToueg.nodes.edges;

import java.awt.Color;

import projects.chandraToueg.nodes.nodeImplementations.MSSNode;

public class Edge extends sinalgo.nodes.edges.Edge {
	@Override
	public void initializeEdge() {
		if (isMSS2MSSConnection()) {
			defaultColor = Color.black;
		} else {
			defaultColor = Color.green;
		}
	}
	
	private boolean isMSS2MSSConnection() {
		return startNode instanceof MSSNode && endNode instanceof MSSNode;		
	}	
}
