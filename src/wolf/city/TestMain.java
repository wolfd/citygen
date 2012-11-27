package wolf.city;

import wolf.gui.Camera;

public class TestMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double delta = 2;

		final City city = new City((int)(1024*delta),(int)(1024*delta),System.currentTimeMillis());
		
		city.generateRoadmap(true);
		final Camera cam = new Camera(city);
		while(true){
			cam.render();
		}
	}

}
