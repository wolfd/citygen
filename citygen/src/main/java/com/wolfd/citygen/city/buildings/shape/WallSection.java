package com.wolfd.citygen.city.buildings.shape;

import com.wolfd.citygen.city.buildings.style.Pattern;

public class WallSection extends Wall{
	public Wall parent;
	
	public WallSection(Wall wall, float sectionLength) {
		super(wall.floor, sectionLength);
		parent = wall;
	}

	public WallSection(Wall wall, float length, Pattern pat) {
		this(wall, length);
		float[] lengths = pat.getChildPattern(length, Pattern.OverflowBehavior.SHRINK);
		for(int i=0; i<pat.children.size(); i++){
			Pattern child = pat.children.get(i);
			if(lengths[i] != 0){
				WallSection ws = new WallSection(this, lengths[i], child);
				children.add(ws);
			}
		}
	}
}
