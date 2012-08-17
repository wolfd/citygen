package wolf.city.buildings.style;

import java.util.ArrayList;


public class Pattern {
	public ArrayList<Pattern> children;
	public Style style;
	public float value; //meters or relative width
	public PatternType type;
	
	public enum PatternType {
		FIXED, RELATIVE
	}
	
	public enum OverflowBehavior {
		REMOVECENTER, REMOVEEDGES, SHRINK
	}
	
	public Pattern(PatternType pt, float val){
		type = pt;
		value = val;
		children = new ArrayList<Pattern>();
	}
	
	public Pattern(PatternType pt, float val, Pattern[] pats){
		this(pt, val);
		for(Pattern p: pats){
			children.add(p);
		}
	}
	
	public float[] getChildPattern(float size, OverflowBehavior ob){
		float[] ret = new float[children.size()];
		float relativeTotal = 0;
		for(int i=0; i<children.size(); i++){
			Pattern p = children.get(i);
			if(p.type == PatternType.RELATIVE) relativeTotal+=p.value;
		}
		float overflowSize = 0;
		for(int i=0; i<children.size(); i++){
			Pattern p = children.get(i);
			if(p.type == PatternType.RELATIVE){ 
				float addSize = p.value/relativeTotal*size;
				ret[i] = addSize;
				overflowSize += addSize;
			}else{
				ret[i] = p.value;
				overflowSize += p.value;
			}
		}
		if(overflowSize > size){
			switch(ob){
			case REMOVECENTER:
				//TODO REMOVECENTER algorithm
				break;
			case REMOVEEDGES:
				overflowSize -= size;
				//forward
				float shrinkSize = overflowSize/2;
				for(int i=0; i<ret.length; i++){
					if(ret[i] < shrinkSize){
						ret[i] -= shrinkSize;
						break;
					}else{
						shrinkSize -= ret[i];
						ret[i] = 0;
					}
				}
				//other direction
				shrinkSize = overflowSize/2;
				for(int i=ret.length-1; i>=0; i--){
					if(ret[i] < shrinkSize){
						ret[i] -= shrinkSize;
						break;
					}else{
						shrinkSize -= ret[i];
						ret[i] = 0;
					}
				}
				break;
			case SHRINK:
				overflowSize -= size;
				float shrinkLength = overflowSize/ret.length;
				for(int i=0; i<ret.length; i++){
					ret[i] -= shrinkLength;
				}
				break;
			}
		}
		return ret;
	}
	
}


