package wolf.city.buildings.shape;

import java.util.ArrayList;

import wolf.city.buildings.Floor;

public class Wall extends WallShape{
	public float length;
	public Floor floor;
	public ArrayList<WallShape> children;
	
	/*
	 * -specified # of sections
	 * -specified width of section (section size alters to fill remainder left by length/specified) 
	 * -pattern directions: forward,  backward, to center, from center. 
	 * -fill pattern (pattern object)
	 * -fill pattern until remainder + numpatterns 
	 * -split into patterned sections by use of pattern. 
	 * -ratio vs absolute length patterns
	 */
	
	public ArrayList<WallSection> split(int numSections){
		float sectionLength = length/(float)numSections;
		ArrayList<WallSection> sections = new ArrayList<WallSection>();
		for(int i=0; i<numSections; i++){
			sections.add(new WallSection(this, sectionLength));
		}
		
		return sections;
	}
	
	public ArrayList<WallSection> split(float specifiedWidth, SplitMethod method){
		int numSections = (int)(length/specifiedWidth);
		float remainder = length%specifiedWidth;
		ArrayList<WallSection> sections = new ArrayList<WallSection>();
		float width = 0;
		
		if(method == SplitMethod.EXPAND){
			width = specifiedWidth+(remainder/(float)numSections);
		}else if(method == SplitMethod.CONTRACT){
			width = specifiedWidth-(remainder/(float)numSections);
			numSections++;
		}
		
		for(int i=0; i<numSections; i++){
			sections.add(new WallSection(this, width));
		}
		
		return sections;
	}
	
	public ArrayList<WallSection> split(float specifiedWidth, SplitDirection direction){
		int numSections = (int)(length/specifiedWidth);
		float remainder = length%specifiedWidth;
		ArrayList<WallSection> sections = new ArrayList<WallSection>();
		
		switch(direction){
		case LEFT:
			for(int i=0; i<numSections; i++){
				sections.add(new WallSection(this, specifiedWidth));
			}
			sections.add(new WallSection(this, remainder));
			break;
		case RIGHT:
			sections.add(new WallSection(this, remainder));
			for(int i=0; i<numSections; i++){
				sections.add(new WallSection(this, specifiedWidth));
			}
			break;
		case TOCENTER:
			if(numSections%2 == 1){
				numSections--;
				//add another strange section to the center
				remainder += specifiedWidth; //combine
				remainder /= 2; //divide section into two
				
				//add beginning
				for(int i=0; i<numSections/2; i++){
					sections.add(new WallSection(this, specifiedWidth));
				}
				//add middle 2
				sections.add(new WallSection(this, remainder));
				sections.add(new WallSection(this, remainder));
				//add rest
				for(int i=0; i<numSections/2; i++){
					sections.add(new WallSection(this, specifiedWidth));
				}
			}else{
				//add beginning
				for(int i=0; i<numSections/2; i++){
					sections.add(new WallSection(this, specifiedWidth));
				}
				//add middle
				sections.add(new WallSection(this, remainder));
				//add rest
				for(int i=0; i<numSections/2; i++){
					sections.add(new WallSection(this, specifiedWidth));
				}
			}
			break;
		case FROMCENTER:
			sections.add(new WallSection(this, remainder/2)); //split remainder into two
			//add center
			for(int i=0; i<numSections; i++){
				sections.add(new WallSection(this, specifiedWidth));
			}
			sections.add(new WallSection(this, remainder/2));
			break;
		}
		
		return sections;
	}
	
}
