package wolf.gui;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import wolf.city.City;
import wolf.city.buildings.Building;
import wolf.city.road.Intersection;
import wolf.city.road.Road;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class BuildingView {
	private int windowSize = 800;
	private double vSize = 128;
	private boolean densityDisplay = false;
	private Building b;
	
	public BuildingView(Building b){
		this.b = b;
		
		try{
			
			Display.setDisplayMode(new DisplayMode(windowSize, windowSize ));
			Display.create();
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.setProperty("org.lwjgl.opengl.Display.allowSoftwareOpenGL", "true");
			try {
				Display.setDisplayMode(new DisplayMode(windowSize, windowSize ));
				Display.create();
			} catch (LWJGLException e1) {
				e1.printStackTrace();
				System.exit(0);
			}
		}
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(vSize, 0, vSize, 0, 1000, -1000);
		GL11.glViewport(0, 0, windowSize, windowSize);
		
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
	}

	public void draw(){
		if(!Display.isCloseRequested()){
			//Random random = new Random(1);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			
			GL11.glBegin(GL11.GL_LINES);
			for(int i=0; i<b.sections.size(); i++){
				for(int j=0; j < b.sections.get(i).shape.getCoordinates().length; j++){
					Coordinate c = b.sections.get(i).shape.getCoordinates()[j];
					//render stuff;
					GL11.glVertex3d(c.x,c.y,c.z);
				}
				for(int f=0; f<b.sections.get(i).floors.size(); f++){
					
				}
			}
			GL11.glEnd();

			Display.update();
		}else{
			close();
		}
	}

	public void close(){
		Display.destroy();
		//System.exit(0);
	}
}
