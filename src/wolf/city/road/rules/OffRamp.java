package wolf.city.road.rules;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;

import wolf.city.City;
import wolf.city.road.Intersection;
import wolf.city.road.Road;
import wolf.city.road.RoadType;
import wolf.util.Turtle;

public class OffRamp implements RoadRule {
	int numberTests = 10;
	public City c;

	public OffRamp(City c){
		this.c = c;
	}
	@Override
	public Road globalGoals(City city, Road road, Direction d) { //0 - left, 1 - forward, 2 - right, 3 - backward
		Road returnRoad = null;

		float previousAngle = (float)Math.toDegrees(Angle.angle(road.a.pos, road.b.pos));
		double nextRoadLength = 32;
		//System.out.println(nextRoadLength);
		if(road.getType() == RoadType.HIGHWAY || road.getType() == RoadType.BRIDGE){
			RoadType nextRoadType = RoadType.STREET;
			Turtle t = new Turtle(road.b.pos, 0);
			float angle = 0;

			t = new Turtle(road.b.pos, 0);
			double roadLength = nextRoadLength;
			switch(d){
			case LEFT: //left
				angle = 90+previousAngle;//RandomHelper.random(city.random.nextFloat(), 50, 20);
				roadLength = nextRoadLength;
				break;
			case FORWARD: //forward
				angle = previousAngle;
				roadLength = nextRoadLength;
				break;
			case RIGHT: //right
				angle = 270+previousAngle;//RandomHelper.random(city.random.nextFloat(), -20, -50);
				roadLength = nextRoadLength;
				break;
			case BACKWARD: //backward
				angle = 180+previousAngle;
				roadLength = nextRoadLength;
				break;
			default:
				//invalid direction
				break;
			}

			t.angle = angle;
			t.move((float)roadLength);
			
			returnRoad = new Road(road.b, new Intersection(new Coordinate(t.pos)), nextRoadType, new Basic(c));
		}
		return returnRoad;
	}

	@Override
	public RoadRule mutate() {
		return this;
	}

	@Override
	public City getCity() {
		return c;
	}

}
