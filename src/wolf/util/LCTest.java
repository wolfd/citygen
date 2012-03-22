package wolf.util;

import java.awt.Cursor;
import java.util.LinkedList;

import javax.swing.JFrame;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.PixelFormat;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import wolf.city.road.Intersection;
import wolf.city.road.Road;
import wolf.city.road.RoadType;

public class LCTest {
	
	public boolean running = true;
	private JFrame frame;
	private LCPanel panel;

	public LCTest() {
		
		frame = new JFrame("LC testing tool");
		frame.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
		panel = new LCPanel();
		frame.add(panel);
		
		frame.setVisible(true);
		panel.repaint();
		frame.setSize(panel.windowWidth, panel.windowHeight);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}



	
}
