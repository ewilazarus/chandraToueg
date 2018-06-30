package projects.chandraToueg.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;

import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;

public class MHNode extends Node {
	
			
	@Override
	public void handleMessages(Inbox inbox) {
	}

	@Override
	public void preStep() {
	}

	@Override
	public void init() {
	}

	@Override
	public void neighborhoodChange() {
	}

	@Override
	public void postStep() {		
	}

	@Override
	public void checkRequirements() throws WrongConfigurationException {
	}
	
	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		super.drawAsDisk(g, pt, highlight, 10);
	}
	
	@Override
	public Color getColor() {
		return Color.green;
	}
}
