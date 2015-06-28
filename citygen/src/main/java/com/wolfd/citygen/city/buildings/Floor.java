package com.wolfd.citygen.city.buildings;

import java.util.ArrayList;

import com.wolfd.citygen.city.buildings.shape.WallContainer;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class Floor {

	public ArrayList<WallContainer> exterior;
	public float height;

	public Floor(Geometry shape, float height) {
		this.height = height;
		exterior = new ArrayList<WallContainer>();
		Coordinate[] cs = shape.getCoordinates();
		exterior.add(new WallContainer(this, cs[cs.length-1], cs[0]));
		for(int i=1; i<cs.length; i++){
			Coordinate p0 = cs[i-1];
			Coordinate p1 = cs[i];
			exterior.add(new WallContainer(this, p0, p1));
			
		}
	}
}
