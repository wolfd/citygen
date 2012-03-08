package wolf.city;

import java.util.Random;

import wolf.city.map.Population;
import wolf.city.map.Terrain;
import wolf.city.map.Water;
import wolf.city.map.Wealth;
import wolf.gui.CityView;
import wolf.util.MapRender;

public class City {
	//dimensions
	public int sizeX;
	public int sizeY;
	//maps
	public Terrain terrain;
	public Water water;
	public Wealth wealth;
	public Population pop;
	
	public Roadmap rm;
	
	//parameters - style, time period, roadmap generation values
	public Random random;
	
	//statistics
	public Statistics statistics;
	
	public City(int sizeX, int sizeY, long seed){
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		random = new Random(seed);
		for(int i=0; i<10; i++){ //warm up the random number generator
			random.nextDouble();
		}
		terrain = new Terrain(this);
		water = new Water(this);
		wealth = new Wealth(this);
		pop = new Population(this);
		
		//subtract water areas from population?
		
		//make roadmap
		rm = new Roadmap(this);
	}
	
	public void generateRoadmap(){
		CityView cv = new CityView(this);
		rm.generate(cv); 
		MapRender.render(this,"render");
		cv.close();
	}
}


