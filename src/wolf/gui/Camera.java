package wolf.gui;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.ARBFragmentShader;
import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.ARBVertexShader;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.glu.GLUtessellator;
import org.lwjgl.util.glu.GLUtessellatorCallback;
import org.lwjgl.util.glu.GLUtessellatorCallbackAdapter;
import org.lwjgl.util.glu.Sphere;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Vector3f;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import wolf.city.City;
import wolf.city.block.CityBlock;
import wolf.city.block.Lot;
import wolf.city.buildings.FakeBuilding;
import wolf.city.road.Road;
import wolf.util.tess.TessCallback;
import wolf.util.tess.VertexData;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;


public class Camera {
	private static final String WINDOW_TITLE = "World";
	public Vector3f pos;
	public Vector3f force;
	public Vector3f rot;
	private int windowWidth = 800;
	private int windowHeight = 640;
	private float fov = 80;
	private float zFar = 4000f;
	private float zNear = 4f;
	private float mouseSensitivity = .2f;
	private boolean lookAtCenter;
	private boolean renderBuildings = true;
	private boolean renderBlocksAndLots = false;
	private GLUtessellator tess = gluNewTess();

	private int shader = 0;
	private int vertShader = 0;
	private int fragShader = 0;

	private boolean useShader = true;
	private int program=0;

	private int cityList;
	private City c;
	private long lastNanoTime;


	public Camera(City city){
		pos = new Vector3f(0,0,-1000);
		rot = new Vector3f(0,0,0);
		force = new Vector3f(0,0,0);
		c = city;
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
		//glEnable(GL_LIGHTING);
		glEnable(GL_DEPTH_TEST);
		//		glEnable(GL_LIGHTING);
		//glEnable(GL_LIGHT0);
		//glEnable(GL_COLOR_MATERIAL);
		//glColorMaterial(GL_FRONT,GL_AMBIENT_AND_DIFFUSE);
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
		
		tess.gluTessProperty(GLU_TESS_WINDING_RULE, GLU_TESS_WINDING_POSITIVE);
		GLUtessellatorCallback callback = new TessCallback();
        tess.gluTessCallback(GLU_TESS_VERTEX, callback);
        tess.gluTessCallback(GLU_TESS_BEGIN, callback);
        tess.gluTessCallback(GLU_TESS_END, callback);
        tess.gluTessCallback(GLU_TESS_COMBINE, callback);
		/*
		 * create the shader program. If OK, create vertex
		 * and fragment shaders
		 */
		try {
			vertShader = createShader("shaders/basic.vert",ARBVertexShader.GL_VERTEX_SHADER_ARB);
			fragShader = createShader("shaders/basic.frag",ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);
		}
		catch(Exception exc) {
			exc.printStackTrace();
			return;
		}
		finally {
			if(vertShader == 0 || fragShader == 0){
				System.out.println("Shaders broke or something");
				return;
			}
		}

		program = ARBShaderObjects.glCreateProgramObjectARB();

		if(program == 0)
			return;

		/*
		 * if the vertex and fragment shaders setup sucessfully,
		 * attach them to the shader program, link the sahder program
		 * (into the GL context I suppose), and validate
		 */
		ARBShaderObjects.glAttachObjectARB(program, vertShader);
		ARBShaderObjects.glAttachObjectARB(program, fragShader);

		ARBShaderObjects.glLinkProgramARB(program);
		if (ARBShaderObjects.glGetObjectParameteriARB(program, ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB) == GL_FALSE) {
			System.err.println(getLogInfo(program));
			return;
		}

		ARBShaderObjects.glValidateProgramARB(program);
		if (ARBShaderObjects.glGetObjectParameteriARB(program, ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB) == GL_FALSE) {
			System.err.println(getLogInfo(program));
			return;
		}

		useShader = true;



		//set up 3d info
		//Random r = new Random(1);
		cityList = glGenLists(1);

		glNewList(cityList, GL_COMPILE);
		//		Sphere s = new Sphere();
		//		s.draw(150, 25, 25);
		//make the display list
		if(c.rm.roads != null && c.rm.roads.size() > 0){ //road render
			int red = 0;
			int blue = 0;
			int alpha = 0;
			int green = 0;

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
				case HIGHWAY:{
					red = 50;
					green = 50;
					blue = 55;
					break;
				}
				case STREET:{
					red = 50;
					green = 50;
					blue = 55;
					break;
				}
				case MAIN:{
					red = 75;
					green = 75;
					blue = 75;
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
				Coordinate[] cs = g.reverse().getCoordinates();
				if(cs.length >= 3){
					glBegin(GL_QUADS);
					glColor4ub((byte)red, (byte)green, (byte)blue, (byte)alpha);

					Vector3f n = normal(cs[0], cs[1], cs[2]);
					glNormal3f(n.x, n.y, n.z);
					for(int j=0;j<4;j++){
						Coordinate p = cs[j];
						glVertex3d(p.x,p.y,c.ter.get((int)p.x, (int)p.y));

					}
					glEnd();
				}
			}

		}
		if(renderBlocksAndLots  && c.bm.blocks != null && c.bm.blocks.size() > 0){
			for(CityBlock b : c.bm.blocks){
				if(b.lots != null && b.lots.size()>0){
					for(Lot l: b.lots){

						Coordinate[] cs = l.shape.reverse().getCoordinates();
						glBegin(GL_LINE_LOOP);
						glColor3f(.3f,.3f,.3f);
						//Coordinate q = cs[cs.length-1];
						//glVertex2d(q.x,q.y);
						for(int j=0;j<cs.length;j++){
							Coordinate p = cs[j];
							glVertex3d(p.x,p.y,c.ter.get((int)p.x, (int)p.y));
						}
						glEnd();
					}
					Coordinate[] cs = b.shape.getCoordinates();
					glBegin(GL_LINE_LOOP);
					glColor3f(.2f,.2f,.3f);
					for(int j=0;j<cs.length;j++){
						Coordinate p = cs[j];
						glVertex3d(p.x,p.y,c.ter.get((int)p.x, (int)p.y));

					}
					glEnd();
				}else{
					//render block
				}
			}
		}
		if(renderBuildings && c.fb != null && c.fb.buildings.size() > 0){
			if(useShader){ 
				ARBShaderObjects.glUseProgramObjectARB(program);
				GL20.glUniform1f(GL20.glGetUniformLocation(program, "shininess"), 1f);
				System.out.println("Using shader");
			}
			for(FakeBuilding b : c.fb.buildings){
				Coordinate[] cs = b.g.getCoordinates();
				if(!CGAlgorithms.isCCW(cs)) cs = b.g.reverse().getCoordinates(); 
				if(cs.length >= 3){
					//glBegin(GL_TRIANGLE_STRIP);
					Vector3f n = normal(cs[0], cs[1], cs[2]);
					//render top of building
					tess.gluTessBeginPolygon(null);
					tess.gluTessBeginContour();
					tess.gluTessNormal(n.x, n.y, n.z);
					for(int j=cs.length-1; j>=0; j--){
						Coordinate p = cs[j];
						double[] vert = new double[]{p.x,p.y,p.z+b.height};
						tess.gluTessVertex(vert, 0, new VertexData(vert));
					}
					tess.gluTessEndContour();
					tess.gluTessEndPolygon();
					//glEnd();
					/*glBegin(GL_LINES);
					glVertex3d(cs[0].x,cs[0].y,cs[0].z+b.height);
					float normalLen = 50;
					glVertex3d(cs[0].x+(n.x*normalLen),cs[0].y+(n.y*normalLen),cs[0].z+b.height+(n.z*normalLen));
					glEnd();*/
					glBegin(GL_POLYGON);
					//render bottom of building
//					tess.gluBeginPolygon();
//					tess.gluTessBeginContour();
//					tess.gluTessNormal(n.x, n.y, -n.z);
					for(int j=0;j<cs.length;j++){
						Coordinate p = cs[j];
						glVertex3d(p.x,p.y,p.z);

					}
//					tess.gluTessEndContour();
//					tess.gluEndPolygon();
					glEnd();

					//float rand = r.nextFloat()/5;
					//glColor3f(.3f+rand,.3f+rand,.3f+rand);

					if(cs.length>0){
						Coordinate q = cs[0];

						for(int j=cs.length-1;j>=0;j--){
							//glColor3f(r.nextFloat(),r.nextFloat(),r.nextFloat());
							Coordinate p = cs[j];
							Vector3f n1 = normal(new Coordinate(q.x,q.y,p.z), new Coordinate(q.x,q.y,p.z+b.height), new Coordinate(p.x,p.y,p.z+b.height));
							glBegin(GL_QUADS);
							glNormal3f(n1.x, n1.y, n1.z);
							glVertex3d(q.x,q.y,p.z);
							glVertex3d(q.x,q.y,p.z+b.height);
							glVertex3d(p.x,p.y,p.z+b.height);							
							glVertex3d(p.x,p.y,p.z);
							glEnd();

							q = cs[j];
						}
						
						/*
						Coordinate q = cs[cs.length-1];

						for(int j=0;j<cs.length;j++){
							//glColor3f(r.nextFloat(),r.nextFloat(),r.nextFloat());
							Coordinate p = cs[j];
							Vector3f n1 = normal(new Coordinate(q.x,q.y,p.z), new Coordinate(q.x,q.y,p.z+b.height), new Coordinate(p.x,p.y,p.z+b.height));
							glBegin(GL_QUADS);
							glNormal3f(n1.x, n1.y, n1.z);
							glVertex3d(q.x,q.y,p.z);
							glVertex3d(q.x,q.y,p.z+b.height);
							glVertex3d(p.x,p.y,p.z+b.height);							
							glVertex3d(p.x,p.y,p.z);
							glEnd();

							q = cs[j];
						}
						 */
					}

				}
			}
			//release the shader
			ARBShaderObjects.glUseProgramObjectARB(0);
		}
		ByteBuffer posbuff = ByteBuffer.allocateDirect(4*Float.SIZE/8);
		posbuff.putFloat(1.0f).putFloat(1.0f).putFloat(1.0f).putFloat(0.0f).flip();
		glLight(GL_LIGHT0, GL_POSITION, posbuff.asFloatBuffer());
		glEndList();

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
		glRotatef(rot.x, 1, 0, 0);
		glRotatef(rot.y, 0, 1, 0);
		glRotatef(rot.z, 0, 0, 1);

		if(lookAtCenter){
			gluLookAt(-pos.x,-pos.y,-pos.z,0,0,0,0,1,0);
		}else{
			//gluLookAt(-pos.x,-pos.y,-pos.z,0,0,0,0,1,0);
			glTranslatef(pos.x, pos.y, pos.z);
		}
	}

	//newell's method
	private static Vector3f normal(Coordinate[] cs){
		Vector3f normal = new Vector3f(0,0,0);
		for(int i=0; i<cs.length; i++){
			Vector3f cur = new Vector3f((float)cs[i].x, (float)cs[i].y, (float)((cs[i].z != Double.NaN) ? cs[i].z : 0));
			Vector3f next = new Vector3f((float)cs[(i+1) % cs.length].x, (float)cs[(i+1) % cs.length].y, (float)((cs[(i+1) % cs.length].z != Double.NaN) ? cs[(i+1) % cs.length].z : 0));

			normal.x = normal.x + ((cur.y - next.y) * (cur.z - next.z));
			normal.y = normal.y + ((cur.z - next.z) * (cur.x - next.x));
			normal.z = normal.z + ((cur.x - next.x) * (cur.y - next.y));
		}

		//normalize the normal vector!
		normalize(normal);
		return normal;
	}

	private static Vector3f normal(Coordinate a, Coordinate b, Coordinate c){
		Vector3f u = new Vector3f((float)(b.x-a.x), (float)(b.y-a.y), (float)(b.z-a.z));
		Vector3f v = new Vector3f((float)(c.x-a.x), (float)(c.y-a.y), (float)(c.z-a.z));

		return normalize(new Vector3f((u.y*u.z)-(u.z*v.y), ((u.z*v.x)-(u.x*v.z)), ((u.x*v.y)-(u.y*v.x))));
	}

	private static Vector3f normalize(Vector3f v){
		float length = (float)Math.sqrt(Math.pow(v.x,2)+Math.pow(v.y,2)+Math.pow(v.z,2));
		v.x /= length;
		v.y /= length;
		v.z /= length;
		return v;
	}

	private void input(){
		long nanoTime = System.nanoTime();
		long deltaTime = nanoTime-lastNanoTime;
		lastNanoTime = nanoTime;
		if(Mouse.isButtonDown(0) && Mouse.isInsideWindow()){
			//Mouse.setGrabbed(true);
			rot.y += Mouse.getDX()*mouseSensitivity;
			rot.x -= Mouse.getDY()*mouseSensitivity;
		}else{
			//Mouse.setGrabbed(false);
			Mouse.getDX();
			Mouse.getDY();
		}
		float speed;
		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)){
			speed = .0000000005f;
		}else{
			speed = .0000000001f;
		}

		if(Keyboard.isKeyDown(Keyboard.KEY_Q)){
			//move(new Vector3f(0,0,1),.000001f);
			addForce(0,0,-speed*deltaTime);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_E)){
			//move(new Vector3f(0,0,1),.000001f);
			addForce(0,0,speed*deltaTime);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_S)){
			//move(new Vector3f(0,0,1),.000001f);
			addForce(0,speed*deltaTime,0);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_W)){
			//move(new Vector3f(0,0,1),.000001f);
			addForce(0,-speed*deltaTime,0);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_A)){
			//move(new Vector3f(0,0,1),.000001f);
			addForce(speed*deltaTime,0,0);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_D)){
			//move(new Vector3f(0,0,1),.000001f);
			addForce(-speed*deltaTime,0,0);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_Z)){
			force = new Vector3f(0,0,0);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_SPACE)){
			lookAtCenter = true;
		}else{
			lookAtCenter = false;
		}
	}

	public void render(){
		if(!Display.isCloseRequested()){
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

			input();
			//rotate(0,0,.01f);
			update();
			glCallList(cityList);

			Display.update();
		}else{
			Display.destroy();
			c.windowClosed();
			System.exit(0);
		}
	}

	/*
	 * With the exception of syntax, setting up vertex and fragment shaders
	 * is the same.
	 * @param the name and path to the vertex shader
	 */
	private int createShader(String filename, int shaderType) throws Exception {
		int shader = 0;
		try {
			shader = ARBShaderObjects.glCreateShaderObjectARB(shaderType);

			if(shader == 0)
				return 0;

			ARBShaderObjects.glShaderSourceARB(shader, readFileAsString(filename));
			ARBShaderObjects.glCompileShaderARB(shader);

			if (ARBShaderObjects.glGetObjectParameteriARB(shader, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL_FALSE)
				throw new RuntimeException("Error creating shader: " + getLogInfo(shader));

			return shader;
		}
		catch(Exception exc) {
			ARBShaderObjects.glDeleteObjectARB(shader);
			throw exc;
		}
	}

	private static String getLogInfo(int obj) {
		return ARBShaderObjects.glGetInfoLogARB(obj, ARBShaderObjects.glGetObjectParameteriARB(obj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
	}

	private String readFileAsString(String filename) throws Exception {
		StringBuilder source = new StringBuilder();

		FileInputStream in = new FileInputStream(filename);

		Exception exception = null;

		BufferedReader reader;
		try{
			reader = new BufferedReader(new InputStreamReader(in,"UTF-8"));

			Exception innerExc= null;
			try {
				String line;
				while((line = reader.readLine()) != null)
					source.append(line).append('\n');
			}
			catch(Exception exc) {
				exception = exc;
			}
			finally {
				try {
					reader.close();
				}
				catch(Exception exc) {
					if(innerExc == null)
						innerExc = exc;
					else
						exc.printStackTrace();
				}
			}

			if(innerExc != null)
				throw innerExc;
		}
		catch(Exception exc) {
			exception = exc;
		}
		finally {
			try {
				in.close();
			}
			catch(Exception exc) {
				if(exception == null)
					exception = exc;
				else
					exc.printStackTrace();
			}

			if(exception != null)
				throw exception;
		}

		return source.toString();
	}
}
