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
import org.lwjgl.util.vector.Vector3f;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.algorithm.LineIntersector;
import com.vividsolutions.jts.algorithm.RobustLineIntersector;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.index.quadtree.Quadtree;

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
import wolf.city.road.rules.RoadRule;
import wolf.gui.CityView;
import wolf.util.Log;
import wolf.util.OBJ;
import wolf.util.OBJOutput;
import wolf.util.RandomHelper;
import wolf.util.Turtle;

public class Roadmap implements OBJOutput{
	
	public Quadtree roads;
	private City city;
	private Configuration config;
	private LineIntersector li;
	private Log log;

	private static final float roadThickness = 5;
	private static final boolean roadToZero = true; //exported .obj file's roads will go to the base, or just have a thickness
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
	//private int highwayCount;
	private float minimumPopulationMainRoad;
	private int populationSampleRadiusMainRoad;
	private int popTests;
	private int waterTests;
	private double maximumRatioIntersectionArea;
	private double minimumIntersectionAngle;
	private double minimumRoadLength;
	private double maximumRoadSnapDistance;
	@SuppressWarnings("unused")
	private double minimumRoadSnapDistance;
	public double chaos;
	//private float minimumPopulationHighway;
	public boolean finished = false;
	public Geometry shape; //generate when final
	private int minimumNumberParents;
	
	public CityView cv;
	
	private RoadQueue rqH; //highways
	private RoadQueue rqM; //main roads
	private RoadQueue rq; //streets
	private GeometryFactory gf = new GeometryFactory();


	public Roadmap(City city){
		log = city.log;
		this.city = city;
		roads = new Quadtree();
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
		noWaterCutoffDensity = config.getFloat("noWaterCutoffDensity", .8f);
		bridgeMaxLength = config.getInt("bridgeMaxLength", 512);
		bridgeTests = config.getInt("bridgeTests", 32);
		bridgePopulationCheckRadius = config.getInt("bridgePopulationCheckRadius", 5);
		bridgeMinimumPopulation = config.getFloat("bridgeMinimumPopulation", .4f);
		intersectionMaxConnections = config.getInt("intersectionMaxConnections", 5);
		seedHighwayAngleSize = config.getDouble("seedHighwayAngleSize", 15);
		minimumPopulation = config.getFloat("minimumPopulation", .17f);
		//minimumPopulationHighway = config.getFloat("minimumPopulationHighway", .3f);
		seedAtCenter = config.getBoolean("seedAtCenter", false);
		minimumPopulationMainRoad = config.getFloat("minimumPopulationMainRoad", .40f);
		populationSampleRadiusMainRoad = config.getInt("populationSampleRadiusMainRoad", 5);
		popTests = config.getInt("popTests", 8);
		waterTests = config.getInt("waterTests", 8);
		maximumRatioIntersectionArea = config.getDouble("maximumRatioIntersectionArea", .1);
		minimumIntersectionAngle = config.getDouble("minimumIntersectionAngle", 35d); //25
		minimumRoadLength = config.getDouble("minimumRoadLength", 5d);
		maximumRoadSnapDistance = config.getDouble("maximumRoadSnapDistance", 40d);
		minimumRoadSnapDistance = config.getDouble("minimumRoadSnapDistance", 2d);
		minimumNumberParents = config.getInt("minimumNumberParents", 8);
		chaos = config.getDouble("chaos", .5);

		try {
			((AbstractFileConfiguration) config).save();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}

		li = new RobustLineIntersector();
	}

	
	public void setupGeneration(){
		log.log("Starting roadmap generation");
		rqH = new RoadQueue(); //highways
		rqM = new RoadQueue(); //main roads
		rq = new RoadQueue(); //streets

		//seed map with a couple highways
		seedRoadMap(rqH);

		//generate highways entirely

		//log.log("Highways generating");
	}
	
	public boolean generateIteration(CityView cv){
		
		if(!rqH.isEmpty()){
			//generate highways, save street seeds to rq
			Road road = rqH.remove();
			if(road.finished){
				//finished
			}else{
				if(city.pop.getCircleAvg((int)road.b.pos.x, (int)road.b.pos.y, populationSampleRadiusMainRoad) >= minimumPopulationMainRoad){
					//seed a main road (these go from highway to highway)
					Road r;
					if(city.random.nextBoolean()){
						r = road.rule.globalGoals(city, road, Direction.LEFT);
					}else{
						r = road.rule.globalGoals(city, road, Direction.RIGHT);
					}
					r.setType(RoadType.MAIN);
					rqM.add(localConstraints(r));

				}else if(city.pop.getCircleAvg((int)road.b.pos.x, (int)road.b.pos.y, populationSampleRadiusHighwayIntersection) >= minimumPopulationHighwayIntersection){
					//determine if this location should have an intersection (or should all locations have an intersection and then prune later?)

					OffRamp rampRule = new OffRamp(city);
					//yes, have an intersection! Free, with your purchase!
					rq.add(localConstraints(rampRule.globalGoals(city, road, Direction.LEFT)));
					rq.add(localConstraints(rampRule.globalGoals(city, road, Direction.RIGHT)));
				}
				if(road.getType() == RoadType.HIGHWAY){
					Basic basicRule = new Basic(city);
					Road newRoad = basicRule.globalGoals(city, road, Direction.FORWARD);
					rqH.add(localConstraints(newRoad));
				}else{
					rqM.add(road);
				}
				
			}
			road = connect(road);
			roads.insert(road.getEnvelope(), road);
			cv.roads.add(road);
			if(cv != null){
				cv.draw();
			}
		}


		//setup for main roads
		//basicRule.turnRateForward = 40;
		//log.log("Main roads generating");
		rqM.stackStyle = true;
		if(!rqM.isEmpty()){
			Road road = rqM.remove();
			if(road.finished){
				//finished
			}else{
				Road r = road.rule.globalGoals(city, road, Direction.FORWARD);
				r.setType(RoadType.MAIN);
				//have an intersection! Free, with your purchase!
				Road inters1 = r.rule.globalGoals(city, road, Direction.LEFT);
				Road inters2 = r.rule.globalGoals(city, road, Direction.RIGHT);
				//set the type to street, give it a proper rule
				inters1.setType(RoadType.STREET);
				inters1.rule = new Grid(city);
				inters2.setType(RoadType.STREET);
				inters2.rule = new Grid(city);

				rq.add(inters1);
				rq.add(inters2);
				rqM.add(localConstraints(r));
			}
			
			road = connect(road);
			roads.insert(road.getEnvelope(), road);
			cv.roads.add(road);
			
			if(cv != null){
				cv.draw();
			}
		}
		//generate streets entirely

		//log.log("Streets generating");
		rq.stackStyle = true;
		if(!rq.isEmpty()){
			
			//generate streets
			if(city.random.nextDouble()>.99){
				rq.stackStyle = false;
			}else{
				rq.stackStyle = true;
			}
			Road road = localConstraints(rq.remove());
			if(road != null){
				if(road.finished){
					//finished
				}else{
					if(road.rule instanceof OffRamp){
						road.rule = new Grid(city);
					}
					if(city.random.nextDouble()>.9){ //makes it look like a modern/whatever neighborhood
						road.rule = road.rule.mutate();
					}
					//use grid pattern to fill in areas between highways (Manhattan-esque pattern, but not perfect)
					if(city.pop.get((int)road.b.pos.x, (int)road.b.pos.y)>minimumPopulation){
						rq.add(localConstraints(road.rule.globalGoals(city, road, Direction.BACKWARD)));
						rq.add(localConstraints(road.rule.globalGoals(city, road, Direction.FORWARD)));
						rq.add(localConstraints(road.rule.globalGoals(city, road, Direction.LEFT)));
						rq.add(localConstraints(road.rule.globalGoals(city, road, Direction.RIGHT)));
					}
					//city.pop.removeDensityLine(road);
				}
				road = connect(road);
				roads.insert(road.getEnvelope(), road);
				cv.roads.add(road);

			}
			if(cv != null){
				cv.draw();
			}
		}
		if(rq.isEmpty() && rqH.isEmpty() && rqM.isEmpty()){
			return false;
		}else return true;
	}
	
	public void generate(){
		setupGeneration();

		while(generateIteration(cv));
		
		//		{//trim roads with not enough 'parents'
		//			for(int i=0; i<roads.size(); i++){
		//				if(roads.get(i).numberParents<minimumNumberParents){
		//					roads.remove(i);
		//				}
		//			}
		//		}
		//intersection fix
//		for(int i=0; i<roads.size(); i++){
//			Road a = roads.get(i);
//			for(int j=0; j<roads.size(); j++){
//				Road b = roads.get(j);
//				LineSegment extendedLine = a.getLineSegment();
//				double length = extendedLine.getLength();
//				extendedLine.pointAlong(length*1.1);
//				Coordinate c = extendedLine.intersection(b.getLineSegment()); //problem is that if they touch at all, it will intersect
//				if(c != null && c.distance(a.a.pos)>floatingPointError && c.distance(a.b.pos)>floatingPointError && c.distance(b.a.pos)>floatingPointError && c.distance(b.b.pos)>floatingPointError){
//					{
//						Intersection end = a.b;
//						Intersection mid = new Intersection(c);
//						a.b = mid;
//						Road r = new Road(a);
//						r.a = mid;
//						r.b = end;
//						roads.add(r);
//					}
//					{
//						Intersection end = b.b;
//						Intersection mid = new Intersection(c);
//						b.b = mid;
//						Road r = new Road(b);
//						r.a = mid;
//						r.b = end;
//						roads.add(r);
//					}
//				}
//			}
//			if(cv != null){
//				cv.draw();
//			}
//		}
		log.log("Roads: "+roads.size());
		{//union all of the road geometries
			Geometry[] geoms = new Geometry[roads.size()];
			ArrayList<Road> roadList = (ArrayList<Road>) roads.queryAll();
			for(int i=0; i<roadList.size(); i++){
				geoms[i] = (roadList.get(i).getFinalGeometry());
			}
			GeometryFactory gf = new GeometryFactory();
			GeometryCollection polygonCollection = gf.createGeometryCollection(geoms);
			//union
			shape = polygonCollection.buffer(0);
		}
		finished = true;
	}


	private Road connect(Road road) {
		if(road.intersectedRoad != null){
			Road split = new Road(road.b, road.intersectedRoad.b, road.intersectedRoad.getType(), road.intersectedRoad.rule);
			road.intersectedRoad.b = road.b;
			split.a.addConnecting(road);
			roads.insert(split.getEnvelope(), split);
			cv.roads.add(split);
		}
		road.a.addConnecting(road); //connect intersections
		road.b.addConnecting(road);
		return road;
	}


	private void seedRoadMap(RoadQueue roadQueue) {
		Basic basicRule = new Basic(city);
		//seed at center (not such a great thing)
		if(seedAtCenter){
			float x, y;
			while(true){ //keep these variables in check
				x = (float) ((this.city.random.nextFloat()-.5)*city.sizeX); //can place in center half of city
				y = (float) ((this.city.random.nextFloat()-.5)*city.sizeY);
				/*if(city.water.getCircleAvg((int)x, (int)y, noWaterSampleRadius) < noWaterCutoffDensity){
					break;
				}*/
				break;
			}
			Coordinate startPoint = new Coordinate(x,y);
			double angle = Math.toDegrees(Angle.angle(startPoint, new Coordinate(0,0)));
			Turtle t = new Turtle(startPoint, angle);
			//t.turn(45);
			t.move(32);
			roadQueue.add(new Road(new Intersection(startPoint), new Intersection(t.pos), RoadType.HIGHWAY, basicRule));
		}
		int highwayCount = Math.max((city.sizeX*2+city.sizeY*2)/(3*1024),1);
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
				roadQueue.add(new Road(new Intersection(startPoint), new Intersection(t.pos), RoadType.HIGHWAY, basicRule));
				break;
			}
			case 1:{ //South
				log.log("Highway from the south");
				float x = (float) ((this.city.random.nextFloat()-.5)*city.sizeX); //can place in center half of city
				Coordinate startPoint = new Coordinate(x, -city.sizeY/2);
				Turtle t = new Turtle(startPoint, ((city.random.nextDouble()*180)%seedHighwayAngleSize)+90);
				t.move(length);
				roadQueue.add(new Road(new Intersection(startPoint), new Intersection(t.pos), RoadType.HIGHWAY, basicRule));
				break;
			}
			case 2:{ //East
				log.log("Highway from the east");
				float y = (float) ((this.city.random.nextFloat()-.5)*city.sizeY);
				Coordinate startPoint = new Coordinate(-city.sizeX/2, y);
				Turtle t = new Turtle(startPoint, ((city.random.nextDouble()*180)%seedHighwayAngleSize)+0);
				t.move(length);
				roadQueue.add(new Road(new Intersection(startPoint), new Intersection(t.pos), RoadType.HIGHWAY, basicRule));
				break;
			}
			case 3:{ //West
				log.log("Highway from the west");
				float y = (float) ((this.city.random.nextFloat()-.5)*city.sizeY);
				Coordinate startPoint = new Coordinate(city.sizeX/2, y);
				Turtle t = new Turtle(startPoint, ((city.random.nextDouble()*180)%seedHighwayAngleSize)+180);
				t.move(length);
				roadQueue.add(new Road(new Intersection(startPoint), new Intersection(t.pos), RoadType.HIGHWAY, basicRule));
				break;
			}
			default:{
				System.out.println("You done goofed. Check the seedRoadMap function's direction variable");
			}
			}
		}

	}

	public Road localConstraints(Road r){
		//cheap tests
		r = lengthCheck(r);
		r = maxConnections(r);
		r = inBounds(r);

		r = snapToIntersection(r);
		r = intersectionAngleCheck(r);
		//expensive tests
		//r = waterCheck(r);
		r = proximityCheck(r);
		r = trimToIntersection(r);
		r = lengthCheck(r); //fixes from trim
		r = popCheck(r);
		return r;
	}





	private Road snapToIntersection(Road r) {
		if(r==null){
			return null;
		}

		Coordinate point =  r.b.pos;
		ArrayList<Road> roadsLoc = (ArrayList<Road>) roads.query(r.getEnvelope());
		for(int ir=0; ir<roadsLoc.size(); ir++){
			Road i = roadsLoc.get(ir);

			if(i.getType() == RoadType.HIGHWAY && r.getType() == RoadType.STREET){

			}else{
				//doesn't find closest, just finds one and goes with it.
				double distA = r.b.pos.distance(i.a.pos);
				double distB = r.b.pos.distance(i.b.pos);
				if(distA<maximumRoadSnapDistance && distB<maximumRoadSnapDistance){
					if(distA<distB){
						r.b = i.a;
						return r;
					}else{
						r.b = i.b;
						return r;
					}
				}else{
					if(distA<maximumRoadSnapDistance){
						r.b = i.a;
						return r;
					}
					if(distB<maximumRoadSnapDistance){
						r.b = i.b;
						return r;
					}
				}

				//snap to road
				Coordinate closest = i.getLineSegment().closestPoint(point);
				double dist = point.distance(closest);
				if(dist < maximumRoadSnapDistance ){//&& dist > minimumRoadSnapDistance){
					r.b.pos = closest;
					r.intersectedRoad = i;
					return r;
				}
			}
		}

		return r;
	}


	private Road intersectionAngleCheck(Road r) {
		if(r==null){
			return null;
		}

		Geometry g0 = r.getGeometry(2).difference(r.getIntersectionGeometry());
		double angle = Angle.angle(r.a.pos, r.b.pos);
		//double dist = (Math.pow(r.a.pos.x,2)+Math.pow(r.a.pos.y,2));
		//double normX = r.a.pos.x/dist;
		//double normY = r.a.pos.y/dist;
		//double angle = Math.atan2(r.a.pos.x, r.a.pos.y);

		if(angle<0){
			angle = Math.PI + angle; //half circle
		}

		ArrayList<Road> roadList = (ArrayList<Road>) roads.query(r.getEnvelope());
		for(int i=0; i<roadList.size(); i++){
			Road road = roadList.get(i);
			Geometry g1 = road.getGeometry(2).difference(road.getIntersectionGeometry());
			if(g0.intersects(g1) || g0.distance(g1)<4){
				double thisAngle = Angle.angle(road.a.pos, road.b.pos);
				if(thisAngle<0){
					thisAngle = Math.PI + thisAngle; //half circle
				}
				double bigAngle = Math.max(thisAngle, angle);
				double smallAngle = Math.min(thisAngle, angle);

				double difference = Math.toDegrees(bigAngle - smallAngle);

				if(difference<minimumIntersectionAngle || difference>(360-minimumIntersectionAngle)){
					return null;
				}
			}
		}


		return r;
	}

	private Road proximityCheck(Road r) {
		if(r==null){
			return null;
		}
		double expand = r.width*1f;
		Geometry a = r.getGeometry(expand);
		//check related spaces in grid
		ArrayList<Road> roadList = (ArrayList<Road>) roads.query(r.getEnvelope());
		for(int i=0; i<roadList.size(); i++){
			Road ir = roadList.get(i);
			if(ir.getType() != RoadType.HIGHWAY && ir.getType() != RoadType.MAIN){
				//if(!tested.contains(i) && !((i.getType() == RoadType.HIGHWAY || i.getType() == RoadType.MAIN) && (r.getType() == RoadType.STREET || r.getType() == RoadType.HIGHWAY)) /*streets ignore main and highway types*/){
				Geometry b = ir.getGeometry(expand);
				if(a.intersects(b)){
					Geometry c = a.intersection(b);
					if(c.getArea()>maximumRatioIntersectionArea*a.getArea()){
						return null;
					}
					if(c.getArea()>maximumRatioIntersectionArea*b.getArea()){
						return null;
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

		if(distance>r.width*1.5){ //minimum road length
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
			r.finished = true;
			return r;
		}

		return r;
	}


	private Road trimToIntersection(Road r) {
		if(r==null){
			return null;
		}

		if(r.getType() == RoadType.MAIN || r.getType() == RoadType.HIGHWAY){
			ArrayList<Road> roadList = (ArrayList<Road>) roads.query(r.getEnvelope());
			for(int i=0; i<roadList.size(); i++){
				Road road = roadList.get(i);

				li.computeIntersection(r.a.pos, r.b.pos, road.a.pos, road.b.pos);
				if(li.hasIntersection()){
					Coordinate intersection = li.getIntersection(0);
					r.intersectedRoad = road;
					r.b.pos = intersection;
				}
			}
		}
		//			if(splitRoad != null){
		//				Intersection b = splitRoad.b;
		//				splitRoad.b = new Intersection(r.b.pos);
		//				//splitRoad.b.addConnecting(r); //may not be best idea
		//				Road split = new Road(splitRoad.b, b, splitRoad.getType(), splitRoad.rule);
		//				roads.add(connect(split));
		//				grid.add(split); //for collision detection
		//			}
		return r;
	}

	private Road popCheck(Road r) {
		if(r==null){
			return null;
		}
		for(int i=0; i<popTests; i++){
			double xD = r.a.pos.x - r.b.pos.x;
			double yD = r.a.pos.y - r.b.pos.y;

			double x = (xD/(popTests+(popTests/10d)))*(i+(popTests/10d));
			double y = (yD/(popTests+(popTests/10)))*(i+(popTests/10));
			if(r.getType() == RoadType.HIGHWAY){
				//if(city.pop.get((int)x, (int)y)<=minimumPopulationHighway){
				//	r.setType(RoadType.MAIN); //no longer a highway, but a main road still;
				//}
			}
			if(city.pop.get((int)(x+r.b.pos.x), (int)(y+r.b.pos.y))<=minimumPopulation){
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

	@Override
	public void asOBJ(OBJ obj) {
		obj.startObject("roads");
		
		for(int i=0; i<Intersection.intersections.size(); i++){
			Intersection is = Intersection.intersections.get(i);
			if(is.connecting.size() > 1){ //if there is more than one road to make an intersection out of
				ArrayList<LineSegment> segments0 = new ArrayList<LineSegment>();
				ArrayList<LineSegment> segments1 = new ArrayList<LineSegment>();
				//compute intersection points and max radius of roads (minimum value for roadExtrusion value)
				double maxRadius = 0;
				for(int j=0; j<is.connecting.size(); j++){
					Road r = is.connecting.get(j);
					double radius = r.width/2;
					if(radius>maxRadius){
						maxRadius = radius;
					}
					//calculate intersection lines
					Turtle t0;
					if(r.a.pos.equals(is.pos)){
						t0 = new Turtle(is.pos, Math.toDegrees(Angle.angle(is.pos, r.b.pos)));
					}else{
						t0 = new Turtle(is.pos, Math.toDegrees(Angle.angle(is.pos, r.a.pos)));
					}
					Turtle t1 = new Turtle(t0.pos, t0.angle);
					//move out to sides of roads
					t0.turn(90);
					t1.turn(-90);
					t0.move(radius);
					t1.move(radius);
					Coordinate t0Start = new Coordinate(t0.pos);
					Coordinate t1Start = new Coordinate(t1.pos);
					//return to first orientation
					t0.turn(-90);
					t1.turn(90);
					t0.move(r.width*6);
					t1.move(r.width*6);
					
					LineSegment ls0 = new LineSegment(t0Start, t0.pos);
					LineSegment ls1 = new LineSegment(t1Start, t1.pos);
					segments0.add(ls0);
					segments1.add(ls1);
				}
				//compute intersections of roads extruded from center of intersection.
				double[] extrusion = new double[is.connecting.size()];
				//double maxDistance = -1;
				for(int j=0; j<is.connecting.size(); j++){
					for(int k=0; k<is.connecting.size(); k++){
						if(j != k){
							Coordinate c0 = segments0.get(j).intersection(segments1.get(k));
							Coordinate c1 = segments1.get(j).intersection(segments0.get(k));
							if(c0 != null){
								double dist = c0.distance(is.pos);
								
								if(dist>extrusion[j]){
									extrusion[j] = dist;
									log.log("dist: "+dist+" other");
								}
							}
							
							if(c1 != null){
								double dist = c1.distance(is.pos);
								
								if(dist>extrusion[j]){
									extrusion[j] = dist;
								}
							}
							
						}
					}
					if(extrusion[j] < maxRadius) extrusion[j] = maxRadius;
					for(int k=0; k<is.connecting.size(); k++){
						if(is.connecting.get(k).a == is){
							is.connecting.get(k).roadExtrusionA = extrusion[k];
						}else{
							is.connecting.get(k).roadExtrusionB = extrusion[k];
						}
					}
				}
				//create final road extrusion
				
				LineSegment[] segments = new LineSegment[is.connecting.size()];
				for(int j=0; j<is.connecting.size(); j++){
					Road r = is.connecting.get(j);
					double radius = r.width/2; //maybe a tad faster to load into variable?
					Turtle t0;
					if(r.a.pos.equals(is.pos)){
						t0 = new Turtle(is.pos, Math.toDegrees(Angle.angle(is.pos, r.b.pos)));
					}else{
						t0 = new Turtle(is.pos, Math.toDegrees(Angle.angle(is.pos, r.a.pos)));
					}
					
					//move out from intersection and split into road width
					t0.move(extrusion[j]);
					t0.turn(90);
					Turtle t1 = new Turtle(t0.pos, t0.angle-180);
					t0.move(radius);
					t1.move(radius);
					
					segments[j] = new LineSegment(t0.pos, t1.pos);
				}
				//need to sort segments by angle order, then use that to generate shape. Convex hull will not work.
				
				double[] angles = new double[segments.length]; //no ring, so no last element
				//calculate all point angles
				for(int j=0; j<angles.length; j++) angles[j] = Math.min(Math.PI*2-Angle.angle(is.pos, segments[j].p0),Math.PI*2-Angle.angle(is.pos, segments[j].p1));
				//selection sort
				for(int j=0; j<angles.length; j++){
					int min = j;
					for(int k=j+1; k<angles.length; k++){
						if(angles[k]<angles[min]){
							min = k; //remember new min
						}
					}
					
					//swap min element with current element
					if(min != j){
						//swap angle array
						double ang = angles[j];
						angles[j] = angles[min];
						angles[min] = ang;
						
						//swap segment array
						LineSegment tempS = segments[j];
						segments[j] = segments[min];
						segments[min] = tempS;
					}
				}
				//close ring
				Coordinate[] points = new Coordinate[is.connecting.size()*2+1];
				for(int j=0; j<segments.length; j++){
					points[j*2] = segments[j].p0;
					points[j*2+1] = segments[j].p1;
				}
				points[points.length-1] = points[0];
				//create ring
				Geometry intersectionShape = gf.createLinearRing(points);
				obj.startObject("intersection_"+this.hashCode());
	
				//convert Coordinate to Vector3f
				Coordinate[] cs = intersectionShape.getCoordinates(); 
				Vector3f[] verts = new Vector3f[cs.length];
				Vector3f[] vertsz = new Vector3f[cs.length];
	
				
				
				
				float elevation = city.ter.get((int)is.pos.x, (int)is.pos.y);
	
				for(int j=0; j<cs.length; j++){
					verts[cs.length-j-1] = new Vector3f((float)cs[j].x,(float)cs[j].y, elevation);
					vertsz[j] = new Vector3f((float)cs[j].x,(float)cs[j].y, 0);
				}
	
				for(int j=0; j<cs.length; j++){
					obj.face(new Vector3f[]{new Vector3f(verts[j].x, verts[j].y, 0), new Vector3f(verts[(j+1)%cs.length].x, verts[(j+1)%cs.length].y, 0), verts[j]});
					obj.face(new Vector3f[]{new Vector3f(verts[(j+1)%cs.length].x, verts[(j+1)%cs.length].y, 0), verts[(j+1)%cs.length], verts[j]});
				}
	
				obj.face(verts);
				obj.face(vertsz);
				obj.endObject();
			}else{
				//roads that have only 1 intersection
				
			}
		}
		ArrayList<Road> roadList = (ArrayList<Road>) roads.queryAll();
		for(int i=0; i<roadList.size(); i++){
			Road r = roadList.get(i);
			//make rectangle geometry with road ends as terrain height.
			Vector3f a = new Vector3f((float)r.a.pos.x, (float)r.a.pos.y, city.ter.get((int)r.a.pos.x, (int)r.a.pos.y));
			Vector3f b = new Vector3f((float)r.b.pos.x, (float)r.b.pos.y, city.ter.get((int)r.b.pos.x, (int)r.b.pos.y));

			double ang = Angle.angle(r.a.pos, r.b.pos);
			
			//for avoiding intersection center ... doesn't work flawlessly
			float xBackA = (float)(Math.cos(ang)*(r.roadExtrusionA)); 
			float xBackB = (float)(Math.cos(ang)*(r.roadExtrusionB));
			float yBackA = (float)(Math.sin(ang)*(r.roadExtrusionA));
			float yBackB = (float)(Math.sin(ang)*(r.roadExtrusionB));
			
			a.x += xBackA;
			a.y += yBackA;
			b.x -= xBackB;
			b.y -= yBackB;
			
			float x = (float)Math.cos(ang+Math.toRadians(90))*(r.width/2);
			float y = (float)Math.sin(ang+Math.toRadians(90))*(r.width/2);

			Vector3f p1 = new Vector3f(a.x+x, a.y+y, a.z);
			Vector3f p2 = new Vector3f(b.x+x, b.y+y, b.z);

			Vector3f p3 = new Vector3f(b.x-x, b.y-y, b.z);
			Vector3f p4 = new Vector3f(a.x-x, a.y-y, a.z);
			
			Vector3f p1z, p2z, p3z, p4z;
			
			if(roadToZero){
				p1z = new Vector3f(a.x+x, a.y+y, 0);
				p2z = new Vector3f(b.x+x, b.y+y, 0);
				p3z = new Vector3f(b.x-x, b.y-y, 0);
				p4z = new Vector3f(a.x-x, a.y-y, 0);
			}else{
				p1z = new Vector3f(a.x+x, a.y+y, a.z-roadThickness);
				p2z = new Vector3f(b.x+x, b.y+y, b.z-roadThickness);
				p3z = new Vector3f(b.x-x, b.y-y, b.z-roadThickness);
				p4z = new Vector3f(a.x-x, a.y-y, a.z-roadThickness);
			}
			
			//top
			obj.face(new Vector3f[]{p4,p3,p2,p1});
			
			//sides
			//side
			obj.face(new Vector3f[]{p4z,p3z,p3,p4});
			//side
			obj.face(new Vector3f[]{p2z,p1z,p1,p2});
			
			//intersection (b)
			if(r.b.connecting.size()==1)
			obj.face(new Vector3f[]{p3z,p2z,p2,p3});
			//intersection (a)
			if(r.a.connecting.size()==1)
			obj.face(new Vector3f[]{p1z,p4z,p4,p1});
			
			//bottom
			obj.face(new Vector3f[]{p1z,p2z,p3z,p4z});
			
		}
		obj.endObject();
	}

	/*private Road waterCheck(Road r){
		if(r==null){
			return null;
		}
		Coordinate loc = r.b.pos;
		//WATER CHECK
		//is it in water?
		for(int test=0; test<waterTests; test++){
			double xD = r.a.pos.x - r.b.pos.x;
			double yD = r.a.pos.y - r.b.pos.y;

			double x = ((xD/(double)waterTests)*(double)test)+r.a.pos.x;
			double y = ((yD/(double)waterTests)*(double)test)+r.a.pos.y;
			float waterAvg = city.water.getCircleAvg((int)x, (int)y, noWaterSampleRadius);
			if(waterAvg >= noWaterCutoffDensity){
				//BRIDGE
				if(!(r.getType() == RoadType.HIGHWAY || r.getType() == RoadType.BRIDGE)){
					return null;
				}else{
					//technically, it should never be a bridge
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
								r.setType(RoadType.HIGHWAY);
							}
							r.b.pos = bestBridgePosition;
							return r;
						}else{
							return null;
						}

					}

				}
			}
		}
		//Road passed WATER CHECK
		return r;
	}*/
}
