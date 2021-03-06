package com.wolfd.citygen.city;

import java.sql.SQLException;
import java.util.Random;

import com.wolfd.citygen.city.block.CityBlock;
import com.wolfd.citygen.city.buildings.FakeBuildings;
import com.wolfd.citygen.city.map.Population;
import com.wolfd.citygen.city.map.Terrain;
import com.wolfd.citygen.gui.CityView;
import com.wolfd.citygen.util.Database;
import com.wolfd.citygen.util.Log;
import com.wolfd.citygen.util.MapRender;
import com.wolfd.citygen.util.OBJ;
import com.wolfd.citygen.util.Popup;

public class City {
	private static final boolean singleObjFile = false;
	//dimensions
	public int sizeX;
	public int sizeY;
	//maps
	//public Water water;
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
		//water = new Water(this);
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
			rm.cv = cv;
			rm.generate();
			cv.close();
		}else{
			rm.generate();
		}
		log.log("Done generating roads");
		bm.getBlocks(rm);
		log.log("Done generating city blocks");
		fb.generate();
		log.log("Done generating mock buildings");
//		new Thread(new Runnable(){
//			public void run(){
//				bm.save("data/blocks.txt", "data/lots.txt");
//			}
//		}).start();
		
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
		boolean objOutput = false;
		
		if(Popup.confirm("Render?", "CityGen")){
			renderMap = true;
		}
//		if(Popup.confirm("Save STL file?", "CityGen")){
//			stlOutput = true;
//		}
		if(Popup.confirm("Save OBJ file?", "CityGen")){
			objOutput = true;
		}
		if(renderMap){
			MapRender.render(this, "render");
		}
//		if(stlOutput){
//			fb.saveSTL();
//		}
		if(objOutput){
			if(singleObjFile){
				OBJ obj = new OBJ(false);
				for(int i=0; i<fb.buildings.size(); i++){
					fb.buildings.get(i).asOBJ(obj);
				}
				
				rm.asOBJ(obj);
				
				for(int i=0; i<bm.blocks.size(); i++){
					CityBlock cb = bm.blocks.get(i);
					for(int j=0; j<cb.lots.size(); j++){
						cb.lots.get(j).asOBJ(obj);
					}
				}
				
				obj.save("data/city.obj");
			}else{
				OBJ obj = new OBJ(false);
				for(int i=0; i<fb.buildings.size(); i++){
					fb.buildings.get(i).asOBJ(obj);
				}

				obj.save("data/city.obj");

				OBJ objRoads = new OBJ(false);
				rm.asOBJ(objRoads);

				objRoads.save("data/roads.obj");

				OBJ objLots = new OBJ(false);
				for(int i=0; i<bm.blocks.size(); i++){
					CityBlock cb = bm.blocks.get(i);
					for(int j=0; j<cb.lots.size(); j++){
						cb.lots.get(j).asOBJ(objLots);
					}
				}

				objLots.save("data/lots.obj");
				
				OBJ objTerrain = new OBJ(false);
				ter.asOBJ(objTerrain);

				objTerrain.save("data/ter.obj");
			}
		}
		log.save("/log-"+System.currentTimeMillis()+".log");
	}
}


