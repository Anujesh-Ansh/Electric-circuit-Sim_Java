package componentdescriptors;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import circuitlogic.solver.devices.ACSource;
import simulatorgui.rendering.Animable;
import simulatorgui.rendering.DeviceUI;
import simulatorgui.rendering.LogarithmicSlider;
import simulatorgui.rendering.RenderingCanvas;
import utilities.NumericUtilities;

public class ACSourceDescriptor extends DeviceUI {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8355178463434070064L;
	private double amplitude = 100d;
	private double frequency = 1;
	private double phase = 0;
	private double current = 0;

	public ACSourceDescriptor(RenderingCanvas canvas, int ID) throws IOException {
		this(canvas, new Point(0, 0));
	}

	public ACSourceDescriptor(RenderingCanvas canvas, Point position) throws IOException {
		super(canvas, "components/acsource.png", 100, 100, new Point[] { new Point(45, 0), new Point(-45, 0) },
				"ACSource");
		addAnimator(new Animable() {
	
			@Override
			public void animate(Graphics g) {
				Graphics2D gx = (Graphics2D) g.create();
				gx.translate(getWidth() / 2, getHeight() / 2);
				gx.setColor(Color.white);
				Animable.writeCenteredText(NumericUtilities.getPrefixed(amplitude, 4) + "V", Animable.globalFont, gx,
						new Point(0, 40));
				Animable.drawArrow(gx, 0, -33, current, 0.0);
				gx.dispose();
			}
		});

		this.setLocation(position);

	}

	@Override
	public void displayProperties(JComponent parent) {
		parent.removeAll();
		JLabel title = new JLabel("AC Source");
		title.setForeground(Color.green);
		title.setAlignmentX(CENTER_ALIGNMENT);
		parent.add(title);
		{
			JLabel ampTag = new JLabel("Amplitude = " + NumericUtilities.getPrefixed(amplitude, 4) + "V");
			ampTag.setAlignmentX(CENTER_ALIGNMENT);
			parent.add(ampTag);
			LogarithmicSlider ampVal = new LogarithmicSlider(-9, 6, 4, "V");
			ampVal.setLogValue(amplitude);
			ampVal.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					ACSourceDescriptor.this.amplitude = NumericUtilities.getRounded(ampVal.getLogValue(), 4);
					canvas.Render();
					ampTag.setText("Amplitude = " + NumericUtilities.getPrefixed(amplitude, 4) + "V");
				}
			});
			parent.add(ampVal);
		}
		{
			JLabel hzTag = new JLabel("Frequency = " + NumericUtilities.getPrefixed(frequency, 4) + "Hz");
			hzTag.setAlignmentX(CENTER_ALIGNMENT);
			parent.add(hzTag);
			LogarithmicSlider hzVal = new LogarithmicSlider(-9, 6, 4, "Hz");
			hzVal.setLogValue(frequency);
			hzVal.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					ACSourceDescriptor.this.frequency = NumericUtilities.getRounded(hzVal.getLogValue(), 4);
					canvas.Render();
					hzTag.setText("Frequency = " + NumericUtilities.getPrefixed(frequency, 4) + "Hz");
				}
			});
			parent.add(hzVal);
		}
		{
			JLabel phTag = new JLabel("Phase = " + (int) phase + "°");
			phTag.setAlignmentX(CENTER_ALIGNMENT);
			parent.add(phTag);
			JSlider phVal = new JSlider(0, 360);
			phVal.setValue((int) phase);
			phVal.setPaintTicks(true);
			phVal.setMajorTickSpacing(45);
			phVal.setMinorTickSpacing(15);
			phVal.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					ACSourceDescriptor.this.phase = phVal.getValue();
					canvas.Render();
					phTag.setText("Phase = " + (int) phase + "°");
				}
			});
			parent.add(phVal);
		}
		parent.revalidate();
		parent.repaint();
	}

	@Override
	public void writeState(HashMap<String, Object> data, double t) {
		current = (double) data.get(ACSource.CURRENT);
		this.t = t;
	}

	@Override
	public HashMap<String, Object> readProperties() {
		HashMap<String, Object> data = new HashMap<>();
		data.put(ACSource.AMPLITUDE, amplitude);
		data.put(ACSource.FREQUENCY, frequency);
		data.put(ACSource.PHASE, phase);
		return data;
	}

	@Override
	public void writeProperties(HashMap<String, Object> data) {
		amplitude = (double) data.get(ACSource.AMPLITUDE);
		frequency = (double) data.get(ACSource.FREQUENCY);
		phase = (double) data.get(ACSource.PHASE);
	}
}
