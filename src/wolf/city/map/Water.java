package wolf.city.map;

import wolf.city.City;

public class Water extends ModifiableMap {

	
	public Water(City city) {
		super(city, false, 1);
		// TODO Auto-generated constructor stub
	}

	public Terrain erodeTerrain(Terrain ter){
		//function to erode terrain 
		// TODO make an algorithm to erode terrain with water
		return ter;
		
	}
	

}
