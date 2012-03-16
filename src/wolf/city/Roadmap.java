package wolf.city;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.configuration.AbstractFileConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.algorithm.LineIntersector;
import com.vividsolutions.jts.algorithm.RobustLineIntersector;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import wolf.city.road.GridSpace;
import wolf.city.road.Intersection;
import wolf.city.road.Road;
import wolf.city.road.RoadGrid;
import wolf.city.road.RoadQueue;
import wolf.city.road.RoadType;
import wolf.city.road.rules.Basic;
import wolf.city.road.rules.Direction;
import wolf.city.road.rules.Grid;
import wolf.city.road.rules.OffRamp;
import wolf.gui.CityView;
import wolf.util.Log;
import wolf.util.RandomHelper;
import wolf.util.Turtle;

public class Roadmap extends Thread{
	public volatile List<Road> roads = new LinkedList<Road>();
	private City city;
	private Configuration config;
	LineIntersector li;
	private RoadGrid grid;
	private Log log;

	private float minimumPopulationHighwayIntersection; //load all parameters from generation properties file
	private int populationSampleRadiusHighwayIntersection;
	private int noWaterSampleRadius;
	public float noWaterCutoffDensity;
	private int bridgeMaxLength;
	private int bridgeTests;
	private int bridgePopulationCheckRadius;
	private float bridgeMinimumPopulation;
	private int intersectionMaxConnections;
	private double seedHighwayAngleSize;
	private float minimumPopulation;
	private boolean seedAtCenter;
	private int highwayCount;
	private float minimumPopulationMainRoad;
	private int populationSampleRadiusMainRoad;
	private int popTests;
	private int waterTests;
	private double maximumRatioIntersectionArea;
	private double minimumIntersectionAngle;
	private double minimumRoadLength;


	public Roadmap(City city){
		log = city.log;
		this.city = city;
		//Load configuration file
		try {
			File configFile = new File("config/roadmapConfig.properties");
			File parent = configFile.getParentFile();
			if(!parent.exists() && !parent.mkdirs()){
				//bad
			}
			if(!configFile.exists()){
				configFile.createNewFile();
			}
			config = new PropertiesConfiguration(configFile);
		} catch (ConfigurationException e) {

			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		minimumPopulationHighwayIntersection = config.getFloat("minimumPopulationHighwayIntersection", .3f); //load all parameters from generation properties file
		populationSampleRadiusHighwayIntersection = config.getInt("populationSampleRadiusHighwayIntersection", 5);
		noWaterSampleRadius = config.getInt("noWaterSampleRadius", 3);
		noWaterCutoffDensity = config.getFloat("noWaterCutoffDensity", .9f); //.7 old value
		bridgeMaxLength = config.getInt("bridgeMaxLength", 350);
		bridgeTests = config.getInt("bridgeTests", 16);
		bridgePopulationCheckRadius = config.getInt("bridgePopulationCheckRadius", 5);
		bridgeMinimumPopulation = config.getFloat("bridgeMinimumPopulation", .4f);
		intersectionMaxConnections = config.getInt("intersectionMaxConnections", 5);
		seedHighwayAngleSize = config.getDouble("seedHighwayAngleSize", 35);
		minimumPopulation = config.getFloat("minimumPopulation", .2f);
		seedAtCenter = config.getBoolean("seedAtCenter", false);
		highwayCount = config.getInt("highwayCount", 4);
		minimumPopulationMainRoad = config.getFloat("minimumPopulationMainRoad", .35f);
		populationSampleRadiusMainRoad = config.getInt("populationSampleRadiusMainRoad", 5);
		popTests = config.getInt("popTests", 8);
		waterTests = config.getInt("waterTests", 8);
		maximumRatioIntersectionArea = config.getDouble("maximumRatioIntersectionArea", .1);
		minimumIntersectionAngle = config.getDouble("minimumIntersectionAngle", 25d);
		minimumRoadLength = config.getDouble("minimumRoadLength", 6d);

		try {
			((AbstractFileConfiguration) config).save();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		li = new RobustLineIntersector();
		grid = new RoadGrid(city.sizeX, city.sizeY);
	}


	public void run(){ //multithreading?
		generate();
	}

	//code to generate roads
	public void generate(){
		generate(null);
	}

	public void generate(CityView cv){
		log.log("Starting roadmap generation");
		RoadQueue rqH = new RoadQueue(); //highways
		RoadQueue rqM = new RoadQueue(); //main roads
		RoadQueue rq = new RoadQueue(); //streets


		//seed map with a couple highways
		seedRoadMap(rqH);

		//generate highways entirely
		Basic basicRule = new Basic();
		Grid gridRule = new Grid();
		OffRamp rampRule = new OffRamp();
		log.log("Highways generating");
		while(rqH.isNotEmpty()){
			//generate highways, save street seeds to rq
			Road road = rqH.remove();
			if(city.pop.getCircleAvg((int)road.b.pos.x, (int)road.b.pos.y, populationSampleRadiusMainRoad) >= minimumPopulationMainRoad){
				//seed a main road (these go from highway to highway)
				Road r;
				if(city.random.nextBoolean()){
					r = basicRule.globalGoals(city, road, Direction.LEFT);
				}else{
					r = basicRule.globalGoals(city, road, Direction.RIGHT);
				}
				r.setType(RoadType.MAIN);
				rqM.add(localConstraints(r));

			}else if(city.pop.getCircleAvg((int)road.b.pos.x, (int)road.b.pos.y, populationSampleRadiusHighwayIntersection) >= minimumPopulationHighwayIntersection){
				//determine if this location should have an intersection (or should all locations have an intersection and then prune later?)

				//yes, have an intersection! Free, with your purchase!
				rq.add(localConstraints(rampRule.globalGoals(city, road, Direction.LEFT)));
				rq.add(localConstraints(rampRule.globalGoals(city, road, Direction.RIGHT)));
			}
			rqH.add(localConstraints(basicRule.globalGoals(city, road, Direction.FORWARD)));
			//city.pop.removeDensityLine(road);
			roads.add(connect(road));
			grid.add(road); //for collision detection
			if(cv != null){
				cv.draw();
			}
		}
		city.pop.reset();


		//setup for main roads
		basicRule.turnRateForward = 40;
		log.log("Main roads generating");
		while(rqM.isNotEmpty()){
			Road road = rqM.remove();
			Road r = basicRule.globalGoals(city, road, Direction.FORWARD);
			r.setType(RoadType.MAIN);
			if(city.pop.getCircleAvg((int)road.b.pos.x, (int)road.b.pos.y, populationSampleRadiusHighwayIntersection) >= minimumPopulationHighwayIntersection){
				//determine if this location should have an intersection (or should all locations have an intersection and then prune later?)

				//yes, have an intersection! Free, with your purchase!
				rq.add(localConstraints(gridRule.globalGoals(city, road, Direction.LEFT)));
				rq.add(localConstraints(gridRule.globalGoals(city, road, Direction.RIGHT)));
			}
			rqM.add(localConstraints(r));
			//city.pop.removeDensityLine(road);
			roads.add(road);
			grid.add(road); //for collision detection
			if(cv != null){
				cv.draw();
			}
		}
		city.pop.reset();
		//generate streets entirely

		log.log("Streets generating");
		rq.stackStyle = true;
		while(rq.isNotEmpty()){
			//generate streets
			if(city.random.nextDouble()>.9){
				rq.stackStyle = false;
			}else{
				rq.stackStyle = true;
			}
			Road road = rq.remove();
			if(road != null){
				//use grid pattern to fill in areas between highways (Manhattan-esque pattern, but not perfect)
				if(city.pop.get((int)road.b.pos.x, (int)road.b.pos.y)>minimumPopulation){
					rq.add(localConstraints(gridRule.globalGoals(city, road, Direction.BACKWARD)));
					rq.add(localConstraints(gridRule.globalGoals(city, road, Direction.FORWARD)));
					rq.add(localConstraints(gridRule.globalGoals(city, road, Direction.LEFT)));
					rq.add(localConstraints(gridRule.globalGoals(city, road, Direction.RIGHT)));
				}
				//city.pop.removeDensityLine(road);
				roads.add(road);
				grid.add(road); //for collision detection
			}
			if(cv != null){
				cv.draw();
				waitInMs(100);
			}
		}
		city.pop.reset();
		//prune unnecessary roads (?)
		log.log("Roads: "+roads.size());
	}


	@SuppressWarnings("unused")
	private void waitInMs(int ms) {
		if(false){
			long time = System.currentTimeMillis();
			while(System.currentTimeMillis()<time+ms){
			}
		}
	}

	private Road connect(Road road) {
		road.a.addConnecting(road.b); //connect intersections
		road.b.addConnecting(road.a);
		return road;
	}


	private void seedRoadMap(RoadQueue roadQueue) {
		//seed at center (not such a great thing)
		if(seedAtCenter){
			float x, y;
			while(true){ //keep these variables in check
				x = (float) ((this.city.random.nextFloat()-.5)*city.sizeX); //can place in center half of city
				y = (float) ((this.city.random.nextFloat()-.5)*city.sizeY);
				if(city.water.getCircleAvg((int)x, (int)y, noWaterSampleRadius) < noWaterCutoffDensity){
					break;
				}
			}
			Coordinate startPoint = new Coordinate(x,y);
			double angle = Math.toDegrees(Angle.angle(startPoint, new Coordinate(0,0)));
			Turtle t = new Turtle(startPoint, angle);
			//t.turn(45);
			t.move(32);
			roadQueue.add(new Road(new Intersection(startPoint), new Intersection(t.pos), RoadType.HIGHWAY));
		}
		for(int i=0; i<highwayCount; i++){
			//highway from random side
			int direction = Math.abs(city.random.nextInt()%4);
			float length = 128;
			switch(direction){
			case 0:{ //North
				log.log("Highway from the north");
				float x = (float) ((this.city.random.nextFloat()-.5)*city.sizeX); //can place in center half of city
				Coordinate startPoint = new Coordinate(x, city.sizeY/2);
				Turtle t = new Turtle(startPoint, ((city.random.nextDouble()*180)%seedHighwayAngleSize)+270);
				t.move(length);
				roadQueue.add(new Road(new Intersection(startPoint), new Intersection(t.pos), RoadType.HIGHWAY));
				break;
			}
			case 1:{ //South
				log.log("Highway from the south");
				float x = (float) ((this.city.random.nextFloat()-.5)*city.sizeX); //can place in center half of city
				Coordinate startPoint = new Coordinate(x, -city.sizeY/2);
				Turtle t = new Turtle(startPoint, ((city.random.nextDouble()*180)%seedHighwayAngleSize)+90);
				t.move(length);
				roadQueue.add(new Road(new Intersection(startPoint), new Intersection(t.pos), RoadType.HIGHWAY));
				break;
			}
			case 2:{ //East
				log.log("Highway from the east");
				float y = (float) ((this.city.random.nextFloat()-.5)*city.sizeY);
				Coordinate startPoint = new Coordinate(-city.sizeX/2, y);
				Turtle t = new Turtle(startPoint, ((city.random.nextDouble()*180)%seedHighwayAngleSize)+0);
				t.move(length);
				roadQueue.add(new Road(new Intersection(startPoint), new Intersection(t.pos), RoadType.HIGHWAY));
				break;
			}
			case 3:{ //West
				log.log("Highway from the west");
				float y = (float) ((this.city.random.nextFloat()-.5)*city.sizeY);
				Coordinate startPoint = new Coordinate(city.sizeX/2, y);
				Turtle t = new Turtle(startPoint, ((city.random.nextDouble()*180)%seedHighwayAngleSize)+180);
				t.move(length);
				roadQueue.add(new Road(new Intersection(startPoint), new Intersection(t.pos), RoadType.HIGHWAY));
				break;
			}
			default:{
				System.out.println("You done goofed. Check the seedRoadMap function's direction variable");
			}
			}
		}

	}

	private Road localConstraints(Road r){
		//cheap tests
		r = lengthCheck(r);
		r = maxConnections(r);
		r = inBounds(r);

		r = intersectionAngleCheck(r);
		//expensive tests
		r = waterCheck(r);
		r = proximityCheck(r);
		r = trimToIntersection(r);
		r = lengthCheck(r); //fixes from trim
		r = popCheck(r);

		return r;
	}


	private Road intersectionAngleCheck(Road r) {
		if(r==null){
			return null;
		}
		Geometry g0 = r.getGeometry(2);
		double angle = Angle.angle(r.a.pos, r.b.pos);

		ArrayList<GridSpace> spaces = grid.getSpaces(r);
		ArrayList<Road> tested = new ArrayList<Road>();
		for(GridSpace g: spaces){
			LinkedList<Road> roads = grid.get(g);
			for(Road i: roads){
				if(!tested.contains(i)){
					Geometry g1 = i.getGeometry(2);
					if(g0.intersects(g1) || g0.distance(g1)<4){
						double difference = Math.toDegrees(Math.abs(Angle.angle(i.a.pos, i.b.pos)-angle))%90;
						log.log("Angle:"+difference);
						if(difference < minimumIntersectionAngle){
							log.log("Road removed due to intersectionAngleCheck  :  "+r.toString());
							return null;
						}
					}
				}
			}
		}

		return r;
	}

	private Road proximityCheck(Road r) {
		if(r==null){
			return null;
		}
		int expand = 2;
		Geometry a = r.getGeometry(expand);
		//check related spaces in grid
		ArrayList<GridSpace> spaces = grid.getSpaces(r);
		ArrayList<Road> tested = new ArrayList<Road>();
		for(GridSpace g: spaces){
			LinkedList<Road> roads = grid.get(g);
			for(Road i: roads){
				if(!tested.contains(i)){
					Geometry b = i.getGeometry(expand);
					if(a.intersects(b)){
						Geometry c = a.intersection(b);
						if(c.getArea()>maximumRatioIntersectionArea*a.getArea()){
							log.log("Road removed due to proximityCheck  :  "+r.toString());
							return null;
						}
						if(c.getArea()>maximumRatioIntersectionArea*b.getArea()){
							log.log("Road removed due to proximityCheck  :  "+r.toString());
							return null;
						}
					}
				}
			}
		}
		return r;
	}


	private Road lengthCheck(Road r) {
		if(r==null){
			return null;
		}
		double distance = r.a.pos.distance(r.b.pos);
		if(distance>minimumRoadLength){ //6 blocks is minimum road length
			return r;
		}

		return null;
	}


	private Road inBounds(Road r) {
		if(r==null){
			return null;
		}

		boolean aInside = (r.a.pos.x > -city.sizeX/2 && r.a.pos.x <= city.sizeX/2 && r.a.pos.y > -city.sizeY/2 && r.a.pos.y <= city.sizeY/2);
		boolean bInside = (r.b.pos.x > -city.sizeX/2 && r.b.pos.x <= city.sizeX/2 && r.b.pos.y > -city.sizeY/2 && r.b.pos.y <= city.sizeY/2);
		if(!aInside || !bInside){
			return null;
		}

		return r;
	}


	private Road trimToIntersection(Road r) {
		if(r==null){
			return null;
		}
		for(int i=0; i<roads.size(); i++){
			Road road = roads.get(i);
			li.computeIntersection(r.a.pos, r.b.pos, road.a.pos, road.b.pos);
			if(li.hasIntersection()){
				Coordinate intersection = li.getIntersection(0);

				r.b.pos = intersection;
				return r;
			}
		}
		return r;
	}

	private Road popCheck(Road r) {
		if(r==null){
			return null;
		}
		for(int i=0; i<popTests; i++){
			double xD = r.a.pos.x - r.b.pos.x;
			double yD = r.a.pos.y - r.b.pos.y;

			double x = (xD/(popTests+(popTests/10)))*(i+(popTests/10));
			double y = (yD/(popTests+(popTests/10)))*(i+(popTests/10));
			if(city.pop.get((int)x, (int)y)<=minimumPopulation){
				if(r.getType() != RoadType.HIGHWAY){
					return null;
				}else{
					if(city.pop.get((int)x, (int)y)<=0){ //highways should be able to leave city, but end on edge
						return null;
					}
				}
			}
		}

		return r;
	}

	private Road maxConnections(Road r) {
		if(r==null){
			return null;
		}
		if(r.a.connecting.size()>intersectionMaxConnections){
			r.a.connecting.remove(r);
			r = null;
			return null;
		}
		return r;
	}

	private Road waterCheck(Road r){
		if(r==null){
			return null;
		}
		Coordinate loc = r.b.pos;
		//WATER CHECK
		//is it in water?
		for(int test=0; test<waterTests; test++){
			double xD = r.a.pos.x - r.b.pos.x;
			double yD = r.a.pos.y - r.b.pos.y;

			double x = (xD/waterTests)*test;
			double y = (yD/waterTests)*test;
			float waterAvg = city.water.getCircleAvg((int)x, (int)y, noWaterSampleRadius);
			if(waterAvg >= noWaterCutoffDensity){
				//BRIDGE
				if(r.getType() == RoadType.HIGHWAY || r.getType() == RoadType.BRIDGE){ //technically, it should never be a bridge
					//make a bridge if high enough population density
					float population = city.pop.getCircleAvg((int)loc.x, (int)loc.y, bridgePopulationCheckRadius);

					if(population>=bridgeMinimumPopulation){
						double angle = Math.toDegrees(Angle.angle(r.a.pos,r.b.pos));
						Turtle t = new Turtle(r.a.pos, angle);
						boolean didSucceed = false;


						float minBridgeLength = bridgeMaxLength;
						float bridgeCurLength = bridgeMaxLength+1;
						double bestAngle = 0;
						Coordinate bestBridgePosition = null;
						for(int i=0; i<bridgeTests; i++){
							didSucceed = false;
							float angleMod = RandomHelper.random(city.random.nextFloat(), -30, 30);
							t = new Turtle(r.a.pos, angle+angleMod);
							float curLength = (float) r.a.pos.distance(r.b.pos);
							t.move(curLength);

							for(int j=0;j<bridgeTests;j++){
								t.move((bridgeMaxLength-curLength)/bridgeTests);
								float waterNewAvg = city.water.getCircleAvg((int)t.pos.x, (int)t.pos.y, noWaterSampleRadius);
								//System.out.println("water density: "+waterNewAvg);
								if(waterNewAvg < noWaterCutoffDensity){ //bridge can be built
									didSucceed = true;
									bridgeCurLength = (float) r.a.pos.distance(t.pos);
									//System.out.println("succeeded!");
									break;
								}

							}
							if(didSucceed && bridgeCurLength<minBridgeLength){
								minBridgeLength = bridgeCurLength;
								//System.out.println(bridgeCurLength);
								bestBridgePosition = t.pos;
								bestAngle = t.angle;
							}
						}
						if(didSucceed){
							boolean waterSeen = false;
							Turtle tt = new Turtle(r.a.pos, bestAngle);
							for(int j=0;j<bridgeTests;j++){
								tt.move(minBridgeLength/bridgeTests);
								float water = city.water.get((int)tt.pos.x, (int)tt.pos.y);
								if(water >= noWaterCutoffDensity){
									waterSeen = true;

								}
							}
							if(waterSeen){
								r.setType(RoadType.BRIDGE);
							}else{
								System.out.println("changed");
								r.setType(RoadType.HIGHWAY);
							}
							r.b.pos = bestBridgePosition;
							return r;
						}else{
							return null;
						}

					}

				}
				return null;
			}
		}
		//Road passed WATER CHECK
		return r;
	}
}
