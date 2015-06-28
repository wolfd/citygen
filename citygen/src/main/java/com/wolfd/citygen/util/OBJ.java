package com.wolfd.citygen.util;

import java.util.ArrayList;

import org.lwjgl.util.vector.Vector3f;

public class OBJ {
	private ArrayList<String> v;
	private int vCount;
	private ArrayList<String> vt;
	private int vtCount;
	private ArrayList<String> vn;
	private int vnCount;
	private ArrayList<String> faces;
	private boolean saveForEachObject;
	
	private TextFileOutput tf = new TextFileOutput();
	private String currentObjectName;
	
	public OBJ(boolean saveForEachObject){
		v = new ArrayList<String>();
		vt = new ArrayList<String>();
		vn = new ArrayList<String>();
		faces = new ArrayList<String>();
		vCount = 0;
		vtCount = 0;
		vnCount = 0;
		this.saveForEachObject = saveForEachObject;
	}
	
	public void startObject(String name){
		tf.data.add("o "+name);
		currentObjectName = name;
	}
	
	public void endObject(){
		tf.data.addAll(v);
		tf.data.addAll(vt);
		tf.data.addAll(vn);
		tf.data.addAll(faces);
		v.clear();
		vt.clear();
		vn.clear();
		faces.clear();
		
		
		if(saveForEachObject){ 
			tf.save("data/objects/"+currentObjectName+".obj");
			tf.data.clear();
			vCount = 0;
			vtCount = 0;
			vnCount = 0;
		}
		//add material data?
	}
	
	public void face(Vector3f[] verts){//, Vector3f[] vnorm, Vector3f[] vtex){
		String face = "f";
		//Vector3f normal = normal(verts[0], verts[1], verts[2]);
		//vn.add("vn "+normal.x+" "+normal.y+" "+normal.z);
		//vnCount++;
		for(int i=0; i<verts.length; i++){
			v.add("v "+verts[i].x+" "+verts[i].y+" "+verts[i].z);
			vCount++; //use a static counter because array gets reset for every object
			//face += " "+vCount+"/"+vnCount+"/";
			face += " "+vCount+"//";
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
		tf.save(filename);
	}
}
