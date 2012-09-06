package wolf.city.block;

import java.util.LinkedList;
import java.util.List;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.util.PolygonExtracter;

import wolf.city.City;

public class LotFactory {
	private static GeometryFactory gf = new GeometryFactory(new PrecisionModel(PrecisionModel.FIXED));
	private final static double SMALL_LOT_SIZE = 800;
	private static final double LARGE_LOT_SIZE = 1100;
	private static final double DISTANCE_DELTA = 1;

	public static void makeLots(City c, List<CityBlock> blocks){
		long startTime = System.currentTimeMillis();
		for(int blockIndex=0; blockIndex < blocks.size(); blockIndex++){
			CityBlock block = blocks.get(blockIndex);
			
			if(block.shape.getArea() > SMALL_LOT_SIZE){
				//make a stack of polygons
				List<Polygon> stack = new LinkedList<Polygon>();

				stack.add(block.shape);
				//make a "finished" list
				List<Polygon> finished = new LinkedList<Polygon>();
				//while there are still polygons to be divided
				while(stack.size()>0){
					//get the top element from the stack (and remove it)
					Polygon curStack = stack.remove(stack.size()-1);
					//get the longest edge in the polygon
					int largestIndex = -1;
					double largestSize = 0;
					for(int i=0; i<curStack.getCoordinates().length; i++){

						LineString tempLine;
						if(i==curStack.getCoordinates().length-1){
							tempLine = gf.createLineString(new Coordinate[]{curStack.getCoordinates()[i], curStack.getCoordinates()[0]});

						}else{
							tempLine = gf.createLineString(new Coordinate[]{curStack.getCoordinates()[i], curStack.getCoordinates()[i+1]});
						}

						double length = tempLine.getLength();
						if(length>largestSize){
							largestIndex = i;
							largestSize = length;
						}
					}
					LineString tempLine;
					if(largestIndex==curStack.getCoordinates().length-1){
						tempLine = gf.createLineString(new Coordinate[]{curStack.getCoordinates()[largestIndex], curStack.getCoordinates()[0]});

					}else{
						tempLine = gf.createLineString(new Coordinate[]{curStack.getCoordinates()[largestIndex], curStack.getCoordinates()[largestIndex+1]});

					}
					//get the midpoint and the angle
					Point midpoint = tempLine.getCentroid();
					double angle = Angle.angle(tempLine.getCoordinates()[0], tempLine.getCoordinates()[1]);
					double length = (c.sizeX+c.sizeY)*2; //size of the box that cuts the lots, make larger if having problems
					//make a line from that point to a point perpendicular to the first (actually extended in both directions a long way)
//					LineString split = gf.createLineString(new Coordinate[]{new Coordinate(midpoint.getX()-Math.cos(angle+(Math.PI/2))*length, midpoint.getY()-Math.sin(angle+(Math.PI/2))*length,0), new Coordinate(midpoint.getX()+Math.cos(angle+(Math.PI/2))*length,midpoint.getY()+Math.sin(angle+(Math.PI/2))*length,0)});
//					LineString side1 = gf.createLineString(new Coordinate[]{new Coordinate(split.getCoordinates()[0].x-Math.cos(angle)*length, split.getCoordinates()[0].y-Math.sin(angle)*length,0), new Coordinate(split.getCoordinates()[1].x-Math.cos(angle)*length,split.getCoordinates()[1].y-Math.sin(angle)*length,0)});
//					LineString side2 = gf.createLineString(new Coordinate[]{new Coordinate(split.getCoordinates()[0].x+Math.cos(angle)*length, split.getCoordinates()[0].y+Math.sin(angle)*length,0), new Coordinate(split.getCoordinates()[1].x+Math.cos(angle)*length,split.getCoordinates()[1].y+Math.sin(angle)*length,0)});	
					double cosAngle = Math.cos(angle);
					double sinAngle = Math.sin(angle);
					double cosAngle2 = Math.cos(angle+(Math.PI/2));
					double sinAngle2 = Math.sin(angle+(Math.PI/2));
					
					LineString split = gf.createLineString(new Coordinate[]{new Coordinate(midpoint.getX()-cosAngle2*length, midpoint.getY()-sinAngle2*length,0), new Coordinate(midpoint.getX()+cosAngle2*length,midpoint.getY()+sinAngle2*length,0)});
					LineString side1 = gf.createLineString(new Coordinate[]{new Coordinate(split.getCoordinates()[0].x-cosAngle*length, split.getCoordinates()[0].y-sinAngle*length,0), new Coordinate(split.getCoordinates()[1].x-cosAngle*length,split.getCoordinates()[1].y-sinAngle*length,0)});
					LineString side2 = gf.createLineString(new Coordinate[]{new Coordinate(split.getCoordinates()[0].x+cosAngle*length, split.getCoordinates()[0].y+sinAngle*length,0), new Coordinate(split.getCoordinates()[1].x+cosAngle*length,split.getCoordinates()[1].y+sinAngle*length,0)});

					
					//create rings to make polygons
					LinearRing ring1 = gf.createLinearRing(new Coordinate[]{split.getCoordinates()[0],split.getCoordinates()[1],side1.getCoordinates()[1], side1.getCoordinates()[0], split.getCoordinates()[0]});
					LinearRing ring2 = gf.createLinearRing(new Coordinate[]{split.getCoordinates()[0],split.getCoordinates()[1],side2.getCoordinates()[1], side2.getCoordinates()[0], split.getCoordinates()[0]});
					//create polygons to split from
					Polygon split1 = gf.createPolygon(ring1, null);
					Polygon split2 = gf.createPolygon(ring2, null);
					//clip the polygon on one side, then clip it on the other (make sure clipper is outside the bounding box
					//validation for debugging
					if(!split1.isValid()){
						System.out.println("Problem with split1");
						System.out.println(split1.toText());
					}

					if(!split2.isValid()){
						System.out.println("Problem with split2");
						System.out.println(split2.toText());
					}
					Geometry g1 = null;
					Geometry g2 = null;
					try{
						g1 = curStack.intersection(split1);
						g2 = curStack.intersection(split2);
					}catch(TopologyException e){
						System.err.println("TopologyException: "+ e.getMessage());
					}finally {
						if(g1 != null && g2 != null){
							@SuppressWarnings("unchecked")
							List<Polygon> lot1Arr = PolygonExtracter.getPolygons(g1);
							@SuppressWarnings("unchecked")
							List<Polygon> lot2Arr = PolygonExtracter.getPolygons(g2);

							//get the size of each polygon, if smaller than break-off point add to finished

							for(int lot1Index=0; lot1Index<lot1Arr.size(); lot1Index++){
								Polygon lot1 = lot1Arr.get(lot1Index);
								Point ctr = lot1.getCentroid();
								double distanceFromCenter = Math.sqrt((ctr.getX()*ctr.getX())+(ctr.getY()*ctr.getY()));
								double cutOffSize;
								if(c.random.nextBoolean()){
									cutOffSize = Math.max(Math.min(distanceFromCenter,LARGE_LOT_SIZE)*DISTANCE_DELTA,SMALL_LOT_SIZE);
									cutOffSize = cutOffSize + cutOffSize*(c.random.nextDouble()/4);
								}else{
									cutOffSize = c.random.nextDouble()*(LARGE_LOT_SIZE - SMALL_LOT_SIZE)+SMALL_LOT_SIZE;
								}
								if(lot1.getArea()<cutOffSize){
									finished.add(lot1);
								}else{
									stack.add(lot1);
								}
							}
							for(int lot2Index=0; lot2Index<lot2Arr.size(); lot2Index++){
								Polygon lot2 = lot2Arr.get(lot2Index);
								Point ctr = lot2.getCentroid();
								double distanceFromCenter = Math.sqrt((ctr.getX()*ctr.getX())+(ctr.getY()*ctr.getY()));
								double cutOffSize;
								if(c.random.nextBoolean()){
									cutOffSize = Math.max(Math.min(distanceFromCenter,LARGE_LOT_SIZE)*DISTANCE_DELTA,SMALL_LOT_SIZE);
									cutOffSize = cutOffSize + cutOffSize*(c.random.nextDouble()/4);
								}else{
									cutOffSize = c.random.nextDouble()*(LARGE_LOT_SIZE - SMALL_LOT_SIZE)+SMALL_LOT_SIZE;
								}
								if(lot2.getArea()<cutOffSize){
									finished.add(lot2);
								}else{
									stack.add(lot2);
								}
							}
						}
					}
				}
				List<Lot> tempFinished = new LinkedList<Lot>();
				for(int fIndex=0; fIndex<finished.size(); fIndex++){
					tempFinished.add(new Lot(finished.get(fIndex)));
				}
				block.lots = tempFinished;
			}else{
				List<Lot> lots = new LinkedList<Lot>();
				lots.add(new Lot(block.shape));
				block.lots = lots;
			}
		}
		System.out.println("LotFactory took: "+(System.currentTimeMillis()-startTime)+"ms");
	}
	
}
