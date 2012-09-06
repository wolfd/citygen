package wolf.city.buildings;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;

import wolf.city.block.Lot;
import wolf.util.OBJ;
import wolf.util.OBJOutput;
import wolf.util.STLOutput;

import static wolf.util.STL.*;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.triangulate.DelaunayTriangulationBuilder;


public class FakeBuilding implements STLOutput, OBJOutput{
	private static GeometryFactory gf = new GeometryFactory();
	public Geometry g;
	public int height;
	public double zOffset;
	public Lot lot;

	public FakeBuilding(Geometry geometry, int h, double z, Lot l){
		height = h;
		zOffset = z;
		g = geometry;
		lot = l;
		
		g.apply(new CoordinateSequenceFilter() {
            boolean done = false;

            public boolean isGeometryChanged() {
                return true;
            }

            public boolean isDone() {
                return done;
            }

			@Override
			public void filter(CoordinateSequence seq, int index) {
				seq.setOrdinate(index, 2, zOffset);
			}

        });
		
		g.geometryChanged();
	}

	public String toSTL(){
		String stl = "";
		Coordinate[] cs = g.getCoordinates();
//		for(int i=0; i<cs.length; i++){
//			cs[i].z = zOffset;
//		}
		if(cs.length >= 3){
			

			Coordinate c1 = cs[cs.length-1];
			for(int i=0; i<cs.length; i++){
				Coordinate c2 = cs[i];
				//make 2 triangles
				Coordinate ch1 = new Coordinate(c1);
				if(Double.isNaN(ch1.z)) ch1.z = 0;
				ch1.z += height;
				Coordinate ch2 = new Coordinate(c2);
				if(Double.isNaN(ch2.z)) ch2.z = 0;
				ch2.z += height;
				//first triangle
				
				stl += normal(c1, c2, ch1);
				stl += "outer loop\n";
				stl += vertex(c1);
				stl += vertex(c2);
				stl += vertex(ch1);
				stl += "endloop\n";
				stl += "endfacet\n";
				//second triangle
				stl += normal(c2, ch2, ch1);
				stl += "outer loop\n";
				stl += vertex(c2);
				stl += vertex(ch2);
				stl += vertex(ch1);
				stl += "endloop\n";
				stl += "endfacet\n";
				c1 = c2;
			}
			ArrayList<Geometry> gBase = toTriangles(g);
			for(Geometry tri: gBase){
				//base
				stl += normal(tri.getCoordinates()[0],tri.getCoordinates()[1],tri.getCoordinates()[2]);
				stl += "outer loop\n";
				stl += vertex(tri.getCoordinates()[0]);
				stl += vertex(tri.getCoordinates()[1]);
				stl += vertex(tri.getCoordinates()[2]);
				stl += "endloop\n";
				stl += "endfacet\n";
				
				//top
				Coordinate a = new Coordinate(tri.getCoordinates()[2]);
				Coordinate b = new Coordinate(tri.getCoordinates()[1]);
				Coordinate c = new Coordinate(tri.getCoordinates()[0]);
				a.z += height;
				b.z += height;
				c.z += height;
				
				stl += normal(a,b,c);
				stl += "outer loop\n";
				stl += vertex(a);
				stl += vertex(b);
				stl += vertex(c);
				stl += "endloop\n";
				stl += "endfacet\n";
			}
		}
		return stl;

	}

	private ArrayList<Geometry> toTriangles(Geometry g){
		ArrayList<Geometry> valid = new ArrayList<Geometry>();
		DelaunayTriangulationBuilder triator = new DelaunayTriangulationBuilder();
		triator.setSites(g);
		Geometry tris = triator.getTriangles(gf);
		for(int i=0; i<tris.getNumGeometries(); i++){
			if(g.contains(tris.getGeometryN(i))) valid.add(tris.getGeometryN(i));
		}
		return valid;
	}

	@Override
	public void asOBJ(OBJ objfile) {
		Coordinate[] cs = g.getCoordinates();
		if(cs.length >= 3){
			Coordinate c1 = cs[cs.length-1];
			for(int i=0; i<cs.length; i++){
				Coordinate c2 = cs[i];
				//make 2 triangles
				Coordinate ch1 = new Coordinate(c1);
				if(Double.isNaN(ch1.z)) ch1.z = 0;
				ch1.z += height;
				Coordinate ch2 = new Coordinate(c2);
				if(Double.isNaN(ch2.z)) ch2.z = 0;
				ch2.z += height;
				//first triangle
				objfile.face(new Vector3f[]{toVector3f(ch1), toVector3f(c2), toVector3f(c1)});
				//second triangle
				objfile.face(new Vector3f[]{toVector3f(ch1), toVector3f(ch2), toVector3f(c2)});
				c1 = c2;
			}
			ArrayList<Geometry> gBase = toTriangles(g);
			for(Geometry tri: gBase){
				//base
				objfile.face(new Vector3f[]{toVector3f(tri.getCoordinates()[2]), toVector3f(tri.getCoordinates()[1]), toVector3f(tri.getCoordinates()[0])});
				//top
				Coordinate a = new Coordinate(tri.getCoordinates()[0]);
				Coordinate b = new Coordinate(tri.getCoordinates()[1]);
				Coordinate c = new Coordinate(tri.getCoordinates()[2]);
				if(Double.isNaN(a.z)) a.z = 0;
				if(Double.isNaN(b.z)) b.z = 0;
				if(Double.isNaN(c.z)) c.z = 0;
				a.z += height;
				b.z += height;
				c.z += height;
				
				objfile.face(new Vector3f[]{toVector3f(a), toVector3f(b), toVector3f(c)});
			}
		}
	}

	private Vector3f toVector3f(Coordinate c) {
		return new Vector3f(c.x == Double.NaN ? 0 : (float)c.x, c.y == Double.NaN ? 0 : (float)c.y, c.z == Double.NaN ? 0 : (float)c.z);
	}
	

}
