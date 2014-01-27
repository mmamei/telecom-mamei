package analysis.tourist.extractGT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.FileUtils;
import utils.Logger;

public class Tourist_EXP_ResidentOtherRegions {
	
	
	static Map<String,String> R2F_SUFFIX = new HashMap<String,String>(); // region to file
	static {
		R2F_SUFFIX.put("Lombardia", "_G__DATASET_PLS_file_pls_lomb_day.csv");
		R2F_SUFFIX.put("Piemonte", "_G__DATASET_PLS_file_pls_piem_day.csv");
	}
	
	public static void main(String[] args) throws Exception {
		
		String city = "Venezia";
		String[] other = new String[]{"Lombardia","Piemonte"};
		process(city,other);
		Logger.logln("Done!");
	}
	
	
	
	public static void process(String city, String[] other) throws Exception {
		
		Map<String,Map<String,Integer>> users = new HashMap<String,Map<String,Integer>>();
		
		for(String o: other) 
			run(users,o,FileUtils.getFileS("UserEventCounter/"+city+R2F_SUFFIX.get(o)),true);
		run(users,city,FileUtils.getFileS("UserEventCounter/"+city+"_day.csv"),false);
		
		String of = city;
		for(String o: other) of = of+"_"+o;
		PrintWriter out = FileUtils.getPW("UserEventCounter", of+".csv");
		for(String un: users.keySet()) {
			Map<String,Integer> m = users.get(un);
			
			String res = un+","+classify(city,m);
			
			for(String r: m.keySet()) 
				res = res + "," + r + "," + m.get(r)+"%";
			out.println(res);
		}
		out.close();
	}
	
	
	public static void run(Map<String,Map<String,Integer>> users, String city, String file, boolean add) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while((line=br.readLine())!=null) {
			String un = line.substring(0,line.indexOf(","));
			String data = line.substring(line.indexOf(",")+1);	
			Map<String,Integer> m = users.get(un);
			if(add && m==null) {
				m = new HashMap<String,Integer>();
				users.put(un, m);
			}
			if(m!=null)
				m.put(city,parse(city,data));
		}
		br.close();
	}
	
	
	static Map<String,Integer> C2S = new HashMap<String,Integer>(); // city to size (num days of data available)
	static {
		C2S.put("Firenze", 40);
		C2S.put("Venezia", 84);
		C2S.put("Lombardia",35);
		C2S.put("Piemonte",96);
	}
	
	// 22201,17,3,2,28-Mar-2012,30-Mar-2012
	public static int parse(String city,String line) {
		String[] e = line.split(",");
		int ndays = Integer.parseInt(e[2]);
		int interval = Integer.parseInt(e[3]) + 1;
		int p1 = 100 * ndays / C2S.get(city);
		return p1;
	}
	
	
	public static String classify(String city, Map<String,Integer> m) {
		int p1 = m.get(city);
		int p2 = 0;
		for(int p : m.values())
			p2 = Math.max(p2, p);
		
		if(p2 > 40 && p1 < 5) return "TOURIST";
		if(p1 > 70) return "RESIDENT";
		return "UNKNOWN";
	}
}
