package wolf.city.buildings.shape;

import wolf.city.buildings.Floor;

import com.vividsolutions.jts.geom.Coordinate;

public class WallContainer {
	public Floor floor;
	public Wall wall;
	public Coordinate p0;
	public Coordinate p1;
	
	
	public WallContainer(Floor f, Coordinate p0, Coordinate p1) {
		this.p0 = p0;
		this.p1 = p1;
		floor = f;
		
		wall = new Wall(floor, (float)this.p0.distance(this.p1));
		if(Math.random()>.5){
			wall.children = wall.split(5, SplitDirection.FROMCENTER);
		}else{
			wall.children = wall.split(16, SplitDirection.TOCENTER);
		}
	}
}
