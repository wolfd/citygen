package wolf.city.buildings;


import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import wolf.city.City;
import wolf.city.block.CityBlock;
import wolf.city.block.Lot;
import wolf.util.TextFileOutput;
import static wolf.util.STL.*;


public class FakeBuildings {
	private static final double MIN_HEIGHT = 4;
	private static final double MAX_HEIGHT = 200;
	private static final double MIN_AREA = 100;
	private static final double MIN_RATIO_BUILDING = 3;
	private static final float MIN_POPULATION_BUILDING = .2f;
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
						boolean waterPresent = false;
						for(Coordinate cord: l.shape.getCoordinates()){
							if(c.water.get((int)cord.x, (int)cord.y)>c.rm.noWaterCutoffDensity) waterPresent = true;
						}
						if(!waterPresent){
							Coordinate center = l.shape.getCentroid().getCoordinate();
							float population = c.pop.get((int)center.x, (int)center.y);
							if(population>MIN_POPULATION_BUILDING){
								if(l.shape.distance(c.rm.shape)<4 && l.shape.getArea() > MIN_AREA){
									Geometry buildingShape;
									if(c.random.nextBoolean()){
										buildingShape = l.shape.buffer(-(int)(c.random.nextDouble()*4));
									}else{
										buildingShape = l.shape.buffer(-1);
									}
									double ratio = buildingShape.getArea()/buildingShape.getLength();
									if(ratio > MIN_RATIO_BUILDING){
										FakeBuilding building;
										if(c.random.nextDouble()>.9){
											building = new FakeBuilding(buildingShape, (int) Math.min((MIN_HEIGHT+((c.random.nextDouble()+1)*100*population)),MAX_HEIGHT), l);
										}else{
											building = new FakeBuilding(buildingShape, (int) Math.min(MIN_HEIGHT+(MAX_HEIGHT*Math.pow(population,2)),MAX_HEIGHT), l);
										}
										buildings.add(building);
										l.building = building;
									}
								}
							}
						}
					}
				}
			}
		}
		
	}

	public void saveSTL(){
		c.log.log("Saving STL");
		if(buildings.size() > 0){
			TextFileOutput tf = new TextFileOutput();
			tf.data.add("solid buildings\n"); //file header

			for(int i=0; i<buildings.size(); i++){
				FakeBuilding b = buildings.get(i);
				//print status to console
				if(i%20==0){
					System.out.println((float)i/(float)buildings.size()*100f+"% Complete");
				}
				tf.data.add(b.toSTL());
			}
			//end file
			tf.data.add("endsolid buildings\n");

			tf.save("data/buildings.stl");
		}
	}

}
