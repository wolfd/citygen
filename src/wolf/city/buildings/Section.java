package wolf.city.buildings;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Geometry;

public class Section {
	public Geometry shape;
	public ArrayList<Floor> floors;


	public Section(Geometry g, int numFloors) {
		shape = g;
		floors = new ArrayList<Floor>();
		for(int i=0; i<numFloors; i++){
			floors.add(new Floor(shape, 5));
		}
	}
}
