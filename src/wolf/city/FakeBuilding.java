package wolf.city;

import com.vividsolutions.jts.geom.Geometry;

public class FakeBuilding {
	public Geometry g;
	public int height;
	
	public FakeBuilding(Geometry geometry, int h){
		height = h;
		g = geometry;
	}
}
