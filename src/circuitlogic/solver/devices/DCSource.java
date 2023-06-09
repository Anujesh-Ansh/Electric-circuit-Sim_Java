package circuitlogic.solver.devices;

import circuitlogic.solver.Circuit;
import circuitlogic.solver.Component;

public class DCSource extends Component {
	public static final String EMF = "emf";
	public static final String CURRENT = "current";

	public DCSource(Circuit c) {
		super(c);
		segments = new Circuit.Segment[1];
		segments[0] = c.addSegment();
		properties.put(EMF, 1.0);
		states.put(CURRENT, 0.0);
	}

	@Override
	public void updateState(double t, double dt) {
		super.updateState(t, dt);
		segments[0].setEMF((double) properties.get(EMF));
		var current = segments[0].getCurrent();
		states.put(CURRENT, current);
	}

	@Override
	public Circuit.Node getPin(int index) throws Exception {
		if (index < 0 || index > 1) {
			throw new Exception("Node index must be in [0-1]");
		}
		return segments[0].getNode(index);
	}
}
