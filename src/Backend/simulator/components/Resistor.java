package Backend.simulator.components;

import Backend.simulator.Circuit;
import Backend.simulator.Component;
import Backend.simulator.Circuit.Node;
import Backend.simulator.Circuit.Segment;

public class Resistor extends Component {
	public static final String CURRENT = "current";
	public static final String RESISTANCE = "resistance";

	public Resistor(Circuit c) {
		super(c);
		segments = new Circuit.Segment[1];
		segments[0] = c.AddSegment();
		properties.put(RESISTANCE, 1.0);
		updateProperties();
		updateState(t, 0);
	}

	@Override
	public void updateState(double t, double dt) {
		super.updateState(t, dt);
		segments[0].resistance = (double) properties.get(RESISTANCE);
//		var current = segments[0].current;
//		if (Node.compareDepth(segments[0].getNode(0), segments[0].getNode(1)) < 0)
//			current *= -1;
		var current = segments[0].current;
		if (Node.compareDepth(segments[0].getNode(0), segments[0].getNode(1)) < 0)
			current *= -1;
		states.put(CURRENT, current);
	}

	@Override
	public Circuit.Node getExternalNode(int index) throws Exception {
		if (index < 0 || index > 1) {
			throw new Exception("Node index must be in [0-1]");
		}
		return segments[0].getNode(index);
	}

	@Override
	public void updateProperties() {

	}
}
