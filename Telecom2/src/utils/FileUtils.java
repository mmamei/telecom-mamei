package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileUtils {
	
	
	public static final String[] DISKS = new String[]{"C:","G:"};

	
	public static File getFile(String fpath) {
		for(String d: DISKS) {
			File f = new File(d+Config.getInstance().base_dir+"/"+fpath);
			if(f.exists()) return f;
		}
		return null;
	}
	
	public static String getFileS(String fpath) {
		if(fpath.contains(":")) fpath = fpath.substring(3);
		System.out.println(fpath);
		File f = getFile(fpath);
		return f.getAbsolutePath();
	}
	
	
	
	public static File create(String path) {
		File f = getFile(path);
		if(f == null) {
			f = new File(DISKS[0]+Config.getInstance().base_dir+"/"+path);
			f.mkdirs();
		}
		return f;
	}
	
	public static BufferedReader getBR(String fpath) {
		File f = getFile(fpath);
		try {
			return new BufferedReader(new FileReader(f));
		} catch (FileNotFoundException e) {
			return null;
		}
	}
	
	public static PrintWriter getPW(String dir, String file) {
		File d = getFile(dir);
		if(d == null) {
			d = new File(DISKS[0]+Config.getInstance().base_dir+"/"+dir);
			d.mkdirs();
		}
		try {
			System.out.println(d.getAbsolutePath()+"/"+file);
			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(new File(d.getAbsolutePath()+"/"+file))));
			return pw;
		} catch (IOException e) {
			return null;
		}
	}
	
	
	public static void main(String[] args) {
		File f = getFile("UserEventCounterDetailed/Asti.csv");
		System.out.println(f.getAbsolutePath());
		f = getFile("PlaceRecognizer/file_pls_piem_users_above_2000/results.csv");
		System.out.println(f.getAbsolutePath());
	}
	
	
}
