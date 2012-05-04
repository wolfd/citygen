package wolf.city.road;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

public class Intersection{
	private static int curId;
	public static ArrayList<Intersection> intersections;
	public Coordinate pos;
	public Road parent;
	public int id;
	public List<Intersection>connecting = new LinkedList<Intersection>();

	public Intersection(Coordinate pos){
		this.pos = new Coordinate(pos);
		id = curId++;
		intersections.add(this);
	}
	
	public void addConnecting(Intersection intersection){
		connecting.add(intersection);
	}
	
	public String toString(){
		return "pos: "+pos.toString()+" connecting: "+connecting.toString();
	}
}
