package componentdescriptors;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import circuitlogic.solver.devices.Bulb;
import circuitlogic.solver.devices.Led;
import simulatorgui.rendering.Animable;
import simulatorgui.rendering.DeviceUI;
import simulatorgui.rendering.LogarithmicSlider;
import simulatorgui.rendering.RenderingCanvas;
import utilities.NumericUtilities;
import utilities.ResourceManager;

public class LedDescriptor extends DeviceUI {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6501414687864504411L;
	private Color color = new Color(0, 0, 255);
	private double intensity = 0;
	private double current = 0;
	private double ratedVoltage = 1;
	private double ratedWattage = 1;

	public LedDescriptor(RenderingCanvas canvas) throws IOException {
		this(canvas, new Point(0, 0));
	}

	public LedDescriptor(RenderingCanvas canvas, Point position) throws IOException {
		super(canvas, "components/led.png", 100, 100, new Point[] { new Point(45, 0), new Point(-45, 0), }, "Led");
		addAnimator(new Animable() {
			private BufferedImage glow = ResourceManager.loadImage("glow.png", 0).get(0);

			private void drawGlow(Graphics2D g, int w, int h) {
				Graphics2D gx = (Graphics2D) g.create();
				gx.translate(-w / 2, -h / 2);
				gx.drawImage(
						ResourceManager.applyAldebo(glow, color, intensity).getScaledInstance(w, h, Image.SCALE_SMOOTH),
						0, 0, null);
				gx.dispose();
			}

			@Override
			public void animate(Graphics g) {
				Graphics2D gx = (Graphics2D) g.create();
				gx.translate(getWidth() / 2, getHeight() / 2);
				drawGlow(gx, 100, 100);
				gx.setColor(Color.white);
				Animable.writeCenteredText(
						NumericUtilities.getPrefixed(ratedWattage, 3) + "W @"
								+ NumericUtilities.getPrefixed(ratedVoltage, 3) + "V",
						Animable.globalFont, gx, new Point(0, 40));
				Animable.drawArrow(gx, 0, -33, current, 0.0);
				gx.dispose();
			}
		});
		this.setLocation(position);
	}

	@Override
	public void displayProperties(JComponent parent) {
		parent.removeAll();
		JLabel title = new JLabel("LED");
		title.setForeground(Color.green);
		title.setAlignmentX(CENTER_ALIGNMENT);
		parent.add(title);

		{
			JLabel wattLbl = new JLabel("Power = " + NumericUtilities.getPrefixed(ratedWattage, 4) + "W");
			wattLbl.setAlignmentX(CENTER_ALIGNMENT);
			parent.add(wattLbl);
			LogarithmicSlider wattVal = new LogarithmicSlider(-4, 4, 4, "W");
			wattVal.setLogValue(ratedWattage);
			wattVal.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					LedDescriptor.this.ratedWattage = NumericUtilities.getRounded(wattVal.getLogValue(), 4);
					canvas.Render();
					wattLbl.setText("Power = " + NumericUtilities.getPrefixed(ratedWattage, 4) + "W");
				}
			});
			parent.add(wattVal);
		}
		{
			JLabel voltLbl = new JLabel("Voltage = " + NumericUtilities.getPrefixed(ratedVoltage, 4) + "V");
			voltLbl.setAlignmentX(CENTER_ALIGNMENT);
			parent.add(voltLbl);
			LogarithmicSlider voltVal = new LogarithmicSlider(-4, 4, 4, "V");
			voltVal.setLogValue(ratedVoltage);
			voltVal.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					LedDescriptor.this.ratedVoltage = NumericUtilities.getRounded(voltVal.getLogValue(), 4);
					canvas.Render();
					voltLbl.setText("Voltage = " + NumericUtilities.getPrefixed(ratedVoltage, 4) + "V");
				}
			});
			parent.add(voltVal);
		}
		parent.revalidate();
		parent.repaint();
	}

	@Override
	public void writeState(HashMap<String, Object> data, double t) {
		current = (double) data.get(Bulb.CURRENT);
		intensity = (double) data.get(Bulb.INTENSITY);
		this.t = t;
	}

	@Override
	public HashMap<String, Object> readProperties() {
		HashMap<String, Object> data = new HashMap<>();
		data.put(Led.RATED_VOLTAGE, ratedVoltage);
		data.put(Led.RATED_POWER, ratedWattage);
		return data;
	}

	@Override
	public void writeProperties(HashMap<String, Object> data) {
		ratedVoltage = (double) data.get(Led.RATED_VOLTAGE);
		ratedWattage = (double) data.get(Led.RATED_POWER);
	}
}