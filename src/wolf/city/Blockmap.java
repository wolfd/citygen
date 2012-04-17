package wolf.city;

import java.util.LinkedList;

import wolf.city.block.BlockFactory;
import wolf.city.block.CityBlock;
import wolf.util.TextFileOutput;

public class Blockmap {
	private City c;
	public LinkedList<CityBlock> blocks;
	
	public Blockmap(City city){
		c = city;
		blocks = new LinkedList<CityBlock>();
	}
	
	public void getBlocks(Roadmap rm){
		blocks = BlockFactory.makeBlocks(c, rm.roads);
	}
	
	public void save(String filepath){
		TextFileOutput tf = new TextFileOutput();
		for(CityBlock b:blocks){
			tf.data.add(b.shape.toText());
		}
		for(int i=0; i<tf.data.size(); i++){
			String s = tf.data.get(i);
			if(i!=0){
				s = s.replaceAll("POLYGON", ",");
			}else{
				s = s.replaceAll("POLYGON", "MULTIPOLYGON (");
			}
			tf.data.set(i, s);
		}
		tf.data.add(")");
		tf.save(filepath);
	}
}
