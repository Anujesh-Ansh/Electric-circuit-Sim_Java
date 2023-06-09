package circuitlogic.solver;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.naming.directory.AttributeInUseException;
import javax.swing.JOptionPane;

import simulatorgui.Driver;
import utilities.NumericUtilities;

public class SimulatorAPI implements Runnable {

	private final HashMap<Integer, Component> identifiers;
	private boolean initialized = false;
	private double timeElapsed = 0;
	public final SimulationState state;
	private Circuit c;
	private double timeScale;
	private static final double MIN_SIM_RATE = 10;
	private static final double MAX_SIM_RATE = 1000;

	public class SimulationState {
		private SimulationState() {
			stateData = new HashMap<>();
			timeStamp = 0;
		}

		public double getT() {
			return timeStamp;
		}

		private double timeStamp;
		public HashMap<Integer, HashMap<String, Object>> stateData;
	}

	public SimulatorAPI() {
		this.timeScale = 1;
		this.c = new Circuit();
		this.state = new SimulationState();
		identifiers = new HashMap<Integer, Component>();
		this.timeScale = 1;
	}

	private HashMap<Integer, HashMap<String, Object>> collectStateData() {
		HashMap<Integer, HashMap<String, Object>> data = new HashMap<>();
		for (var k : identifiers.keySet()) {
			data.put(k, identifiers.get(k).getAllStates());
		}
		return data;
	}

	private long simulateStep(double dt, int substeps) throws Exception {
		long t1 = System.nanoTime();
		int t = substeps;
		double rDt = dt / substeps;
		while (t-- > 0) {
			if (!initialized) {
				c.initialiseCircuit();
				initialized = true;
				for (var data : identifiers.values()) {
					data.updateState(0, dt);
				}
			}
			c.generateEmfMatrix();
			c.generateResistanceMatrix(rDt);
			c.generateInductanceMatrix();

			c.solveCurrent(rDt);
			c.updateSegments(rDt);
			timeElapsed += rDt;
			for (var data : identifiers.values()) {
				data.updateState(timeElapsed, rDt);
			}
		}
		state.stateData = collectStateData();
		state.timeStamp = timeElapsed;
		long t2 = System.nanoTime();
		return t2 - t1;
	}

	public void addComponent(int identifier, String componentName) throws Exception {
		if (identifiers.containsKey(identifier)) {
			throw new AttributeInUseException("Key " + identifier + " already exists");
		}
		try {
			@SuppressWarnings("unchecked")
			Class<Component> x = (Class<Component>) Class.forName("circuitlogic.solver.devices." + componentName);
			try {
				var comp = c.addComponent(x.getDeclaredConstructor(new Class[] { Circuit.class }).newInstance(c));
				identifiers.put(identifier, comp);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				e.printStackTrace();
			}

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public enum mode {
		RUNNING, PAUSED, TERMINATED
	}

	private mode CurrMode = mode.TERMINATED;

	public mode getMode() {
		return CurrMode;
	}

	public void play() {
		CurrMode = mode.RUNNING;
	}

	public void pause() {
		CurrMode = mode.PAUSED;
	}

	public void stop() {
		CurrMode = mode.TERMINATED;
	}

	public void setTimeScale(double t) {
		this.timeScale = t;
	}

	/** Connect set of nodes. Format is [{iden1, n1}...] */
	public void connect(ArrayList<int[]> data) {
		try {
			var temp = new ArrayList<Circuit.Node>();
			for (int i = 0; i < data.size(); i++) {
				if (data.get(i).length != 2)
					throw new IllegalArgumentException("Data array must contain as format {iden1, n1}");
				var node = identifiers.get(data.get(i)[0]).getPin(data.get(i)[1]);
				temp.add(node);
			}
			c.mergeNodes(temp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void run() {
		int simulationRate = 100;
		
		var startT = System.nanoTime();
		long accumulator = 0;

		long elapsed = 0;
		while (CurrMode != mode.TERMINATED) {
			var prevT = startT;
			startT = System.nanoTime();
			timeScale = Driver.getDriver().speed;
			accumulator += timeScale * (startT - prevT);
			while (CurrMode == mode.PAUSED)
				; // wait while paused

			if (accumulator < 0) {
				continue;
			}
			try {
				elapsed = simulateStep(timeScale / simulationRate, 1);
			} catch (Exception e1) {
				System.err.println("SHORT");
				JOptionPane.showMessageDialog(null, e1.getMessage(), "Simulation failure", JOptionPane.ERROR_MESSAGE);
				CurrMode = mode.TERMINATED;
				return;
			}
			accumulator -= timeScale / simulationRate * 1000000000;
//			System.out.println(accumulator);
			if(accumulator > 0) {
				simulationRate/=1.1;
			}else {
				simulationRate*=1.1;
			}
			simulationRate = (int)NumericUtilities.clamp(simulationRate, MIN_SIM_RATE, MAX_SIM_RATE);
//			if (elapsed < stepMS * 1000000000) {
//				try {
//					Thread.currentThread();
//					Thread.sleep(stepMS - (elapsed) / 1000000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
		}
	}

	/** Connect two nodes. Format is iden1, n1, iden2, n2. */
	public void connect(int iden1, int n1, int iden2, int n2) {
		try {
			var temp = new ArrayList<Circuit.Node>();
			var node1 = identifiers.get(iden1).getPin(n1);
			var node2 = identifiers.get(iden2).getPin(n2);
			temp.add(node1);
			temp.add(node2);
			c.mergeNodes(temp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setProperty(int componentID, String property, Object value) {
		var comp = identifiers.get(componentID);
		if (comp == null)
			throw new RuntimeException("Component ID not found: " + componentID);
		else
			comp.setProperty(property, value);
	}
}