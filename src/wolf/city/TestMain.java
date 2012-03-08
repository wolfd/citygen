package wolf.city;

public class TestMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// 9 is really buggy currently
		City city = new City(2048,2048,1234890);//System.currentTimeMillis());
		city.generateRoadmap();

	}

}
