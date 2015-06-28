package com.wolfd.citygen.city.buildings.shape;

import com.wolfd.citygen.city.buildings.Floor;
import com.wolfd.citygen.city.buildings.style.Pattern;

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
		//wall.split(5, SplitMethod.CONTRACT);
		Pattern p = new Pattern(Pattern.PatternType.FIXED, (float)(Math.random()*.05f), new Pattern[]{new Pattern(Pattern.PatternType.RELATIVE, .25f), new Pattern(Pattern.PatternType.RELATIVE, .75f)});
		
		wall.split(p);
	}
	
	
}
