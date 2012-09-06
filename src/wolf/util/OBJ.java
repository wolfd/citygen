package wolf.util;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;

import com.vividsolutions.jts.geom.Coordinate;

public class OBJ {
	ArrayList<String> v;
	ArrayList<String> vt;
	ArrayList<String> vn;
	ArrayList<String> faces;
	
	public OBJ(){
		v = new ArrayList<String>();
		vt = new ArrayList<String>();
		vn = new ArrayList<String>();
		faces = new ArrayList<String>();
	}
	
	public void face(Vector3f[] verts){//, Vector3f[] vnorm, Vector3f[] vtex){
		String face = "f";
		Vector3f normal = normal(verts[0], verts[1], verts[2]);
		vn.add("vn "+normal.x+" "+normal.y+" "+normal.z);
		for(int i=0; i<verts.length; i++){
			v.add("v "+verts[i].x+" "+verts[i].y+" "+verts[i].z);
			face += " "+v.size()+"/"+vn.size()+"/";
		}
		faces.add(face);
	}
	
	private static Vector3f normal(Vector3f a, Vector3f b, Vector3f c){
		Vector3f u = new Vector3f((float)(b.x-a.x), (float)(b.y-a.y), (float)(b.z-a.z));
		Vector3f v = new Vector3f((float)(c.x-a.x), (float)(c.y-a.y), (float)(c.z-a.z));
		
		return normalize(new Vector3f((u.y*u.z)-(u.z*v.y), ((u.z*v.x)-(u.x*v.z)), ((u.x*v.y)-(u.y*v.x))));
	}

	private static Vector3f normalize(Vector3f v){
		float length = (float)Math.sqrt((v.x*v.x)+(v.y*v.y)+(v.z*v.z));
		v.x /= length;
		v.y /= length;
		v.z /= length;
		return v;
	}
	
	public void save(String filename){
		TextFileOutput tf = new TextFileOutput();
		tf.data.addAll(v);
		tf.data.addAll(vt);
		tf.data.addAll(vn);
		tf.data.addAll(faces);
		
		tf.save(filename);
	}
}
