package wolf.city;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.triangulate.DelaunayTriangulationBuilder;

public class FakeBuilding {
	private static GeometryFactory gf = new GeometryFactory();
	public Geometry g;
	public int height;

	public FakeBuilding(Geometry geometry, int h){
		height = h;
		g = geometry;
	}

	public String toSTL(){
		String stl = "";
		Coordinate[] cs = g.getCoordinates();
		if(cs.length >= 3){
			

			Coordinate c1 = cs[cs.length-1];
			for(int i=0; i<cs.length; i++){
				Coordinate c2 = cs[i];
				//make 2 triangles
				Coordinate ch1 = new Coordinate(c1);
				if(Double.isNaN(ch1.z)) ch1.z = 0;
				ch1.z += height;
				Coordinate ch2 = new Coordinate(c2);
				if(Double.isNaN(ch2.z)) ch2.z = 0;
				ch2.z += height;
				//first triangle
				
				stl += normal(c1, c2, ch1);
				stl += "outer loop\n";
				stl += vertex(c1);
				stl += vertex(c2);
				stl += vertex(ch1);
				stl += "endloop\n";
				stl += "endfacet\n";
				//second triangle
				stl += normal(c2, ch2, ch1);
				stl += "outer loop\n";
				stl += vertex(c2);
				stl += vertex(ch2);
				stl += vertex(ch1);
				stl += "endloop\n";
				stl += "endfacet\n";
				c1 = c2;
			}
			ArrayList<Geometry> gBase = toTriangles(g);
			for(Geometry tri: gBase){
				//base
				stl += normal(tri.getCoordinates()[2],tri.getCoordinates()[1],tri.getCoordinates()[0]);
				stl += "outer loop\n";
				stl += vertex(tri.getCoordinates()[2]);
				stl += vertex(tri.getCoordinates()[1]);
				stl += vertex(tri.getCoordinates()[0]);
				stl += "endloop\n";
				stl += "endfacet\n";
				
				//top
				Coordinate a = new Coordinate(tri.getCoordinates()[0]);
				Coordinate b = new Coordinate(tri.getCoordinates()[1]);
				Coordinate c = new Coordinate(tri.getCoordinates()[2]);
				a.z += height;
				b.z += height;
				c.z += height;
				
				stl += normal(tri.getCoordinates()[0],tri.getCoordinates()[1],tri.getCoordinates()[2]);
				stl += "outer loop\n";
				stl += vertex(a);
				stl += vertex(b);
				stl += vertex(c);
				stl += "endloop\n";
				stl += "endfacet\n";
			}
		}
		return stl;

	}

	private ArrayList<Geometry> toTriangles(Geometry g){
		ArrayList<Geometry> valid = new ArrayList<Geometry>();
		DelaunayTriangulationBuilder triator = new DelaunayTriangulationBuilder();
		triator.setSites(g);
		Geometry tris = triator.getTriangles(gf);
		for(int i=0; i<tris.getNumGeometries(); i++){
			if(g.contains(tris.getGeometryN(i))) valid.add(tris.getGeometryN(i));
		}
		return valid;
	}
	
	private String vertex(Coordinate c){
		if(Double.isNaN(c.z)) c.z = 0;
		return "vertex "+c.x+" "+c.y+" "+c.z+"\n";
	}
	
	private String normal(Coordinate a, Coordinate b, Coordinate c){
		Coordinate edge1 = new Coordinate(b.x-a.x,b.y-a.y,b.z-a.z);
		Coordinate edge2 = new Coordinate(c.x-a.x,c.y-a.y,c.z-a.z);
		Coordinate normal = cross(edge1,edge2);
		double mag = Math.sqrt(Math.pow(normal.x,2)+Math.pow(normal.z,2)+Math.pow(normal.z,2));
		normal.x /= mag;
		normal.y /= mag;
		normal.z /= mag;
		if(Double.isNaN(normal.x)) normal.x = 0;
		if(Double.isNaN(normal.y)) normal.y = 0;
		if(Double.isNaN(normal.z)) normal.z = 0;
		return "facet normal "+normal.x+" "+normal.y+" "+normal.z+"\n";
	}
	
	private Coordinate cross(Coordinate a, Coordinate b){
		Coordinate result = new Coordinate();
		if(Double.isNaN(a.z)) a.z = 0;
		if(Double.isNaN(b.z)) b.z = 0;
		result.x = a.y * b.z - a.z * b.y;
		result.y = a.z * b.x - a.x * b.z;
		result.z = a.x * b.y - a.y * b.x;
		return result;
	}
	
	private Coordinate correct(Coordinate c){
		return null;
	}
}
