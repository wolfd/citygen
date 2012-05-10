package wolf.city;

import java.sql.SQLException;
import java.util.Random;

import wolf.city.map.Population;
import wolf.city.map.Terrain;
import wolf.city.map.Water;
import wolf.city.map.Wealth;
import wolf.gui.CityView;
import wolf.util.Database;
import wolf.util.Log;
import wolf.util.MapRender;
import wolf.util.Popup;

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
	public Blockmap bm;
	public FakeBuildings fb;

	//parameters - style, time period, roadmap generation values
	public Random random;

	//statistics
	public Statistics statistics;
	public Log log;

	public City(int sizeX, int sizeY, long seed){
		log = new Log();
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		random = new Random(seed);
		for(int i=0; i<10; i++){ //warm up the random number generator
			random.nextDouble();
		}
		//terrain = new Terrain(this);
		water = new Water(this);
		//wealth = new Wealth(this);
		pop = new Population(this);

		rm = new Roadmap(this);
		bm = new Blockmap(this);
		fb = new FakeBuildings(this);
		log.log("Seed: "+seed);
	}

	public void generateRoadmap(){
		//final CityView cv = new CityView(this);
		rm.generate(/*cv*/);
		bm.getBlocks(rm);
		fb.generate();
		bm.save("data/blocks.txt", "data/lots.txt");
		
		try {
			Database d = new Database();
			d.open("city.db");
			d.saveCityData(this);
			d.close();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//cv.close();
		//		if(Popup.confirm("Render?", "CityGen")){
		//			MapRender.render(this,"render");
		//		}
		//		log.save("/log.log");
		catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void windowClosed(){
		if(Popup.confirm("Render?", "CityGen")){
			MapRender.render(this,"render");
		}
		log.save("/log-"+System.currentTimeMillis()+".log");
	}
}


