package dataset.file;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.Logger;

public class DuplicateFiles {
	
	
	public static void main(String[] args) throws Exception {
		Map<String,List<File>> allFiles = new HashMap<String,List<File>>();
		analyzeDirectory(new File("G:/DATASET/PLS/file_pls"),allFiles);
		int tot_deteted = 0;
		for(String n: allFiles.keySet()) {
			List<File> files = allFiles.get(n);
			if(files.size() > 1) {
				for(int i=1; i<files.size();i++) { // keep only the 0
					files.get(i).delete();
					tot_deteted ++;
				}
			}
		}
		System.out.println("Deleted files = "+tot_deteted);
	}
	
	private static void analyzeDirectory(File directory, Map<String,List<File>> allFiles) throws Exception {
		
		Logger.logln("\t"+directory.getAbsolutePath());
		
		File[] items = directory.listFiles();
		
		for(int i=0; i<items.length;i++){
			File item = items[i];
			if(item.isFile()) {
				String n = item.getName();
				List<File> files  = allFiles.get(n);
				if(files == null) {
					files = new ArrayList<File>();
					allFiles.put(n, files);
				}
				files.add(item);
			}
			else if(item.isDirectory())
				analyzeDirectory(item,allFiles);
		}	
	}
	
}
