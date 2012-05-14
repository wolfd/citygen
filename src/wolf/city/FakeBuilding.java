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

	private ArrayList<Coordinate[]> split(ArrayList<Coordinate> cs){
		ArrayList<Coordinate> untouched = (ArrayList<Coordinate>) cs.clone();
		ArrayList<Coordinate[]> tris = new ArrayList<Coordinate[]>();
		boolean done = false;
		while(!done ){
			int leftmost = -1;
			double left = cs.get(0).x;
			for(int i=1; i<cs.size(); i++){
				if(cs.get(i).x < left){
					left = cs.get(i).x;
					leftmost = i;
				}
			}
			if(leftmost == -1) return null;
			int lastIndex = (leftmost-1)%cs.size();
			
			int nextIndex = (leftmost+1)%cs.size();
			Coordinate[] test = new Coordinate[]{cs.get(lastIndex), cs.get(leftmost), cs.get(nextIndex)};
			//test the test
			for(int j=0; j<untouched.size(); j++){
				Coordinate p = untouched.get(j);
				Coordinate a = test[0];
				Coordinate b = test[1];
				Coordinate c = test[2];
				if(!(p.equals(a) || p.equals(b) || p.equals(c))){
					if(inside(p, a, b, c)){
						//it failed
						//find the new leftmost point
						int leftmost2 = -1;
						double left2 = cs.get(0).x;
						for(int i=1; i<cs.size(); i++){
							if(cs.get(i).x < left2){
								left2 = cs.get(i).x;
								leftmost2 = i;
							}
						}
						if(leftmost2 == -1) return null;
						
						//connect old leftmost with new
						
					}
				}//else nope
			}
			tris.add(test);
			//remove these coords
			//cs.remove(lastIndex);
			cs.remove(leftmost);
			//cs.remove(nextIndex);
			//http://www.siggraph.org/education/materials/HyperGraph/scanline/outprims/polygon1.htm
		}
		return tris;
	}
}
