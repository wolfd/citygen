package wolf.city;

import java.sql.SQLException;
import java.util.Random;

import wolf.city.buildings.FakeBuildings;
import wolf.city.map.Population;
import wolf.city.map.Terrain;
import wolf.city.map.Water;
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
	public Water water;
	public Population pop;
	public Terrain ter;

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
		water = new Water(this);
		pop = new Population(this);
		ter = new Terrain(this);

		rm = new Roadmap(this);
		bm = new Blockmap(this);
		fb = new FakeBuildings(this);
		log.log("Seed: "+seed);
	}

	public void generateRoadmap(boolean viewCity){
		if(viewCity){
			final CityView cv = new CityView(this);
			rm.generate(cv);
			cv.close();
		}else{
			rm.generate();
		}
		log.log("Done generating roads");
		bm.getBlocks(rm);
		log.log("Done generating city blocks");
		fb.generate();
		log.log("Done generating mock buildings");
		bm.save("data/blocks.txt", "data/lots.txt");
		
		try {
			Database d = new Database();
			d.open("data/city.db");
			d.saveCityData(this);
			d.close();
			log.log("Database saved");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.err.println("Sqlite did not launch correctly!");
		} catch (SQLException e) {
			System.err.println("SQL not formed correctly!");
			e.printStackTrace();
		}
		
	}

	public void windowClosed(){
		boolean renderMap = false;
		boolean stlOutput = false;
		if(Popup.confirm("Render?", "CityGen")){
			renderMap = true;
		}
		if(Popup.confirm("Save STL file?", "CityGen")){
			stlOutput = true;
		}
		if(renderMap){
			MapRender.render(this,"render");
		}
		if(stlOutput){
			fb.saveSTL();
		}
		log.save("/log-"+System.currentTimeMillis()+".log");
	}
}


