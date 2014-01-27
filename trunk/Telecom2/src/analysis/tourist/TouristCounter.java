package analysis.tourist;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import utils.CopyAndSerializationUtils;
import utils.FileUtils;
import utils.Logger;
import utils.Sort;
import area.region.RegionMap;

public class TouristCounter {
	
	static final String[] MONTHS = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	
	public static void main(String[] args) throws Exception {
		
		for(int i=0; i<MONTHS.length;i++)
			process("Venezia",i);
		
	}
	
	public static void process(String city, int month) throws Exception {
		
		BufferedReader br = FileUtils.getBR("UserEventCounter/"+city+"_cellacXhour.csv");
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(FileUtils.getFile("RegionMap/"+city+".ser"));
		
		Map<String,String> mncT = mncT();
		
		Map<String,Integer> count = new HashMap<String,Integer>();
		
		TouristData4Analysis td;
		String line;
		while((line=br.readLine())!=null) {
			
			if(!line.contains("2013-"+month)) continue;
			
			td = new TouristData4Analysis(line,rm);
			
			if(td.num_days < 4 && td.days_interval < 3 * td.num_days) {
				String country = mncT.get(td.mnt.substring(0,3));
				
				if(country == null) System.out.println(td.mnt.substring(0,3));
				
				Integer c = count.get(country);
				if(c == null) c = 0;
				count.put(country, c+1);
			}
		}
		br.close();
		
		String f = FileUtils.getFileS("TouristData");
		PrintWriter out = new PrintWriter(new FileWriter(f+"/"+city+"_"+MONTHS[month]+"_count.csv"));
	
		LinkedHashMap<String, Integer> ordered = Sort.sortHashMapByValuesD(count,null);
		for(String country : ordered.keySet()) {
			Logger.logln(country+" = "+ordered.get(country));
			out.println(country+","+ordered.get(country));
		}
		out.close();
		
	}
	
	
	public static Map<String,String> mncT() throws Exception {
		Map<String,String> mncT = new HashMap<String,String>();
		
		BufferedReader br = new BufferedReader(new FileReader("G:/DATASET/PLS/MCC-MNC.csv"));
		String line;
		while((line=br.readLine()) != null) {
			//country;operator;mcc;mnc;operatorid
			String[] e = line.split(";");
			mncT.put(e[2], e[0]);
		}
		br.close();
		
		return mncT;
	}
}
