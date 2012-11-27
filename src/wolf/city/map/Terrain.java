package wolf.city.map;

import org.lwjgl.util.vector.Vector3f;

import com.vividsolutions.jts.geom.Coordinate;

import wolf.city.City;
import wolf.util.OBJ;
import wolf.util.OBJOutput;

public class Terrain extends InputMap implements OBJOutput{

	private static final float APX_MAX_HEIGHT = 25;

	public Terrain(City city) {
		super(city, false);
	}
	
	public float get(int x, int y) {
		return super.get(x, y) * APX_MAX_HEIGHT;
	}

	@Override
	public void asOBJ(OBJ obj) {
		obj.startObject("terrain_"+this.hashCode());
		
		
		for(int ix=-sizeX/2; ix<sizeX/2; ix=ix+8){
			for(int iy=-sizeX/2; iy<sizeY/2; iy=iy+8){
				obj.face(
					new Vector3f[]{new Vector3f((float)ix,(float)iy, get(ix, iy)),
					new Vector3f((float)(ix+1),(float)iy, get(ix+1, iy)),
					new Vector3f((float)(ix+1),(float)(iy+1), get(ix+1, iy+1)),
					new Vector3f((float)ix,(float)(iy+1), get(ix, iy+1))});
			}
		}
		
		obj.endObject();
	}

}
