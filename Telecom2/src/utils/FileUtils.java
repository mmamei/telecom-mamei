package utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
		
	public static final File[] DISKS = File.listRoots();

	public static File getFile(String fpath) {
		File[] files = getFiles(fpath);
		if(files.length == 0) return null;
		if(files.length > 1)
			try {
				throw new Exception("WARNING: FILE AT MULTIPLE LOCATIONS");
			} catch (Exception e) {
				e.printStackTrace();
			}
		return files[0];
	}
	
	
	public static File[] getFiles(String fpath) {
		List<File> files = new ArrayList<File>();
		
		if(!fpath.startsWith("BASE") && !fpath.startsWith("DATASET"))
			try {
				throw new Exception("WARNING: Requested file/dir does not start with BASE nor DATASET!!!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		
		for(File d: DISKS) {
			File f = new File(d+"/"+fpath);
			if(f.exists()) 
				files.add(f);
		}
		
		File[] fs = new File[files.size()];
		return files.toArray(fs);
	}
	
	
	 
	
	
	public static File createDir(String fpath) {
		File file = getFile(fpath);
		if(file == null) {
			file = new File(DISKS[0]+"/"+fpath);
			file.mkdirs();
		}
		return file;
	}
	

	public static void main(String[] args) {
		File f = getFile("BASE/UserSetCreator/Firenze.csv");
		System.out.println(f.getAbsolutePath());
		f = getFile("BASE/UserEventCounter/Venezia_cellXHour.csv");
		System.out.println(f.getAbsolutePath());
	}
	
	
}
