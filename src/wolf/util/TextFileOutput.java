package wolf.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

public class TextFileOutput {
	public ArrayList<String> data;
	
	public TextFileOutput(){
		data = new ArrayList<String>();
	}
	
	public boolean save(String filename){

		File f = new File(filename);
		f.getParentFile().mkdirs();
		try {
			f.createNewFile();

			System.out.println("Saving file to "+f.getAbsolutePath());
			if(f.canWrite()){

				try {
					FileOutputStream out = new FileOutputStream(f);
					PrintStream ps = new PrintStream(out);

					for(int i=0; i<data.size(); i++){
						ps.println(data.get(i));
					}

					ps.close();
					out.close();

					return true;

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}



		return false;
	}
}
