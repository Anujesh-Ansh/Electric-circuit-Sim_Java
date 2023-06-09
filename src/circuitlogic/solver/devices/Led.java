package circuitlogic.solver.devices;

import circuitlogic.solver.Circuit;

public class Led extends Diode {
	public static final String RATED_VOLTAGE = "voltage";
	public static final String RATED_POWER = "power";
	public static final String WAVELENGTH_NM = "wavelength";
	public static final String INTENSITY = "intensity";

	public Led(Circuit c) {
		super(c);
		properties.put(WAVELENGTH_NM, 660.0);
		properties.put(RATED_VOLTAGE, 3.0);
		properties.put(RATED_POWER, 0.030);
		states.put(CURRENT, 0.0);
		states.put(INTENSITY, 0.0);
	}

	@Override
	public void updateState(double t, double dt) {
		double res = Math.pow((double) properties.get(RATED_VOLTAGE), 2) / (double) properties.get(RATED_POWER);
		super.setProperty(RESISTANCE, res);
		super.updateState(t, dt);
		double current = (double) super.getState(CURRENT);
		double power = Math.pow((double) super.getState(CURRENT), 2) * (double) super.getProperty(RESISTANCE);
		var intensity = power / (double) properties.get(RATED_POWER);
		states.put(CURRENT, current);
		states.put(INTENSITY, intensity);
	}

	@Override
	public Circuit.Node getPin(int index) {
		return super.getPin(index);
	}
}
