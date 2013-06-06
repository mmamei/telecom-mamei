package pls_parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import utils.Config;

public class RemoveDuplicateFiles {
	
	static Config conf = null;
	
	public static List<String> list = new ArrayList<String>();
	
	public static void main(String[] args) throws Exception {
		String dir = Config.getInstance().pls_folder;
		analyzeDirectory(new File(dir));
	}
	
	private static void analyzeDirectory(File directory) throws Exception{
		File[] items = directory.listFiles();
		for(int i=0; i<items.length;i++){
			File item = items[i];
			if(item.isFile()) {
				
				if(list.contains(item.getName())) {
					System.out.println("Deleting "+item);
					item.delete();
				}
				else list.add(item.getName());
				
			}
			else if(item.isDirectory())
				analyzeDirectory(item);
		}	
	}
}
