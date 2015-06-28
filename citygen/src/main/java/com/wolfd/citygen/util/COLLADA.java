package com.wolfd.citygen.util;

import com.vividsolutions.jts.geom.Coordinate;

public class COLLADA {
	public static String vertex(Coordinate c){
		c = correct(c);
		return "vertex "+c.x+" "+c.z+" "+c.y+"\n";
	}
	
	public static String normal(Coordinate a, Coordinate b, Coordinate c){
		Coordinate edge1 = new Coordinate(b.x-a.x,b.y-a.y,b.z-a.z);
		Coordinate edge2 = new Coordinate(c.x-a.x,c.y-a.y,c.z-a.z);
		Coordinate normal = cross(edge1,edge2);
		double mag = Math.sqrt(Math.pow(normal.x,2)+Math.pow(normal.z,2)+Math.pow(normal.z,2));
		normal.x /= mag;
		normal.y /= mag;
		normal.z /= mag;
		normal = correct(normal);
		return "facet normal "+normal.x+" "+normal.z+" "+normal.y+"\n";
	}
	
	public static Coordinate cross(Coordinate a, Coordinate b){
		Coordinate result = new Coordinate();
		if(Double.isNaN(a.z)) a.z = 0;
		if(Double.isNaN(b.z)) b.z = 0;
		result.x = a.y * b.z - a.z * b.y;
		result.y = a.z * b.x - a.x * b.z;
		result.z = a.x * b.y - a.y * b.x;
		return correct(result);
	}
	
	public static Coordinate correct(Coordinate c){
		if(Double.isNaN(c.z)) c.z = 0;
		if(Double.isNaN(c.x)) c.x = 0;
		if(Double.isNaN(c.y)) c.y = 0;
		if(Double.isInfinite(c.x)) c.x = 1; 
		if(Double.isInfinite(c.y)) c.y = 1; 
		if(Double.isInfinite(c.z)) c.z = 1; 
		return c;
	}
	
	public static String rect(Coordinate tl, Coordinate br){
		String stl = "";
		Coordinate bl = new Coordinate(br);
		bl.x = tl.x;
		Coordinate tr = new Coordinate(tl);
		tr.x = br.x;
		//first triangle
		stl += normal(tr, tl, bl);
		stl += "outer loop\n";
		stl += vertex(tr);
		stl += vertex(tl);
		stl += vertex(bl);
		stl += "endloop\n";
		stl += "endfacet\n";
		//second triangle
		stl += normal(tr, bl, br);
		stl += "outer loop\n";
		stl += vertex(tr);
		stl += vertex(bl);
		stl += vertex(br);
		stl += "endloop\n";
		stl += "endfacet\n";
		
		return stl;
	}
}
