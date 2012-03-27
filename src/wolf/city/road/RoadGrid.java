package wolf.city.road;

import java.util.ArrayList;
import java.util.LinkedList;

import com.vividsolutions.jts.geom.Coordinate;

public class RoadGrid {
	private ArrayList<ArrayList<LinkedList<Road>>> grid;
	private int gridSize = 256;
	private double bufferSize = 5;
	private int gridX;
	private int gridY;
	private int sizeX;
	private int sizeY;
	
	public RoadGrid(int sizeX, int sizeY){
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		gridX = (int)Math.ceil(sizeX/gridSize);
		gridY = (int)Math.ceil(sizeY/gridSize);
		grid = new ArrayList<ArrayList<LinkedList<Road>>>();
		while(grid.size()<gridX){ //create the grid
			grid.add(new ArrayList<LinkedList<Road>>());
			while(grid.get(grid.size()-1).size()<gridY){
				grid.get(grid.size()-1).add(new LinkedList<Road>());
			}
		}
		
	}
	
	public LinkedList<Road> get(int x, int y){
		x = Math.max(Math.min(x,gridX), 0);
		y = Math.max(Math.min(y,gridY), 0);
		return grid.get(x).get(y);
	}
	
	public LinkedList<Road> get(GridSpace g){
		int x = Math.max(Math.min(g.x,gridX), 0);
		int y = Math.max(Math.min(g.y,gridY), 0);
		return grid.get(x).get(y);
	}
	
	public void add(Road r){
		ArrayList<GridSpace> spaces = getSpaces(r);
		for(int i=0; i<spaces.size(); i++){
			get(spaces.get(i)).add(r);
		}
		
	}
	
	public void move(Road r){
		
		for(int ix=0; ix<gridX; ix++){
			for(int iy=0; iy<gridY; iy++){
				get(new GridSpace(ix, iy)).remove(r);
			}
		}
		
		add(r);
	}
	
	public GridSpace gridSpace(Coordinate c){
		double x = c.x + sizeX/2; //convert to (hopefully) positive coordinate
		x = Math.max(Math.min(x, sizeX),0);
		x /= gridSize;
		double y = c.y + sizeY/2; //convert to (hopefully) positive coordinate
		y = Math.max(Math.min(y, sizeY),0);
		y /= gridSize;
		return new GridSpace((int)x,(int)y);
	}
	
	
	public ArrayList<GridSpace> getSpaces(Road r){
		Coordinate a = r.a.pos;
		Coordinate b = r.b.pos;
		Coordinate min = new Coordinate(Math.min(a.x, b.x)-bufferSize, Math.min(a.y, b.y)-bufferSize);
		Coordinate max = new Coordinate(Math.max(a.x, b.x)+bufferSize, Math.max(a.y, b.y)+bufferSize);
		
		GridSpace g0 = gridSpace(min);
		GridSpace g1 = gridSpace(max);
		
		ArrayList<GridSpace> spaces = new ArrayList<GridSpace>();
		
		for(int ix=g0.x; ix<=g1.x; ix++){
			for(int iy=g0.y; iy<g1.y; iy++){
				spaces.add(new GridSpace(ix, iy));
			}
		}
		
		return spaces;
	}
}
