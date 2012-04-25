package wolf.city;

public class TestMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int delta = 2;
		// 9 is really buggy currently
		//1234890
		//1335279392788l is a cool seed currently (delta 2)
		
		/*CADD Presentation seeds
		 * 1335319695441
		 * 1335319857501
		 * 1335321126626
		 * 1335321717775
		 */
		City city = new City(1024*delta,1024*delta,9);//System.currentTimeMillis());
		city.generateRoadmap();
		
		System.exit(0);
	}

}
