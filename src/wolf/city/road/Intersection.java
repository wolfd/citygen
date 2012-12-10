package wolf.city.road;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

public class Intersection{
	private static int curId;
	public static ArrayList<Intersection> intersections = new ArrayList<Intersection>();
	public Coordinate pos;
	public Road parent;
	public int id;
	public List<Road> connecting;
	public double roadExtrusion;

	public Intersection(Coordinate pos){
		this.pos = new Coordinate(pos);
		id = curId++;
		connecting = new LinkedList<Road>();
		intersections.add(this);
		roadExtrusion = -1;
	}
	
	public void addConnecting(Road road){
		connecting.add(road);
	}
	
	public String toString(){
		return "pos: "+pos.toString()+" connecting: "+connecting.toString();
	}
}
