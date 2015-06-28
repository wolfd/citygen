package com.wolfd.citygen.city.road.rules.global;

import com.wolfd.citygen.city.City;
import com.wolfd.citygen.city.road.Intersection;
import com.wolfd.citygen.city.road.Road;
import com.wolfd.citygen.city.road.RoadType;
import com.wolfd.citygen.util.RandomHelper;
import com.wolfd.citygen.util.Turtle;
import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;

public class Basic implements RoadRule {
	int numberTests = 10;
	private double idealLength = 64; //128
	public float turnRateForward = 10; //20
	public float turnRateLeftRight = 30;
	public City c;
	
	public Basic(City c){
		this.c = c;
	}

	@Override
	public Road globalGoals(City city, Road road, Direction d) { //0 - left, 1 - forward, 2 - right, 3 - backward
		Road returnRoad = null;

		float previousAngle = (float)Math.toDegrees(Angle.angle(road.a.pos, road.b.pos));
		//double nextRoadLength = (road.a.pos.distance(road.b.pos)*.5) + (idealLength *.5); //used for determining size of new road
		double nextRoadLength = idealLength;
		double nextRoadLengthStreet = 32;
		//System.out.println(nextRoadLength);



		//ramp forward
		//left 50 to 20, forward 20 to -20, right -20 to -50
		RoadType nextRoadType = RoadType.DEFAULT;
		Turtle t = new Turtle(road.b.pos, 0);
		float angle = 0;
		float bestPopulation = 0;
		Coordinate bestPosition = null;
		for(int i = 0;i<numberTests ;i++){
			t = new Turtle(road.b.pos, 0);
			double roadLength = nextRoadLength;
			switch(d){
			case LEFT: //left
				angle = RandomHelper.random(city.random.nextFloat(), turnRateLeftRight, -turnRateLeftRight)+previousAngle+90;
				nextRoadType = RoadType.MAIN;
				roadLength = nextRoadLengthStreet;
				break;
			case FORWARD: //forward
				angle = RandomHelper.random(city.random.nextFloat(), turnRateForward, -turnRateForward)+previousAngle;
				nextRoadType = RoadType.HIGHWAY;
				roadLength = nextRoadLength;//Math.abs(Math.cos(Math.toRadians((previousAngle-angle)%360))*nextRoadLength); //sin approximates how long next road should be
				break;
			case RIGHT: //right
				angle = RandomHelper.random(city.random.nextFloat(), turnRateLeftRight, -turnRateLeftRight)+previousAngle+270;
				nextRoadType = RoadType.MAIN;
				roadLength = nextRoadLengthStreet;
				break;
			case BACKWARD: //backward
				angle = -RandomHelper.random(city.random.nextFloat(), turnRateForward, -turnRateForward)+previousAngle;
				nextRoadType = RoadType.HIGHWAY;
				roadLength = nextRoadLength;
				break;
			default:
				//invalid direction
				break;
			}

			t.angle = angle;
			t.move((float)roadLength);
			float population = city.pop.get((int)t.pos.x, (int)t.pos.y);
			if(population>=bestPopulation ){ //&& water < .7
				bestPopulation = population;
				bestPosition = new Coordinate(t.pos);
			}
		}
		if(bestPosition == null){
			//same as forward
			t = new Turtle(road.b.pos, 0);
			angle = RandomHelper.random(city.random.nextFloat(), 20, -20);
			nextRoadType = RoadType.HIGHWAY;
			double roadLength = Math.abs(Math.sin(Math.toRadians((previousAngle-angle)%360))*nextRoadLength); //sin approximates how long next road should be
			t.angle = angle;
			t.move((float)roadLength);
			returnRoad = new Road(road.b, new Intersection(new Coordinate(t.pos)), nextRoadType, this, road); //roads set to default should die.
		}else{
			returnRoad = new Road(road.b, new Intersection(bestPosition), nextRoadType, this, road); //roads set to default should die.
		}
		return returnRoad;
	}

	@Override
	public RoadRule mutate() {
		if(c.random.nextDouble()>.9){
			if(c.random.nextBoolean()){
				turnRateForward += (c.random.nextFloat()-.5)*2f*5*c.rm.chaos;
			}else{
				turnRateLeftRight += (c.random.nextFloat()-.5)*2f*5*c.rm.chaos;
			}
		}
		return this;
	}

	@Override
	public City getCity() {
		return c;
	}

}
