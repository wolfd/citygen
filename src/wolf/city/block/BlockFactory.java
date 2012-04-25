package wolf.city.block;

import java.util.LinkedList;
import java.util.List;

import wolf.city.City;
import wolf.city.road.Road;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.util.PolygonExtracter;

public class BlockFactory {
	private static final int e = -16;

	public static LinkedList<CityBlock> makeBlocks(City c, List<Road> roads){
		Geometry[] geoms = new Geometry[roads.size()];
		for(int i=0; i<roads.size(); i++){
			geoms[i] = (roads.get(i).getFinalGeometry());
		}
		GeometryFactory gf = new GeometryFactory();
		GeometryCollection polygonCollection = gf.createGeometryCollection(geoms);
		//union all of the road geometries
		Geometry union = polygonCollection.buffer(0);
		//make a giant square the size of the city
		Geometry cityTemplateSquare = gf.createPolygon(gf.createLinearRing(new Coordinate[]{new Coordinate(-c.sizeX/2-e,c.sizeY/2+e), new Coordinate(c.sizeX/2+e,c.sizeY/2+e), new Coordinate(c.sizeX/2+e,-c.sizeY/2-e), new Coordinate(-c.sizeX/2-e,-c.sizeY/2-e), new Coordinate(-c.sizeX/2-e,c.sizeY/2+e)}),null);
		//subtract the union from the square to find the blocks
		Geometry difference = cityTemplateSquare.difference(union);
		LinkedList<CityBlock> blocks = new LinkedList<CityBlock>();
		@SuppressWarnings("unchecked")
		List<Polygon> blockPolys = PolygonExtracter.getPolygons(difference);
		for(Polygon p:blockPolys){
			blocks.add(new CityBlock(p));
		}
		return blocks;
	}
}
