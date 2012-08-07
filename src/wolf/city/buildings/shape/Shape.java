package wolf.city.buildings.shape;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Geometry;

public class Shape {
	public ArrayList<Shape> children;
	public Shape parent;
	
	public Geometry shape;
	
	public String name;
	
}
