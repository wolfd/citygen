package com.wolfd.citygen.util;

import com.vividsolutions.jts.geom.Coordinate;

public class Turtle {
	public Coordinate pos = new Coordinate();
	public double angle;

	public Turtle(double x, double y, double angle) {
		this.pos.x = x;
		this.pos.y = y;
		this.angle = angle;
	}
	
	public Turtle(Coordinate pos, double angle) {
		this.pos = new Coordinate(pos);
		this.angle = angle;
	}
	
	public void turn(double angle){
		this.angle += angle;
		this.angle = this.angle%360;
	}

	public void move(double d){
		this.pos.x = (this.pos.x + (Math.cos(Math.toRadians(this.angle))*d));
		this.pos.y = (this.pos.y + (Math.sin(Math.toRadians(this.angle))*d));
	}

}
