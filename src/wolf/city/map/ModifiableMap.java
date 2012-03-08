package wolf.city.map;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

import wolf.city.City;
import wolf.city.road.Road;
import wolf.util.Turtle;

public class ModifiableMap extends InputMap {

	private GeometryFactory gf;

	public ModifiableMap(City city){
		super(city);
		PrecisionModel pm = new PrecisionModel(PrecisionModel.FLOATING);
		gf = new GeometryFactory(pm);
	}
	public ModifiableMap(City city, boolean faded){
		super(city, faded);
		PrecisionModel pm = new PrecisionModel(PrecisionModel.FLOATING);
		gf = new GeometryFactory(pm);
	}
	public ModifiableMap(City city, boolean faded, int octaves){
		super(city, faded, octaves);
		PrecisionModel pm = new PrecisionModel(PrecisionModel.FLOATING);
		gf = new GeometryFactory(pm);
	}

	public void set(int x, int y, float value) {
		x+=sizeX/2;
		y+=sizeY/2;
		if(x>=sizeX || x<0 || y>=sizeY || y<0){ //out of bounds
		}else{
			m[x][y] += value;
		}
	}

	@Deprecated
	public void removeDensityLine(Road r) {
		int width = (int) (r.width*3.1); //totally ambiguous
		Coordinate a0 = r.a.pos;
		Coordinate b0 = r.b.pos;
		Turtle ta = new Turtle(a0, Math.toDegrees(Angle.angle(b0, a0)));
		ta.move(width);
		Turtle tb = new Turtle(b0, Math.toDegrees(Angle.angle(a0, b0)));
		tb.move(width);
		Coordinate a = new Coordinate(ta.pos);
		Coordinate b = new Coordinate(tb.pos);

		double ang = Angle.angle(r.a.pos, r.b.pos);


		double x = Math.cos(ang+Math.toRadians(90))*(width/2);
		double y = Math.sin(ang+Math.toRadians(90))*(width/2);

		Coordinate p1 = new Coordinate(a.x+x,a.y+y);
		Coordinate p2 = new Coordinate(b.x+x,b.y+y);

		Coordinate p3 = new Coordinate(b.x-x,b.y-y);
		Coordinate p4 = new Coordinate(a.x-x,a.y-y);

		LinearRing lr = gf.createLinearRing(new Coordinate[]{p1,p2,p3,p4,p1});
		Polygon poly = gf.createPolygon(lr, null);

		for(int ix=(int) Math.min(a.x-width,b.x-width); ix<(int)Math.max(a.x+width, b.x+width); ix++){
			for(int iy=(int)Math.min(a.y-width,b.y-width); iy<(int)Math.max(a.y+width, b.y+width); iy++){
				Coordinate cur = new Coordinate(ix,iy);
				Geometry p = gf.createPoint(cur);
				if(poly.contains(p)){

					set((int)cur.x, (int)cur.y, -.4f);
				}
			}
		}

	}

	public void reset(){
		m = new float[sizeX][sizeY];
	}
}
