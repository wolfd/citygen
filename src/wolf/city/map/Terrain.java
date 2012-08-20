package wolf.city.map;

import wolf.city.City;

public class Terrain extends InputMap {

	private static final float APX_MAX_HEIGHT = 25;

	public Terrain(City city) {
		super(city, false);
	}
	
	public float get(int x, int y) {
		return super.get(x, y) * APX_MAX_HEIGHT;
	}


}
