package utils;

import java.io.File;

public class FileUtils {
		
	public static final File[] DISKS = File.listRoots();

	public static File getFile(String fpath) {
		
		if(!fpath.startsWith("BASE") && !fpath.startsWith("DATASET")) {
			try {
				throw new Exception("WARNING: Requested file does not start with BASE nor DATASET!!!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		File file = null;
		for(File d: DISKS) {
			File f = new File(d+"/"+fpath);
			if(f.exists()) {
				if(file!=null) Logger.log("WARNING: FILE AT MULTIPLE LOCATIONS");
				file = f;
			}
		}
		return file;
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
