package wolf.city.road;

import wolf.city.road.rules.Basic;
import wolf.city.road.rules.Grid;
import wolf.city.road.rules.RoadRule;
import wolf.util.Turtle;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.buffer.BufferParameters;

public class Road {

	public Intersection a;
	public Intersection b;
	private RoadType type;
	public boolean finished;
	public RoadRule rule;
	public int width;
	public int numberParents;
	private static GeometryFactory gf = new GeometryFactory();

	public Road(Intersection a, Intersection b, RoadType type, RoadRule rr, Road parent){
		this.a = a;
		this.b = b;
		this.type = type;
		width = type.getWidth();
		rule = rr;
		if(parent != null){
			
			numberParents += parent.numberParents;
		}
	}
	
	public Road(Intersection a, Intersection b, RoadType type, RoadRule rr){
		this(a, b, type, rr, null);
	}

	public Road(Road r) {
		this(new Intersection(r.a.pos), new Intersection(r.b.pos),r.type, r.rule, null);
	}

	public void setType(RoadType type){
		this.type = type;
		this.width = type.getWidth();
		switch(type){
		case STREET:
			rule = new Grid(rule.getCity());
			break;
		case MAIN:
			rule = new Basic(rule.getCity());
//				public float turnRateForward = 40;
//			};
		case HIGHWAY:
			rule = new Basic(rule.getCity());
		}
	}

	public RoadType getType(){
		return type;
	}

	public Geometry getGeometry(){
		return getGeometry(0);
	}
	public Geometry getGeometry(int extraWidth){
		int modWidth = width + extraWidth;
		Coordinate a0 = a.pos;
		Coordinate b0 = b.pos;

		double ang = Angle.angle(a.pos, b.pos);

		double x = Math.cos(ang+Math.toRadians(90))*(modWidth/2);
		double y = Math.sin(ang+Math.toRadians(90))*(modWidth/2);

		Coordinate p1 = new Coordinate(a0.x+x,a0.y+y);
		Coordinate p2 = new Coordinate(b0.x+x,b0.y+y);

		Coordinate p3 = new Coordinate(b0.x-x,b0.y-y);
		Coordinate p4 = new Coordinate(a0.x-x,a0.y-y);
		
		LinearRing lr = gf.createLinearRing(new Coordinate[]{p1,p2,p3,p4,p1});
		Polygon poly = gf.createPolygon(lr, null);

		return poly;
	}
	
	public LineSegment getLineSegment(){
		LineSegment ls = new LineSegment(a.pos, b.pos);
		return ls;
	}
	
	public Geometry getCollisionGeometry(){
		int collisionGeometryWidth = width*3;
		double collisionGeometryAngle = 30;
		double minimumRoadLength = 4;
		double length = Math.sin(Math.toRadians(collisionGeometryAngle))/collisionGeometryWidth;
		
		Coordinate[] coords = new Coordinate[7];
		
		double ang = Math.toDegrees(Angle.angle(a.pos, b.pos));
		
		Turtle ta = new Turtle(a.pos, ang);
		ta.move(minimumRoadLength*.9);
		coords[0] = new Coordinate(ta.pos); //start point
		coords[6] = coords[0]; //first element is also last
		ta.turn(collisionGeometryAngle);
		ta.move(length);
		coords[1] = new Coordinate(ta.pos);
		ta.angle = ang - 90;
		ta.move(collisionGeometryWidth);
		coords[5] = new Coordinate(ta.pos);
		
		Turtle tb = new Turtle(b.pos, ang+180);
		tb.move(minimumRoadLength*.9);
		coords[3] = new Coordinate(tb.pos); //end point
		tb.turn(collisionGeometryAngle);
		tb.move(length);
		coords[4] = new Coordinate(tb.pos);
		tb.angle = ang - 90;
		tb.move(collisionGeometryWidth);
		coords[2] = new Coordinate(tb.pos);
		
		Polygon p = gf.createPolygon(gf.createLinearRing(coords), null);
		
		
		
		return p;
		
	}
	
	public Geometry getIntersectionGeometry(){
		Geometry buffer1 = gf.createPoint(a.pos).buffer(width*1.1d, 8);
		Geometry buffer2 = gf.createPoint(b.pos).buffer(width*1.1d, 8);
		return buffer1.union(buffer2);
		
	}
	
	public Geometry getFinalGeometry(){
		Geometry g = gf.createLineString(new Coordinate[]{a.pos, b.pos});
		return g.buffer(width, width/2, BufferParameters.CAP_ROUND);
	}
}
