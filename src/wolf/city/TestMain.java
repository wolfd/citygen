package wolf.city;

import wolf.gui.Camera;

public class TestMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double delta = 8;
		// 9 is really buggy currently
		//1234890
		//1335279392788l is a cool seed currently (delta 2) 1335967536290
		
		/*CADD Presentation seeds
		 * 1335319695441
		 * 1335319857501
		 * 1335321126626
		 * 1335321717775
		 * 
		 */
		
		//1337268014460 small city (delta 1)
		
		//1335539797929l
		final City city = new City((int)(1024*delta),(int)(1024*delta),System.currentTimeMillis());
		
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
		final Camera cam = new Camera();
		while(true){
			cam.render(city);
		}
		//System.exit(0);
	}

}
