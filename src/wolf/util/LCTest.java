package wolf.util;

import java.util.LinkedList;

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
	private Road testRoad;
	private Intersection movingIntersection;
	private LinkedList<Road> roads;
	private int windowHeight = 500;
	private int windowWidth = 500;
	public boolean running = true;

	public LCTest() throws LWJGLException{
		roads = new LinkedList<Road>();
		testRoad = new Road(new Intersection(new Coordinate(10,100)),new Intersection(new Coordinate(10,400)), RoadType.STREET);
		roads.add(new Road(new Intersection(new Coordinate(300,10)),new Intersection(new Coordinate(400,10)), RoadType.STREET));
		System.setProperty("org.lwjgl.opengl.Display.allowSoftwareOpenGL", "true");
		Display.create(new PixelFormat());
		Display.setDisplayMode(new DisplayMode(windowWidth, windowHeight));
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, windowWidth, windowHeight, 0, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	}

	public void renderFrame(){
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		if(Mouse.isButtonDown(1)){
			if(movingIntersection == null){
				movingIntersection = getNearestIntersection();
				if(movingIntersection !=null){
					Display.setTitle(movingIntersection.toString());
				}
			}

			moveIntersection();
		}else{
			movingIntersection = null;
		}

		

		GL11.glBegin(GL11.GL_QUADS);
		
		GL11.glColor3f(1f,.0f,.0f);
		GL11.glVertex2d(0,0);
		GL11.glColor3f(0f,0f,.0f);
		GL11.glVertex2d(0, windowHeight);
		GL11.glColor3f(0f,1f,.0f);
		GL11.glVertex2d(windowWidth, windowHeight);
		GL11.glColor3f(0f,0f,.0f);
		GL11.glVertex2d(windowWidth,0);
		
		
		for(int i=0;i<roads.size(); i++){//render roads
			Geometry g = roads.get(i).getGeometry();

			GL11.glColor3f(.5f,.5f,.5f);
			for(int j=0;j<4;j++){
				Coordinate p = g.getCoordinates()[j];
				GL11.glVertex2d(p.x,p.y);
			}

		}
		{//render testRoad
			Geometry g = testRoad.getGeometry();

			GL11.glColor3f(.9f,.5f,.5f);
			for(int j=0;j<4;j++){
				Coordinate p = g.getCoordinates()[j];
				GL11.glVertex2d(p.x,p.y);
				
			}
		}
		GL11.glEnd();


		Display.update();

		if(Display.isCloseRequested()){
			running = false;
			Display.destroy();
			System.exit(0);
		}
	}



	public void moveIntersection(){
		getNearestIntersection();
		if(movingIntersection != null){
			movingIntersection.pos.x = Mouse.getX();
			movingIntersection.pos.y = windowHeight-Mouse.getY();
		}
	}


	public Intersection getNearestIntersection(){
		Coordinate mouseLoc = new Coordinate(Mouse.getX(),windowHeight-Mouse.getY());
		System.out.println(mouseLoc);
		//GL11.glBegin(GL11.GL_POINT);
		//GL11.glVertex2d(mouseLoc.x, mouseLoc.y);
		//GL11.glEnd();
		
		double closestDistance = 25; //25 is largest distance to snap to
		Intersection closest = null;
		
		for(int i=0;i<roads.size();i++){
			if(roads.get(i).a.pos.distance(mouseLoc)<closestDistance) closest = roads.get(i).a;
			if(roads.get(i).b.pos.distance(mouseLoc)<closestDistance) closest = roads.get(i).b;
		}
		if(testRoad.a.pos.distance(mouseLoc)<closestDistance) closest = testRoad.a;
		if(testRoad.b.pos.distance(mouseLoc)<closestDistance) closest = testRoad.b;
		return closest;
		
	}
}
