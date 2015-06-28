package com.wolfd.citygen.util;

import java.awt.Cursor;
import javax.swing.JFrame;

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
