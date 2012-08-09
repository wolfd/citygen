package wolf.gui;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glEnable;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import wolf.city.City;
import wolf.city.buildings.Building;
import wolf.city.buildings.Floor;
import wolf.city.buildings.shape.WallContainer;
import wolf.city.buildings.shape.WallSection;
import wolf.city.road.Intersection;
import wolf.city.road.Road;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineSegment;

public class BuildingView {
	private int windowSize = 800;
	private double vSize = 16;
	private float angleX = 0;
	private float angleY = 0;
	private float distance = 25;
	private Building b;
	private Coordinate offset;
	
	public BuildingView(Building b){
		this.b = b;
		offset = b.lotShape.getCentroid().getCoordinate();
		float height = 0;
		for(int i=0; i<b.sections.size(); i++){
			for(int f=0; f<b.sections.get(i).floors.size(); f++){
				Floor floor = b.sections.get(i).floors.get(f);
				height += floor.height;
			}
		}
		offset.z = height/2;
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
		GL11.glEnable(GL_DEPTH_TEST);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		//GL11.glOrtho(vSize, -vSize, vSize, -vSize, 1000, -1000);
		GLU.gluPerspective(90f, (float)(vSize/vSize), 1, 300);
		//GL11.glFrustum(vSize, -vSize, vSize, -vSize, 1, 100);
		GL11.glViewport(0, 0, windowSize, windowSize);
		
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
	}

	public boolean draw(){
		if(!Display.isCloseRequested()){
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			//GL11.glFrustum(vSize, -vSize, vSize, -vSize, 1, 100);
			GLU.gluPerspective(90f, (float)(vSize/vSize), 1, 300);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glLoadIdentity();
			// move camera a distance r away from the center
			GL11.glTranslatef(0, 0, -distance);
			angleX += Mouse.getDY();
			angleY += Mouse.getDX();
			distance -= ((float)Mouse.getDWheel()/15f);
			// rotate 
			GL11.glRotatef(angleY, 0, 1, 0);
			GL11.glRotatef(angleX, 1, 0, 0);

			// move to center of whatever   
			//GL11.glTranslatef(-cx, -cy, -cz)
			
			//Random random = new Random(1);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glColor3b((byte)56, (byte)182, (byte)245);
			GL11.glBegin(GL11.GL_QUADS);
			
			float height = 0;
			for(int i=0; i<b.sections.size(); i++){
				for(int j=1; j < b.sections.get(i).shape.getCoordinates().length; j++){
					Coordinate c0 = b.sections.get(i).shape.getCoordinates()[j-1];
					Coordinate c1 = b.sections.get(i).shape.getCoordinates()[j];
					//render stuff;
					//System.out.println(c.x-offset.x+","+(c.y-offset.y)+","+c.z);
					GL11.glVertex3d(c0.x-offset.x,c0.y-offset.y,c0.z-offset.z);
					//GL11.glVertex3d(c1.x-offset.x,c1.y-offset.y,c1.z-offset.z);
				}
				
				for(int f=0; f<b.sections.get(i).floors.size(); f++){
					Floor floor = b.sections.get(i).floors.get(f);
					height += floor.height;
					for(int j=0; j < floor.exterior.size(); j++){
						WallContainer wc = floor.exterior.get(j);
						Coordinate p0 = wc.p0;
						Coordinate p1 = wc.p1;
						
						//render stuff;
						//System.out.println(c.x-offset.x+","+(c.y-offset.y)+","+c.z);
						
						//GL11.glVertex3d(p0.x-offset.x,p0.y-offset.y,p0.z+height-offset.z);
						GL11.glVertex3d(p1.x-offset.x,p1.y-offset.y,p1.z+height-offset.z);
						
						
						GL11.glColor3b((byte)56, (byte)182, (byte)245);
					}
					GL11.glEnd();
					GL11.glBegin(GL11.GL_QUADS);

					GL11.glColor3f(.5f,.4f,.3f);
					for(int j=0; j < floor.exterior.size(); j++){
						WallContainer wc = floor.exterior.get(j);
						Coordinate p0 = wc.p0;
						Coordinate p1 = wc.p1;
						float curPosWall = 0;
						float wallLength = (float)wc.p0.distance(wc.p1);
						LineSegment line = new LineSegment(p0, p1);
						
						for(int k=0; k < wc.wall.children.size(); k++){
							if(k%2==1){
								GL11.glColor3f(.2f,.4f,.6f);
							}else{
								GL11.glColor3f(.5f,.4f,.3f);
							}
							WallSection ws = wc.wall.children.get(k);
							Coordinate wsp0 = line.pointAlong(curPosWall/wallLength);
							curPosWall += ws.length;
							Coordinate wsp1 = line.pointAlong(curPosWall/wallLength);
							wsp1.z = 0;
							wsp0.z = 0;
							GL11.glVertex3d(wsp0.x-offset.x,wsp0.y-offset.y,wsp0.z+height-offset.z);
							GL11.glVertex3d(wsp1.x-offset.x,wsp1.y-offset.y,wsp1.z+height-offset.z);
							GL11.glVertex3d(wsp1.x-offset.x,wsp1.y-offset.y,wsp1.z+height-floor.height-offset.z);
							GL11.glVertex3d(wsp0.x-offset.x,wsp0.y-offset.y,wsp0.z+height-floor.height-offset.z);
						}
					}
					GL11.glColor3b((byte)56, (byte)182, (byte)245);
				}
			}
			GL11.glEnd();

			Display.update();
			return true;
		}else{
			close();
			return false;
		}
	}

	public void close(){
		Display.destroy();
		//System.exit(0);
	}
}
