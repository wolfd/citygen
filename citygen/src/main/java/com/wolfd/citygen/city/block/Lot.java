package com.wolfd.citygen.city.block;

import com.wolfd.citygen.city.City;
import com.wolfd.citygen.city.buildings.FakeBuilding;
import com.wolfd.citygen.util.OBJ;
import com.wolfd.citygen.util.OBJOutput;
import org.lwjgl.util.vector.Vector3f;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;

public class Lot implements OBJOutput {
	public Polygon shape;
	public FakeBuilding building;
	private City c;

	public Lot(City c, Polygon f) {
		this.c = c;
		shape = f;
	}

	public String toString(){
		return shape.toString();
	}

	@Override
	public void asOBJ(OBJ obj) {
		obj.startObject("lot_"+this.hashCode());

		//convert Coordinate to Vector3f
		Coordinate[] cs = shape.getCoordinates();
		Vector3f[] verts = new Vector3f[cs.length];
		Vector3f[] vertsz = new Vector3f[cs.length];

		
//		float elevation = 0;
//		if(building != null){
//			elevation = (float) building.zOffset;
//		}else{
//			Point p = shape.getCentroid();
//			elevation = c.ter.get((int)p.getX(), (int)p.getY());
//		}

		for(int i=0; i<cs.length; i++){
			verts[cs.length-i-1] = new Vector3f((float)cs[i].x,(float)cs[i].y, c.ter.get((int)cs[i].x, (int)cs[i].y));
			vertsz[i] = new Vector3f((float)cs[i].x,(float)cs[i].y, 0);
		}

		for(int i=0; i<cs.length; i++){
			//obj.face(new Vector3f[]{new Vector3f(verts[i].x, verts[i].y, 0), new Vector3f(verts[(i+1)%cs.length].x, verts[(i+1)%cs.length].y, 0), verts[(i+1)%cs.length], verts[i]});


			obj.face(new Vector3f[]{new Vector3f(verts[i].x, verts[i].y, 0), new Vector3f(verts[(i+1)%cs.length].x, verts[(i+1)%cs.length].y, 0), verts[i]});
			obj.face(new Vector3f[]{new Vector3f(verts[(i+1)%cs.length].x, verts[(i+1)%cs.length].y, 0), verts[(i+1)%cs.length], verts[i]});

		}

		obj.face(verts);
		obj.face(vertsz);
		obj.endObject();
	}


}
