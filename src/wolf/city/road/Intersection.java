package wolf.city.road;

import java.util.LinkedList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

public class Intersection{
	public Coordinate pos;
	public Road parent;
	public List<Intersection>connecting = new LinkedList<Intersection>();

	public Intersection(Coordinate pos){
		this.pos = new Coordinate(pos);
	}
	
	public void addConnecting(Intersection intersection){
		connecting.add(intersection);
	}
	
	public String toString(){
		return "pos: "+pos.toString()+" connecting: "+connecting.toString();
	}
}
