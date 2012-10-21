package wolf.city;

import wolf.gui.Camera;

public class TestMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double delta = 4;

		final City city = new City((int)(1024*delta),(int)(1024*delta),1348607080077l);//System.currentTimeMillis());
		
		city.generateRoadmap(true);
		final Camera cam = new Camera(city);
		while(true){
			cam.render();
		}
	}

}
