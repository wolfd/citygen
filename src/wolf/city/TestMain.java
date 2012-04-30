package wolf.city;

import wolf.gui.engine.Camera;

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
		
		//1335539797929l
		final City city = new City(1024*delta,1024*delta,System.currentTimeMillis());
		final Camera cam = new Camera();
		//cam.render(city);
//		TimerTask tt = new TimerTask(){
//			public void run(){
//				cam.render(city);
//			}
//		};
//		
//		Timer t = new Timer();
//		
//		t.schedule(tt, 1000, 1000/60);
		
		city.generateRoadmap();
		while(true){
			cam.render(city);
		}
		//System.exit(0);
	}

}
