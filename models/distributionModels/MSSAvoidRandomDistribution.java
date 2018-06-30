package projects.chandraToueg.models.distributionModels;

import java.util.ArrayList;
import java.util.List;

import projects.chandraToueg.nodes.nodeImplementations.MSSNode;
import projects.defaultProject.models.distributionModels.Random;
import sinalgo.nodes.Position;

public class MSSAvoidRandomDistribution extends Random {
	private static List<Position> takenPositions = new ArrayList<Position>();
		
	@Override
	public Position getNextPosition() {
		Position position;
		do {
			position = super.getNextPosition();
		} while (!IsAvailablePosition(position));
				
		takenPositions.add(position);
		return position;
	}
	
	private boolean IsAvailablePosition(Position position) {
		for (Position takenPosition : takenPositions) {
			if (position.distanceTo(takenPosition) < MSSNode.messageReach) {
				return false;
			}
		}
		return true;
	}
}
