package wolf.city;

public class TestMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int delta = 2;
		// 9 is really buggy currently
		//1234890
		City city = new City(1024*delta,1024*delta,System.currentTimeMillis());
		city.generateRoadmap();
	}

}
