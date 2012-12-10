package wolf.city.map;

import org.lwjgl.util.vector.Vector3f;

import wolf.city.City;
import wolf.util.OBJ;
import wolf.util.OBJOutput;

public class Terrain extends InputMap implements OBJOutput{

	private static final float APX_MAX_HEIGHT = 25;
	private static final int MESH_GRID_SIZE = 4;

	public Terrain(City city) {
		super(city, false, 3, .25);
	}
	
	public float get(int x, int y) {
		return super.get(x, y) * APX_MAX_HEIGHT;
	}

	@Override
	public void asOBJ(OBJ obj) {
		obj.startObject("terrain_"+this.hashCode());
		
		
		for(int ix=-sizeX/2; ix<sizeX/2-MESH_GRID_SIZE; ix=ix+MESH_GRID_SIZE){
			for(int iy=-sizeX/2; iy<sizeY/2-MESH_GRID_SIZE; iy=iy+MESH_GRID_SIZE){
				obj.face(
					new Vector3f[]{new Vector3f((float)ix, (float)iy, get(ix, iy)),
					new Vector3f((float)(ix+MESH_GRID_SIZE), (float)iy, get(ix+MESH_GRID_SIZE, iy)),
					new Vector3f((float)(ix+MESH_GRID_SIZE), (float)(iy+MESH_GRID_SIZE), get(ix+MESH_GRID_SIZE, iy+MESH_GRID_SIZE)),
					new Vector3f((float)ix, (float)(iy+MESH_GRID_SIZE), get(ix, iy+MESH_GRID_SIZE))});
			}
		}
		
		for(int ix=-sizeX/2; ix<sizeX/2-MESH_GRID_SIZE; ix=ix+MESH_GRID_SIZE){
			obj.face(new Vector3f[]{
					new Vector3f((float)ix+MESH_GRID_SIZE, -sizeY/2, get(ix+MESH_GRID_SIZE, -sizeY/2)),
					new Vector3f((float)ix, -sizeY/2, get(ix, -sizeY/2)),
					new Vector3f((float)ix, -sizeY/2, 0),
					new Vector3f((float)ix+MESH_GRID_SIZE, -sizeY/2, 0)
			});
			
			obj.face(new Vector3f[]{
					new Vector3f((float)ix+MESH_GRID_SIZE, sizeY/2-MESH_GRID_SIZE, 0),
					new Vector3f((float)ix, sizeY/2-MESH_GRID_SIZE, 0),
					new Vector3f((float)ix, sizeY/2-MESH_GRID_SIZE, get(ix, sizeY/2-MESH_GRID_SIZE)),
					new Vector3f((float)ix+MESH_GRID_SIZE, sizeY/2-MESH_GRID_SIZE, get(ix+MESH_GRID_SIZE, sizeY/2-MESH_GRID_SIZE))
			});
		}
		
		for(int iy=-sizeY/2; iy<sizeY/2-MESH_GRID_SIZE; iy=iy+MESH_GRID_SIZE){
			obj.face(new Vector3f[]{
					new Vector3f(-sizeX/2, (float)iy+MESH_GRID_SIZE, 0),
					new Vector3f(-sizeX/2, (float)iy, 0),
					new Vector3f(-sizeX/2, (float)iy, get(-sizeX/2, iy)),
					new Vector3f(-sizeX/2, (float)iy+MESH_GRID_SIZE, get(-sizeX/2, iy+MESH_GRID_SIZE))
			});
			
			obj.face(new Vector3f[]{
					new Vector3f(sizeX/2-MESH_GRID_SIZE, (float)iy+MESH_GRID_SIZE, get(sizeX/2-MESH_GRID_SIZE, iy+MESH_GRID_SIZE)),
					new Vector3f(sizeX/2-MESH_GRID_SIZE, (float)iy, get(sizeX/2-MESH_GRID_SIZE, iy)),
					new Vector3f(sizeX/2-MESH_GRID_SIZE, (float)iy, 0),
					new Vector3f(sizeX/2-MESH_GRID_SIZE, (float)iy+MESH_GRID_SIZE, 0),
			});
		}
		
		obj.face(new Vector3f[]{
				new Vector3f(sizeX/2-MESH_GRID_SIZE, -sizeY/2, 0),
				new Vector3f(sizeX/2-MESH_GRID_SIZE, sizeY/2-MESH_GRID_SIZE, 0),
				new Vector3f(-sizeX/2, sizeY/2-MESH_GRID_SIZE, 0),
				new Vector3f(-sizeX/2, -sizeY/2, 0),
		});
		
		
		obj.endObject();
	}

}
