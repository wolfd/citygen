package com.wolfd.citygen.city.buildings;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class Building {
	public ArrayList<Section> sections;
	public Geometry lotShape;
	
	public Building(Geometry lotShape){ //temporary
		this.lotShape = lotShape;
		sections = new ArrayList<Section>();
		sections.add(new Section(lotShape, (int)(Math.random()*20)+5));
	}
	
	public Building(String geomString) throws ParseException{
		this(new WKTReader().read(geomString));
	}
}
