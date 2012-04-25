package wolf.city.map;

import wolf.city.City;
import wolf.noise.FBM;

public abstract class InputMap {
	//protected float[][] m; //totally a memory waster, used to be used, now not.
	
	public int sizeX;
	public int sizeY;
	protected FBM noise;
	protected double delta;
	protected boolean faded;

	public InputMap(City city){
		this(city, false);
	}
	
	public InputMap(City city, boolean faded, int octaves) {
		this.faded = faded;
		this.sizeX = city.sizeX;
		this.sizeY = city.sizeY;
		this.delta = 25;
		//set up noise generator
		noise = new FBM(octaves, city.random.nextLong());
		//set up map
		//m = new float[sizeX][sizeY];
	}
	
	public InputMap(City city, boolean faded){ //for dimensions
		this(city, faded, 6);
	}
	
//	@SuppressWarnings("unused")
//	private InputMap(float[][] map){
//		m = map;
//		sizeX = m.length;
//		sizeY = m[0].length;
//	}

	
//	public InputMap(String file){
//		BufferedImage img = null;
//		try{
//			img = ImageIO.read(new File(file));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//		float[] fArray = null;
//		sizeX = img.getWidth();
//		sizeY = img.getHeight();
//		img.getData().getPixels(0, 0, sizeX, sizeY, fArray);fArray
//		//todo: separate the colors, save into m
//	}
	
	
	public float get(int x, int y) {
		x = x+(sizeX/2);
		y = y+(sizeY/2);
		if(x>=sizeX || x<0){
			return 0;
		}else if(y>=sizeY || y<0){
			return 0;
		}else{
			float result = (float)(noise.noise(x/delta,y/delta));
			if(faded==true){
				double dist = Math.sqrt(Math.pow((sizeX/2)-x, 2)+Math.pow((sizeY/2)-y,2));
				float a = 2f;
				float c = (sizeX+sizeY)/2;
				double fade = Math.pow(a*Math.E,-((Math.pow(dist/c*a, 2))/(1)));
				
				result = (float)(result*fade);
			}
//			if(m[x][y] < 0){
//				
//			}
			//System.out.println("Test:"+(result+m[x][y]));
			return result; //+m[x][y];
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
