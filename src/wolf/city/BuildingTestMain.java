package wolf.city;

import com.vividsolutions.jts.io.ParseException;

import wolf.city.buildings.Building;
import wolf.gui.BuildingView;

public class BuildingTestMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String geomString = "POLYGON((-1944 1909 0, -1944 1884 0, -1966 1784 0, -1966 1909 0, -1944 1909 0))";
		try {
			BuildingView bv = new BuildingView(new Building(geomString));
			while(bv.draw());
		} catch (ParseException e) {
			e.printStackTrace();
			System.err.println("Polygon not correctly formed!");
		}

	}

}
