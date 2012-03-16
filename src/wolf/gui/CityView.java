package wolf.gui;

import java.util.Random;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import wolf.city.City;
import wolf.city.road.Road;

public class CityView extends Thread{
	private int windowSize = 800;
	private double vSize;
	private boolean densityDisplay = false;
	private City c;
	public CityView(City c){
		this.c = c;
		vSize = Math.max(c.sizeX, c.sizeY);
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
		GL11.glOrtho(0, vSize, vSize, 0, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	}

	public void run(){
		
	}

	public void draw(){
		if(!Display.isCloseRequested()){
			Random random = new Random(1);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			//render everything
			if(densityDisplay){
				boolean updateDensity;
				if(Math.random() > .99){
					updateDensity = true;
				}else{
					updateDensity = false;
				}

				//population
				if(updateDensity){
					GL11.glBegin(GL11.GL_POINTS);
					for(int ix=-c.sizeX/2; ix<c.sizeX/2; ix++){
						for(int iy=-c.sizeY/2; iy<c.sizeY/2; iy++){
							float density = c.pop.get(ix, iy);
							GL11.glColor3f(density, 0f, 0f);
							GL11.glVertex2i(ix+c.sizeX/2, (int) (vSize-(iy+c.sizeY/2)));
						}
					}
					GL11.glEnd();
				}
			}

			//roads
			int red = 0;
			int blue = 0;
			int alpha = 0;
			int green = 0;

			//GL11.glEnable(GL11.GL_BLEND);
			//GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);


			{
				GL11.glPointSize(20);
				GL11.glBegin(GL11.GL_POINTS);
				GL11.glColor3f(1, 1, 1);
				Road road = c.rm.roads.get(c.rm.roads.size()-1);
				Coordinate p1 = road.a.pos;
				Coordinate p2 = road.b.pos;
				GL11.glVertex2d(p1.x+c.sizeX/2,vSize-(p1.y+c.sizeY/2));
				GL11.glVertex2d(p2.x+c.sizeX/2,vSize-(p2.y+c.sizeY/2));
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
					byte[] bytes = new byte[3];
					random.nextBytes(bytes);
					red = 0+bytes[0];
					green = 150+bytes[1];
					blue = 50+bytes[2];
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
					GL11.glVertex2d(p.x+c.sizeX/2,vSize-(p.y+c.sizeY/2));
				}
			}
			GL11.glEnd();

			Display.update();
			Display.setTitle("Roads: "+c.rm.roads.size());
		}else{
			close();
		}
	}

	public void close(){
		c.windowClosed();
		Display.destroy();
		System.exit(0);
	}
}