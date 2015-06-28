package com.wolfd.citygen.util;

import java.util.List;

import com.wolfd.citygen.city.City;
import com.wolfd.citygen.city.Roadmap;
import com.wolfd.citygen.city.road.Road;

public class MapRender {
	public static void render(City c, String file) {
		int sizeX = c.sizeX;
		int sizeY = c.sizeY;
		Roadmap rm;
		String filename;
		float d = 1f; //delta (times smaller)
		rm = c.rm;
		filename = file;
		ImageOutput img = new ImageOutput(sizeX, sizeY);
		/*for(int iy=0; iy<sizeY; iy++){
			for(int ix=0; ix<sizeX; ix++){
				float red = c.pop.get(ix-(sizeX/2), iy-(sizeY/2));
				float blue = c.water.get(ix-(sizeX/2), iy-(sizeY/2));
				if(blue>c.rm.noWaterCutoffDensity){
					blue = .5f;
				}else{
					blue = 0;
				}
				img.setColor(ix,sizeY-iy-1, red, 0, 0);
			}
		}
		*/
		List<Road> roads = rm.roads.queryAll();
		for(int i=roads.size()-1; i>=0; i--){
			Road r = roads.get(i);
			int width;
			int red;
			int green;
			int blue;
			switch(r.getType()){
			case BRIDGE:{ //grey
				width=r.getType().getWidth();
				red = 70;
				green = 70;
				blue = 70;
				break;
			}
			case HIGHWAY:{ //blue
				width=r.getType().getWidth();
				red = 50;
				green = 50;
				blue = 50;
				break;
			}
			case STREET:{ //green
				width=r.getType().getWidth();
				red = 55;
				green = 55;
				blue = 55;
				break;
			}
			case MAIN:{ //green
				width=r.getType().getWidth();
				red = 45;
				green = 45;
				blue = 45;
				break;
			}
			case DEFAULT:{ //red
				width=r.getType().getWidth();
				red = 255;
				green = 0;
				blue = 0;
				break;
			}
			default:{ //red
				width=4;
				red = 255;
				green = 0;
				blue = 0;
				break;
			}
			}
			//img.line((int)r.a.pos.x, (int)r.a.pos.y, (int)r.b.pos.x, (int)r.b.pos.y, width, 255, 255, 255);
			img.line((int)(r.a.pos.x/d)+(sizeX/2) , (int)(-r.a.pos.y/d)+(sizeY/2), (int)(r.b.pos.x/d)+(sizeX/2), (int)(-r.b.pos.y/d)+(sizeY/2), (int)(width/d), red, green, blue);

		}
		img.saveImage(filename);

	}

}
