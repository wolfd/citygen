package wolf.city.road;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import wolf.util.OBJ;
import wolf.util.OBJOutput;

import com.vividsolutions.jts.geom.Coordinate;

public class Intersection implements OBJOutput{
	private static int curId;
	public static ArrayList<Intersection> intersections = new ArrayList<Intersection>();
	public Coordinate pos;
	public Road parent;
	public int id;
	public List<Road> connecting = new LinkedList<Road>();

	public Intersection(Coordinate pos){
		this.pos = new Coordinate(pos);
		id = curId++;
		intersections.add(this);
	}
	
	public void addConnecting(Road road){
		connecting.add(road);
	}
	
	public String toString(){
		return "pos: "+pos.toString()+" connecting: "+connecting.toString();
	}

	@Override
	public void asOBJ(OBJ obj) {
		int maxWidth = -1;
		for(int i=0; i<connecting.size(); i++){
			Road r = connecting.get(i);
			if(maxWidth > r.width) maxWidth = r.width;
			
		}
		
		//TODO complete asOBJ for intersections
		
	}
}
