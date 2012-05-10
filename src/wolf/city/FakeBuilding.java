package wolf.city;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class FakeBuilding {
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
			stl += "solid building\n";

			Coordinate c1 = cs[cs.length-1];
			for(int i=0; i<cs.length; i++){
				Coordinate c2 = cs[i];
				//make 2 triangles
				Coordinate ch1 = new Coordinate(c1);
				ch1.z += height;
				Coordinate ch2 = new Coordinate(c2);
				ch2.z += height;
				//first triangle
				stl += "facet normal \n";
				stl += "outer loop\n";
				stl += "vertex "+c1.x+" "+c1.y+" "+c1.z+"\n";
				stl += "vertex "+c2.x+" "+c2.y+" "+c2.z+"\n";
				stl += "vertex "+ch1.x+" "+ch1.y+" "+ch1.z+"\n";
				stl += "endloop\n";
				stl += "endfacet\n";
				//second triangle
				stl += "facet normal \n";
				stl += "outer loop\n";
				stl += "vertex "+c2.x+" "+c2.y+" "+c2.z+"\n";
				stl += "vertex "+ch2.x+" "+ch2.y+" "+ch2.z+"\n";
				stl += "vertex "+ch1.x+" "+ch1.y+" "+ch1.z+"\n";
				stl += "endloop\n";
				stl += "endfacet\n";
				c1 = c2;
			}

			stl += "endsolid building\n";
		}
		return stl;

	}

	private boolean sameSide(Coordinate p1, Coordinate p2, Coordinate l1, Coordinate l2){
		return (((p1.x - l1.x) * (l2.y - l1.y) - (l2.x - l1.x) * (p1.y - l1.y)) * ((p2.x - l1.x) * (l2.y - l1.y) - (l2.x - l1.x) * (p2.y - l1.y)) > 0);
	}

	private boolean inside(Coordinate p, Coordinate a, Coordinate b, Coordinate c){
		return sameSide(p, a, b, c) && sameSide(p, b, a,c) && sameSide(p, c, a, b);
	}

	private ArrayList<Coordinate[]> split(Coordinate[] cs){
		ArrayList<Coordinate[]> tris = new ArrayList<Coordinate[]>();
		boolean done = false;
		while(!done ){
			int leftmost = -1;
			double left = cs[0].x;
			for(int i=1; i<cs.length; i++){
				if(cs[i].x < left){
					left = cs[i].x;
					leftmost = i;
				}
			}
			if(leftmost == -1) return null;
			tris.add(new Coordinate[]{cs[(leftmost-1)%cs.length], cs[leftmost], cs[(leftmost+1)%cs.length]});
			//make new cs without these coords
			//http://www.siggraph.org/education/materials/HyperGraph/scanline/outprims/polygon1.htm
		}
		return null;
	}
}
