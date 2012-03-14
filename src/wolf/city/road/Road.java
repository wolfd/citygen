package wolf.city.road;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

public class Road {

	public Intersection a;
	public Intersection b;
	private RoadType type;
	public int width;

	public Road(Intersection a, Intersection b, RoadType type){
		this.a = a;
		this.b = b;
		this.type = type;
		width = type.getWidth();
	}

	public void setType(RoadType type){
		this.type = type;
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
		//this added caps to the geometry	
		//		Turtle ta = new Turtle(a0, Math.toDegrees(Angle.angle(b0, a0)));
		//		ta.move(width);
		//		Turtle tb = new Turtle(b0, Math.toDegrees(Angle.angle(a0, b0)));
		//		tb.move(width);
		//		Coordinate a1 = new Coordinate(ta.pos);
		//		Coordinate b1 = new Coordinate(tb.pos);

		double ang = Angle.angle(a.pos, b.pos);

		double x = Math.cos(ang+Math.toRadians(90))*(modWidth/2);
		double y = Math.sin(ang+Math.toRadians(90))*(modWidth/2);

		Coordinate p1 = new Coordinate(a0.x+x,a0.y+y);
		Coordinate p2 = new Coordinate(b0.x+x,b0.y+y);

		Coordinate p3 = new Coordinate(b0.x-x,b0.y-y);
		Coordinate p4 = new Coordinate(a0.x-x,a0.y-y);

		GeometryFactory gf = new GeometryFactory();
		
		LinearRing lr = gf .createLinearRing(new Coordinate[]{p1,p2,p3,p4,p1});
		Polygon poly = gf.createPolygon(lr, null);

		return poly;
	}
	
	public LineSegment getLineSegment(){
		LineSegment ls = new LineSegment(a.pos, b.pos);
		return ls;
	}
}
