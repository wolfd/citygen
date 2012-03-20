package wolf.city;

import org.lwjgl.LWJGLException;

import wolf.util.LCTest;

public class LCTestMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			LCTest disp = new LCTest();
			while(disp.running){
				disp.renderFrame();
			}
		} catch (LWJGLException e) {
			e.printStackTrace();
		}

	}

}
