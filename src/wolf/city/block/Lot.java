package wolf.city.block;

import com.vividsolutions.jts.geom.Polygon;

public class Lot {
	public Polygon shape;
	
	public Lot(Polygon f) {
		shape = f;
	}
	
	public String toString(){
		return shape.toString();
	}
}
