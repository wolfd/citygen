package wolf.city.road;

import java.util.List;
import java.util.LinkedList;

public class RoadQueue {
	public List<Road> roads;
	public boolean stackStyle;
	
	public RoadQueue() {
		roads = new LinkedList<Road>();
		stackStyle = false;
	}

	public void add(Road road){
		if(road != null && road.a != null && road.b != null && road.a.pos != null && road.b.pos != null){
			roads.add(road);
		}
	}

	public Road remove(){
		Road returnRoad;
		if(stackStyle){
			returnRoad = roads.remove(roads.size()-1);
		}else{
			returnRoad = roads.remove(0);
		}

		return returnRoad;
	}

	public boolean isEmpty(){
		if(roads.size()>0){
			return false;
		}else{
			return true;
		}
	}

}
