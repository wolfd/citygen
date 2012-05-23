package wolf.gui;

import java.util.Random;

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
import wolf.city.block.CityBlock;
import wolf.city.block.Lot;
import wolf.city.buildings.FakeBuilding;
import wolf.city.road.Road;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;


public class Camera {
	private static final String WINDOW_TITLE = "World";
	public Vector3f pos;
	public Vector3f force;
	public Vector3f rot;
	private int windowWidth = 800;
	private int windowHeight = 640;
	private float fov = 110;
	private float zFar = 3000f;
	private float zNear = 1f;
	private float mouseSensitivity = .2f;
//	private boolean lookAtCenter;
	private boolean renderBuildings = true;
	private boolean renderBlocksAndLots = false;


	public Camera(){
		pos = new Vector3f(0,0,1000);
		rot = new Vector3f(0,180,0);
		force = new Vector3f(0,0,0);

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
		glEnable(GL_LINE_SMOOTH);
		//glEnable(GL_CULL_FACE);
		glEnable(GL_DEPTH_TEST);
//		glEnable(GL_LIGHTING);
//		glEnable(GL_LIGHT0);
//		glEnable(GL_COLOR_MATERIAL);
//		glColorMaterial(GL_FRONT_AND_BACK,GL_AMBIENT_AND_DIFFUSE);
		//glEnable(GL_BLEND);
		//glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
		glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
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
	
	public void addForce(float x, float y, float z){
		force.x += x;
		force.y += y;
		force.z += z;
	}

	public void move(Vector3f v, float dist){
		Vector3f mx = (Vector3f) rotX(rotY(new Vector3f(1,0,0), rot.x), rot.y).scale(v.x);
		Vector3f my = (Vector3f) rotX(rotY(new Vector3f(0,1,0), rot.x), rot.y).scale(v.y);
		Vector3f mz = (Vector3f) rotX(rotY(new Vector3f(0,0,1), rot.x), rot.y).scale(v.z);

		Vector3f posChange1 = new Vector3f();
		Vector3f posChange = new Vector3f();
		Vector3f.add(mx, my, posChange1);
		Vector3f.add(posChange1, mz, posChange);

		Vector3f.add(posChange, pos, pos);
	}

	/*
	 * 	p += v.x*roty(rotx(vec(1.0, 0.0, 0.0), CameraPhi), CameraTheta) +
	 *	v.y*roty(rotx(vec(0.0, 1.0, 0.0), CameraPhi), CameraTheta) +
	 *	v.z*roty(rotx(vec(0.0, 0.0, 1.0), CameraPhi), CameraTheta);
	 */

	public Vector3f rotX(Vector3f v, double a){
		double c = Math.toRadians(a);
		return new Vector3f(v.x, (float)(v.y*Math.cos(c) - v.z*Math.sin(c)), (float)(v.y*Math.sin(c) + v.z*Math.cos(c)));
	}

	public Vector3f rotY(Vector3f v, double a){
		double c = Math.toRadians(a);
		return new Vector3f((float)(v.x*Math.cos(c) + v.z*Math.sin(c)), v.y, (float)(-v.x*Math.sin(c) + v.z*Math.cos(c)));
	}

	public Vector3f rotZ(Vector3f v, double a){
		double c = Math.toRadians(a);
		return new Vector3f((float)(v.x*Math.cos(c) - v.y*Math.sin(c)), (float)(v.x*Math.sin(c) + v.y*Math.cos(c)), v.z);
	}

	private void update(){
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		gluPerspective(fov, (float)windowWidth/(float)windowHeight, zNear, zFar);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		//update matrix
		
		Vector3f.add(force, pos, pos);
//		if(lookAtCenter){
//			gluLookAt(pos.x,pos.y,pos.z,0,0,0,0,1,0);
//		}
		glRotatef(rot.x, 1, 0, 0);
		glRotatef(rot.y, 0, 1, 0);
		glRotatef(rot.z, 0, 0, 1);
		glTranslatef(pos.x, pos.y, pos.z);
		
	}

	public void input(){
		if(Mouse.isButtonDown(0) && Mouse.isInsideWindow()){
			rot.y += Mouse.getDX()*mouseSensitivity;
			rot.x -= Mouse.getDY()*mouseSensitivity;
		}else{
			Mouse.getDX();
			Mouse.getDY();
		}
		float speed;
		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)){
			speed = .05f;
		}else{
			speed = .01f;
		}
		
		if(Keyboard.isKeyDown(Keyboard.KEY_Q)){
			//move(new Vector3f(0,0,1),.000001f);
			addForce(0,0,speed);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_E)){
			//move(new Vector3f(0,0,1),.000001f);
			addForce(0,0,-speed);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_S)){
			//move(new Vector3f(0,0,1),.000001f);
			addForce(0,speed,0);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_W)){
			//move(new Vector3f(0,0,1),.000001f);
			addForce(0,-speed,0);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_A)){
			//move(new Vector3f(0,0,1),.000001f);
			addForce(-speed,0,0);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_D)){
			//move(new Vector3f(0,0,1),.000001f);
			addForce(speed,0,0);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_Z)){
			force = new Vector3f(0,0,0);
		}
//		if(Keyboard.isKeyDown(Keyboard.KEY_SPACE)){
//			lookAtCenter = true;
//		}else{
//			lookAtCenter = false;
//		}
	}

	public void render(City c){
		if(!Display.isCloseRequested()){
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			Random r = new Random(1);
			input();
			//rotate(0,0,.01f);
			update();
			//System.out.println(pos.toString());
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
					float zShift = 0f;
					switch(road.getType()){
					case BRIDGE:{ //grey
						red = 50;
						green = 50;
						blue = 50;
						zShift = 1f;
						break;
					}
					case HIGHWAY:{
						red = 50;
						green = 50;
						blue = 55;
						zShift = 5f;
						break;
					}
					case STREET:{
						red = 50;
						green = 50;
						blue = 55;
						zShift = 0f;
						break;
					}
					case MAIN:{
						red = 75;
						green = 75;
						blue = 75;
						zShift = .25f;
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
						GL11.glVertex3d(p.x,p.y,-zShift);
					}
				}
				GL11.glEnd();
			}
			if(renderBlocksAndLots  && c.bm.blocks != null && c.bm.blocks.size() > 0){
				for(CityBlock b : c.bm.blocks){
					if(b.lots != null && b.lots.size()>0){
						for(Lot l: b.lots){

							Coordinate[] cs = l.shape.getCoordinates();
							glBegin(GL_LINE_LOOP);
							glColor3f(.3f,.3f,.3f);
							//Coordinate q = cs[cs.length-1];
							//glVertex2d(q.x,q.y);
							for(int j=0;j<cs.length;j++){
								Coordinate p = cs[j];
								glVertex2d(p.x,p.y);
							}
							glEnd();
						}
						Coordinate[] cs = b.shape.getCoordinates();
						glBegin(GL_LINE_LOOP);
						glColor3f(.2f,.2f,.3f);
						for(int j=0;j<cs.length;j++){
							Coordinate p = cs[j];
							glVertex3d(p.x,p.y, -.1f);
						}
						glEnd();
					}else{
						//render block
					}
				}
			}
			if(renderBuildings && c.fb != null && c.fb.buildings.size() > 0){
				for(FakeBuilding b : c.fb.buildings){
					Coordinate[] cs = b.g.getCoordinates();
					glBegin(GL_POLYGON);
					glColor3f(.2f,.2f,.2f);
					for(int j=0;j<cs.length;j++){
						Coordinate p = cs[j];
						glVertex3d(p.x,p.y,0);
					}
					glEnd();
					glBegin(GL_POLYGON);
					glColor3f(.2f,.2f,.2f);
					for(int j=0;j<cs.length;j++){
						Coordinate p = cs[j];
						glVertex3d(p.x,p.y,-b.height);
					}
					glEnd();
					glBegin(GL_QUADS);
					float rand = r.nextFloat()/5;
					glColor3f(.3f+rand,.3f+rand,.3f+rand);
					if(cs.length>0){
						Coordinate q = cs[cs.length-1];
						for(int j=0;j<cs.length;j++){
							//glColor3f(r.nextFloat(),r.nextFloat(),r.nextFloat());
							Coordinate p = cs[j];
							glVertex3d(p.x,p.y,0);
							glVertex3d(p.x,p.y,-b.height);
							glVertex3d(q.x,q.y,-b.height);
							glVertex3d(q.x,q.y,0);

							q = cs[j];
						}
					}
					glEnd();
				}
			}
			Display.update();
		}else{
			Display.destroy();
			c.windowClosed();
			System.exit(0);
		}
	}

}
