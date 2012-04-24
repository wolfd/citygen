package wolf.city.block;

import java.util.LinkedList;
import java.util.List;

import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.util.PolygonExtracter;

import wolf.city.City;

public class LotFactory {
	private static GeometryFactory gf = new GeometryFactory(new PrecisionModel(PrecisionModel.FIXED));
	private static double cutOffSize = 791;

	public static void makeLots(City c, List<CityBlock> blocks){
		LinkedList<Lot> fLots = new LinkedList<Lot>();
		for(CityBlock block: blocks){
			if(block.equals(blocks.get(0))){
				//this block is the exterior
			}else{
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
							tempLine = gf .createLineString(new Coordinate[]{curStack.getCoordinates()[i], curStack.getCoordinates()[0]});

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
					double length = 1500;//size of the box that cuts the lots, make larger if having problems
					//make a line from that point to a point perpendicular to the first (actually extended in both directions a long way)
					LineString split = gf.createLineString(new Coordinate[]{new Coordinate(midpoint.getX()-Math.cos(angle+(Math.PI/2))*length, midpoint.getY()-Math.sin(angle+(Math.PI/2))*length), new Coordinate(midpoint.getX()+Math.cos(angle+(Math.PI/2))*length,midpoint.getY()+Math.sin(angle+(Math.PI/2))*length)});
					LineString side1 = gf.createLineString(new Coordinate[]{new Coordinate(split.getCoordinates()[0].x-Math.cos(angle)*length, split.getCoordinates()[0].y-Math.sin(angle)*length), new Coordinate(split.getCoordinates()[1].x-Math.cos(angle)*length,split.getCoordinates()[1].y-Math.sin(angle)*length)});
					LineString side2 = gf.createLineString(new Coordinate[]{new Coordinate(split.getCoordinates()[0].x+Math.cos(angle)*length, split.getCoordinates()[0].y+Math.sin(angle)*length), new Coordinate(split.getCoordinates()[1].x+Math.cos(angle)*length,split.getCoordinates()[1].y+Math.sin(angle)*length)});

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
							if(lot1Arr.size()>=1){
								Polygon lot1 = lot1Arr.get(0);
								if(lot1.getArea()<cutOffSize ){
									finished.add(lot1);
								}else{
									stack.add(lot1);
								}
							}
							if(lot2Arr.size()>=1){
								Polygon lot2 = lot2Arr.get(0);
								if(lot2.getArea()<cutOffSize ){
									finished.add(lot2);
								}else{
									stack.add(lot2);
								}
							}
						}
					}
				}
				List<Lot> tempFinished = new LinkedList<Lot>();
				for(Polygon f: finished){
					tempFinished.add(new Lot(f));
				}
				block.lots = tempFinished;
			
				//System.out.println("Block's lots generated, number of lots: "+block.lots.size());

				//convert lots into polygon text
//				Polygon[] pa = new Polygon[block.lots.size()];
//				for(int i=0; i<block.lots.size(); i++){
//					pa[i] = block.lots.get(i).shape;
//				}
			}
			
		}
	}
}
