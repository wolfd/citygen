package com.wolfd.citygen.city.map;

import com.wolfd.citygen.city.City;
import com.wolfd.citygen.util.noise.FBM;

public abstract class InputMap{
	//protected float[][] m; //totally a memory waster, used to be used, now not.
	
	public int sizeX;
	public int sizeY;
	protected FBM noise;
	protected double delta;
	protected boolean faded;
	private double zoom;

	public InputMap(City city){
		this(city, false);
	}
	
	public InputMap(City city, boolean faded, int octaves, double zoom) {
		this.faded = faded;
		this.sizeX = city.sizeX;
		this.sizeY = city.sizeY;
		this.delta = 25;
		this.zoom = zoom;
		//set up noise generator
		noise = new FBM(octaves, city.random.nextLong());
	}
	
	public InputMap(City city, boolean faded){ //for dimensions
		this(city, faded, 6, 1);
	}
	
	public float get(double x, double y) {
		x = x+(sizeX/2);
		y = y+(sizeY/2);
		if(x>=sizeX || x<0){
			return 0;
		}else if(y>=sizeY || y<0){
			return 0;
		}else{
			x *= zoom;
			y *= zoom;
			float result = (float)(noise.noise(x/delta,y/delta));
			if(faded){
				double dist = Math.sqrt(Math.pow((sizeX/2)-x, 2)+Math.pow((sizeY/2)-y,2));
				float a = 2f;
				float c = (sizeX+sizeY)/2;
				double fade = Math.pow(a*Math.E,-((Math.pow(dist/c*a, 2))/(1)));
				
				result = (float)(result*fade);
			}
			return result;
		}
	}
	
	public float getCircleAvg(int x, int y, int r){
		//long timeStart = System.currentTimeMillis();
		float sum = 0;
		int iter = 0;
		for(int ix=(x-r); ix<(x+r); ix++){
			for(int iy=(y-r); iy<(y+r); iy++){
				float distanceToCenter = (float) Math.sqrt((x-ix)*(x-ix)+(y-iy)*(y-iy));
				if(distanceToCenter<r){
					iter += 1;
					sum += get(ix, iy);
				}
			}
		}
		//System.out.println("Time for circle avg:"+(System.currentTimeMillis()-timeStart));
		//System.out.println("Result:"+(sum/iter)+" Sum:"+sum+" Iter:"+iter);
		if(iter > 0){
			return sum/iter;
		}else{
			return 0;
		}
	}
	
	
}
