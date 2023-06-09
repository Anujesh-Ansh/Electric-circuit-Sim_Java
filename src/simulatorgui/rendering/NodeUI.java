package simulatorgui.rendering;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.HashSet;

import simulatorgui.rendering.RenderingCanvas.currentMode;

public class NodeUI extends CanvasDrawable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4558460949541414417L;
	private static final int priority = 0;
	public int radius;
	public static final int DEFAULT_RADIUS = 5;
	public DeviceUI parentDevice;
	private final int nodeIndex;
	private Color nodeColor = Color.red;
	public final HashSet<WireUI> incidentWires;

	public void setNodeColor(Color c) {
		nodeColor = c;
	}

	public void setRadius(int r) {
		radius = r;
		getTransformedBounds();
	}

	public NodeUI(RenderingCanvas canvas, int radius, DeviceUI parent, int nodeIndex) {
		this(new Point(), canvas, radius, parent, nodeIndex);
	}

	public NodeUI(RenderingCanvas canvas, int radius) {
		this(new Point(), canvas, radius, null, 0);
	}

	public NodeUI(Point p, RenderingCanvas canvas) {
		this(p, canvas, NodeUI.DEFAULT_RADIUS, null, 0);
	}

	@Override
	public Point getLocationOnScreen() {
		
//	
		return null;
	}

	@Override
	public void setLocation(Point loc) {
		setLocation(loc.x, loc.y);
	}

	@Override
	public void setLocation(int x, int y) {
		
		super.setLocation(x, y);
		getTransformedBounds();
		for(var w : incidentWires) {
			w.getTransformedBounds();
			canvas.objectsMap.store(w);
		}
		canvas.objectsMap.store(this);
	}

	public NodeUI(Point p, RenderingCanvas canvas, int radius, DeviceUI parent, int nodeIndex) {
		super(canvas);
		this.radius = radius;
		this.parentDevice = parent;
		this.nodeIndex = nodeIndex;
		this.incidentWires = new HashSet<>();
		setSize(radius * 2, radius * 2);
		setLocation(p);
		getTransformedBounds();
		canvas.objectsMap.store(this);
	}

	public void includeWire(WireUI w) {
		this.incidentWires.add(w);
	}

	@Override
	Rectangle getTransformedBounds() {
		
		regions.clear();
		AffineTransform at = new AffineTransform();
		var x = getX() - radius;
		var y = getY() - radius;
		at.translate(x, y);
		regions.add(at.createTransformedShape(new Rectangle(getSize())));
		return regions.get(0).getBounds2D().getBounds();
	}

	@Override
	public void update(Graphics g) {
		
		Graphics2D gx = (Graphics2D) g.create();
		getTransformedBounds();
		gx.translate(getX() - radius, getY() - radius);
		gx.setColor(nodeColor);
		gx.fillOval(0, 0, (int) Math.round(2 * radius), (int) Math.round(2 * radius));
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		
//		System.out.println("Node clicked");
		if (canvas.mode != currentMode.MAKE_WIRE) {
			canvas.mode = currentMode.MAKE_WIRE;

			WireUI w = new WireUI(canvas);
			canvas.currSelected = w;
			w.addNode(this);
//			var temp = new NodeUI(canvas);
//			temp.setLocation(canvas.screenToLocalPoint(e.getLocationOnScreen()));
//			w.addNode(temp);
			canvas.Render();
		} else {
			if (this != ((WireUI) canvas.currSelected).nodes.lastElement()) {
				((WireUI) canvas.currSelected).addNode(this);
				canvas.currSelected = null;
				canvas.mode = currentMode.NONE;
			}

		}
	}

	public void remove() {
		canvas.objectsMap.remove(this);
		for(var w : incidentWires) {
			w.removeNode(this);
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		
	}

	@Override
	public int getPriority() {
		
		return priority;
	}

	public int getNodeIndex() {
		return nodeIndex;
	}
}
