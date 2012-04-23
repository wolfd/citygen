package wolf.city.block;

import java.util.LinkedList;

import com.vividsolutions.jts.geom.Polygon;

public class CityBlock {
	public Polygon shape;
	public LinkedList<Lot> lots;
	
	public CityBlock(Polygon p){
		shape = p;
	}
}
