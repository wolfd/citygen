package com.wolfd.citygen.city.road;

public enum RoadType {
HIGHWAY (16), STREET (7), DEFAULT (1), BRIDGE(14), MAIN(11);

private final int width;
RoadType(int width){
	this.width = width;
}
public int getWidth() {
	return width;
}
}
