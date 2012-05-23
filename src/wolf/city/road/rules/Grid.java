package wolf.city.road.rules;

import com.vividsolutions.jts.algorithm.Angle;
import wolf.city.City;
import wolf.city.road.Intersection;
import wolf.city.road.Road;
import wolf.city.road.RoadType;
import wolf.util.Turtle;

public class Grid implements RoadRule {
	private float verticalMoveDistance = 150; //i dislike having to do this here, i need to make the config work better
	private float horizontalMoveDistance = 75;
	public double direction = 0;
	private City c;

	public Grid(City city){
		c = city;
	}
	public Grid(Grid g){
		horizontalMoveDistance = g.horizontalMoveDistance;
		verticalMoveDistance = g.verticalMoveDistance;
		direction = g.direction;
		c = g.c;
	}
	@Override
	public Road globalGoals(City city, Road road, Direction d) {
		double previousAngle = Math.toDegrees(Angle.angle(road.a.pos, road.b.pos));
		Road r = new Road(road.b, new Intersection(road.b.pos), RoadType.STREET, this, road);
		Turtle t;
		if(road.getType() == RoadType.HIGHWAY){// || road.type == RoadType.MAIN){
			t = new Turtle(r.a.pos, previousAngle);
		}else{
			double angle = (Math.floor((previousAngle+45)/90)*90+direction);
			t = new Turtle(r.a.pos, angle);//+((city.random.nextDouble()-.5)*20));
		}
		switch(d){
		case FORWARD:{
			t.move(roadLength(t.angle));
			r.b.pos = t.pos;
			return r;
		}
		case BACKWARD:{
			t.move(-roadLength(t.angle));
			r.b.pos = t.pos;
			return r;
		}
		case LEFT:{
			t.turn(90);
			t.move(-roadLength(t.angle));
			r.b.pos = t.pos;
			return r;
		}
		case RIGHT:{
			t.turn(90);
			t.move(roadLength(t.angle));
			r.b.pos = t.pos;
			return r;
		}
		default:{
			return null;
		}
		}
		
		
	}
	
	private double roadLength(double angle){
		angle = angle%360;
		int dirAngle = (int)Math.floor((angle+45)/90);
		if(dirAngle == 0 || dirAngle == 2){
			return horizontalMoveDistance;
		}else{
			return verticalMoveDistance;
		}	
	}
	
	public RoadRule mutate(){
		Grid g = new Grid(this);
		if(c.random.nextBoolean()){
			g.direction += 30;
		}else{
			g.direction -= 30;
		}
		return g;
	}
	@Override
	public City getCity() {
		return c;
	}

}
