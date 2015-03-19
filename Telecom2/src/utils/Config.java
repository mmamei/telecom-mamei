package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;


public class Config {
	private static String CFILE = "G:/CODE/Telecom/Telecom2/config/config.txt"; // nel server all'univ
	static {
		File f = new File(CFILE);
		if(!f.exists()) CFILE = "C:/Users/Marco/Google Drive/Code/TELECOM/Telecom2/config/config.txt"; // vaio marco
	}
	
	
	// singleton
	private static Config conf = null;
	
	public static Config getInstance() {
		if (conf == null) {
			conf = new Config();
		}
		return conf;
	}
	
	private Config() {
		String line = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File(CFILE)));
			while((line = br.readLine()) != null) {
				line = line.trim();
				if(line.length()==0) continue;
				if(line.startsWith("//")) continue;
				String s[] = line.split("=");
				parse(s[0].trim(),s[1].trim());
			}
			br.close();
		} catch(Exception e) {
			System.err.println("Cannot read config file! "+line);
			
			e.printStackTrace();
		}
	}
	
	public String paper_folder = null;
	public String web_kml_folder = null;
	public String network_map_dir = null;
	public String pls_root_folder = null;
	public String pls_folder = null;
	public String base_folder = null;
	public Calendar pls_start_time = null;
	public Calendar	pls_end_time = null;
	public String placemarks_file = null;
	public String events_file = null;
	
	private void parse(String name, String value) {
		if(name.equals("network_map_dir")) {
			network_map_dir = value;
		}
		else if(name.equals("web_kml_folder")) {
			web_kml_folder = value;
		}
		else if(name.equals("paper_folder")) {
			paper_folder = value;
		}
		else if(name.equals("pls_folder")) {
			pls_root_folder = value;
			pls_folder = value;
		}
		else if(name.equals("base_folder")) {
			base_folder = value;
		}
		else if(name.equals("pls_start_time")) {
			SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			pls_start_time = new GregorianCalendar();
			try {
				pls_start_time.setTime(f.parse(value));
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		else if(name.equals("pls_end_time")) {
			SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			pls_end_time = new GregorianCalendar();
			try {
				pls_end_time.setTime(f.parse(value));
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		else if(name.equals("placemarks_file")) 
			placemarks_file = value;
		else if(name.equals("events_file")) 
			events_file = value;
		else {
			Logger.logln("Warning: parameter "+name+" = "+value+" unrecognized!");
		}
	}
	
	
	public void changeDataset(String dataset) {
		if(dataset.equals("italy")) {
			network_map_dir = "G:/DATASET/PLS/file_rete";
			pls_root_folder = "G:/DATASET/PLS/file_pls";
			pls_folder = "G:/DATASET/PLS/file_pls";
			base_folder = "C:/BASE";
		}
		if(dataset.equals("ivory-set2")) {
			network_map_dir = "G:/DATASET/D4D1/file_rete";
			pls_root_folder = "G:/DATASET/D4D1/file_pls";
			pls_folder = "G:/DATASET/D4D1/file_pls";
			base_folder = "C:/BASE_D4D1";
		}
		if(dataset.equals("ivory-set3")) {
			network_map_dir = "G:/DATASET/D4D2/file_rete";
			pls_root_folder = "G:/DATASET/D4D2/file_pls";
			pls_folder = "G:/DATASET/D4D2/file_pls";
			base_folder = "C:/BASE_D4D2";
		}
	}
	
	
	public String get_pls_subdir() {
		String subdir = pls_folder;
		subdir = subdir.replaceAll(":", "");
		subdir = subdir.replaceAll("/|\\\\", "_");
		return subdir;
	}
	
}
