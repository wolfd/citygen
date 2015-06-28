package com.wolfd.citygen.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

public class Log {
	boolean verbose = true;
	
	public enum LogType{
		WARNING, INFO, CRITICAL, DEBUG;
	}

	ArrayList<String> data;
	ArrayList<LogType> type;

	public Log(){
		data = new ArrayList<String>();
		type = new ArrayList<LogType>();
	}

	public void log(String msg){
		LogType e = LogType.INFO;
		log(msg, e);
	}

	public void log(String msg, LogType e){
		type.add(e);
		data.add(msg);
		dispLog(e, msg);
	}

	private void dispLog(LogType e, String msg){
		if(verbose){
			System.out.println("["+e.toString()+"] "+msg);
		}else if(e == LogType.CRITICAL || e == LogType.WARNING){
			System.out.println("["+e.toString()+"] "+msg);
		}
	}

	public boolean save(String filename){
		
		File f = new File("logs/"+filename);
		f.getParentFile().mkdirs();
		try {
			f.createNewFile();

			System.out.println("Saving log to "+f.getAbsolutePath());
			if(f.canWrite()){

				try {
					FileOutputStream out = new FileOutputStream(f);
					PrintStream ps = new PrintStream(out);
					
					for(int i=0; i<data.size(); i++){
						ps.println("["+type.get(i)+"] "+data.get(i));
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
