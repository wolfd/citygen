package com.wolfd.citygen.city.block;

import java.util.List;

import com.vividsolutions.jts.geom.Polygon;

public class CityBlock {
	public Polygon shape;
	public List<Lot> lots;
	
	public CityBlock(Polygon p){
		shape = p;
	}
}
