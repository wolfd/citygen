package wolf.city;


import java.util.ArrayList;

import wolf.city.block.CityBlock;
import wolf.city.block.Lot;


public class FakeBuildings {
	private static final double MIN_HEIGHT = 16;
	private static final double MAX_HEIGHT = 200;
	private static final double MIN_AREA = 100;
	City c;
	public ArrayList<FakeBuilding> buildings;
	public FakeBuildings(City city) {
		c = city;
		buildings = new ArrayList<FakeBuilding>();
	}
	
	public void generate(){
		if(c.bm.blocks != null && c.bm.blocks.size() > 0){
			for(CityBlock b : c.bm.blocks){
				if(b.lots != null && b.lots.size()>0){
					for(Lot l: b.lots){
						if(l.shape.distance(c.rm.shape)<4 && l.shape.getArea() > MIN_AREA){
							buildings.add(new FakeBuilding(l.shape.buffer(-1), (int) Math.min((MIN_HEIGHT+((c.random.nextDouble()+1)*100*c.pop.get((int)l.shape.getCentroid().getCoordinate().x, (int)l.shape.getCentroid().getCoordinate().y))),MAX_HEIGHT)));
						}
					}
				}
			}
		}
	}

}
