package wolf.city;

public class TestMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// 9 is really buggy currently
		//1234890
		City city = new City(1024*4,1024*4,System.currentTimeMillis());
		city.generateRoadmap();

	}

}
