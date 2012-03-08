package wolf.city.road.rules;

import wolf.city.City;
import wolf.city.road.Road;

public interface RoadRule {

	public Road globalGoals(City city, Road road, Direction d); //0 - left, 1 - forward, 2 - right, 3 - backward
}
