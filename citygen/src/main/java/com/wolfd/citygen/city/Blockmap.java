package com.wolfd.citygen.city;

import java.util.LinkedList;

import com.wolfd.citygen.city.block.BlockFactory;
import com.wolfd.citygen.city.block.CityBlock;
import com.wolfd.citygen.city.block.Lot;
import com.wolfd.citygen.city.block.LotFactory;
import com.wolfd.citygen.util.TextFileOutput;

public class Blockmap {
	private City c;
	public LinkedList<CityBlock> blocks;

	public Blockmap(City city){
		c = city;
		blocks = new LinkedList<CityBlock>();
	}

	public void getBlocks(Roadmap rm){
		blocks = BlockFactory.makeBlocks(c, rm.roads.queryAll());
		LotFactory.makeLots(c, blocks);
	}

	public void save(String filepathBlocks, String filepathLots){
		{
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
			tf.save(filepathBlocks);
		}
		{
			TextFileOutput tf = new TextFileOutput();
			for(CityBlock b:blocks){
				if(b.lots != null){
					for(Lot i:b.lots){
						tf.data.add(i.shape.toText());
					}
				}
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
			tf.save(filepathLots);
		}
	}
}
