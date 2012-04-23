package wolf.city;

public class TestMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int delta = 4;
		boolean notSatisfied = true;
		// 9 is really buggy currently
		//1234890
		while(notSatisfied){
		City city = new City(1024*delta,1024*delta,System.currentTimeMillis());
		city.generateRoadmap();
		if(city.rm.roads.size()>25*delta){
			notSatisfied = false;
		}
		}

	}

}
