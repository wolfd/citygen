package wolf.gui.engine;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import wolf.city.City;
import wolf.city.road.Road;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;


public class Camera {
	private static final String WINDOW_TITLE = "World";
	public Vector3f pos;
	public Vector3f rot;
	private int windowWidth = 800;
	private int windowHeight = 640;
	private float fov = 90;
	private float zFar = 10000f;
	private float zNear = .001f;
	private float mouseSensitivity = .1f;


	public Camera(){
		pos = new Vector3f(0,0,100);
		rot = new Vector3f(0,-90,0);

		try {
			Display.setDisplayMode(new DisplayMode(windowWidth, windowHeight));
			Display.create();
		} catch (LWJGLException e) {
			System.setProperty("org.lwjgl.opengl.Display.allowSoftwareOpenGL", "true");
			try {
				Display.setDisplayMode(new DisplayMode(windowWidth, windowHeight));
				Display.create();
			} catch (LWJGLException e1) {
				e.printStackTrace();
				e1.printStackTrace();
				System.exit(0);
			}
		}

		Display.setTitle(WINDOW_TITLE);

		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glViewport(0,0,windowWidth,windowHeight);
		gluPerspective(fov, (float)windowWidth/(float)windowHeight, zNear, zFar);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
	}

	public void rotate(float x, float y, float z){
		rot.x += x;
		rot.y += y;
		rot.z += z;
	}

	public void translate(float x, float y, float z){
		pos.x += x;
		pos.y += y;
		pos.z += z;
	}

	public void moveForward(float dist){
		pos.y += dist*Math.sin(Math.toRadians(rot.x));
		pos.x += dist*Math.cos(Math.toRadians(rot.y));
		//pos.z += z*Math.cos(Math.toRadians(rot.z)); //?? this is probably not right
	}

	private void update(){
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		gluPerspective(fov, (float)windowWidth/(float)windowHeight, zNear, zFar);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		//update matrix
		//gluLookAt(pos.x,pos.y,pos.z,0,0,0,0,-1,0);
		
		glRotatef(rot.x, 1, 0, 0);
		glRotatef(rot.y, 0, 1, 0);
		glRotatef(rot.z, 0, 0, 1);
		glTranslatef(pos.x, pos.y, pos.z);
	}

	public void input(){
		if(Mouse.isButtonDown(0) && Mouse.isInsideWindow()){
			rot.y -= Mouse.getDX()*mouseSensitivity;
			rot.x -= Mouse.getDY()*mouseSensitivity;
		}else{
			Mouse.getDX();
			Mouse.getDY();
		}
		
		if(Keyboard.isKeyDown(Keyboard.KEY_W)){
			moveForward(1f);
		}else{
			moveForward(.001f);
		}
	}

	public void render(City c){
		if(!Display.isCloseRequested()){
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

			input();
			//rotate(0,.1f,0);
			update();
			System.out.println(rot.toString());
			{
				glPointSize(1);
				glBegin(GL11.GL_POINTS);
				glColor3f(.1f, .1f, .1f);
				for(int ix=-100;ix<=100;ix+=10){
					for(int iy=-100;iy<=100;iy+=10){
						for(int iz=-100;iz<=100;iz+=10){
							glVertex3i(ix,iy,iz);
						}
					}
				}
				glEnd();
			}
			if(c.rm.roads != null && c.rm.roads.size() > 0){ //road render
				int red = 0;
				int blue = 0;
				int alpha = 0;
				int green = 0;
				if(!c.rm.finished){ //render endpoints of last road generated
					GL11.glPointSize(20);
					GL11.glBegin(GL11.GL_POINTS);
					GL11.glColor3f(1, 1, 1);
					Road road = c.rm.roads.get(c.rm.roads.size()-1);
					Coordinate p1 = road.a.pos;
					Coordinate p2 = road.b.pos;
					GL11.glVertex2d(p1.x,p1.y);
					GL11.glVertex2d(p2.x,p2.y);
					GL11.glEnd();
				}
				for(int i=0; i<c.rm.roads.size(); i++){
					Road road = c.rm.roads.get(i);
					Geometry g = road.getGeometry();

					switch(road.getType()){
					case BRIDGE:{ //grey
						red = 50;
						green = 50;
						blue = 50;
						break;
					}
					case HIGHWAY:{ //blue-green
						red = 0;
						green = 50;
						blue = 50;
						break;
					}
					case STREET:{ //green
						red = 0;
						green = 150;
						blue = 50;
						break;
					}
					case MAIN:{ //green
						red = 0;
						green = 100;
						blue = 100;
						break;
					}
					case DEFAULT:{ //red
						red = 255;
						green = 0;
						blue = 0;
						break;
					}
					default:{ //red
						red = 255;
						green = 0;
						blue = 0;
						break;
					}
					}
					GL11.glBegin(GL11.GL_QUADS);
					GL11.glColor4ub((byte)red, (byte)green, (byte)blue, (byte)alpha);
					for(int j=0;j<4;j++){
						Coordinate p = g.getCoordinates()[j];
						GL11.glVertex2d(p.x,p.y);
					}
				}
				GL11.glEnd();
			}
			Display.update();
		}else{
			Display.destroy();
			System.exit(0);
		}
	}

}
