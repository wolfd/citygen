package wolf.city.block;

import wolf.city.buildings.FakeBuilding;

import com.vividsolutions.jts.geom.Polygon;

public class Lot {
	public Polygon shape;
	public FakeBuilding building;
	
	public Lot(Polygon f) {
		shape = f;
	}
	
	public String toString(){
		return shape.toString();
	}
}
