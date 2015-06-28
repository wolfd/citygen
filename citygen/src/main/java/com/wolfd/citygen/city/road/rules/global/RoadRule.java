package com.wolfd.citygen.city.road.rules.global;

import com.wolfd.citygen.city.City;
import com.wolfd.citygen.city.road.Road;

public interface RoadRule {
	public Road globalGoals(City city, Road road, Direction d); //0 - left, 1 - forward, 2 - right, 3 - backward
	public RoadRule mutate();
	public City getCity();
}
