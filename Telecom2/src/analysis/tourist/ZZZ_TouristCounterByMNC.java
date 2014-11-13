package analysis.tourist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;
import utils.Sort;
import analysis.PLSSpaceDensity;

public class ZZZ_TouristCounterByMNC {
	
	static final String[] MONTHS = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	
	public static void main(String[] args) throws Exception {
		
		String pre = "file_pls_ve_";
		String city = "Venezia";
		
		BufferedReader br = new BufferedReader(new FileReader(new File(Config.getInstance().base_folder+"/UserEventCounter/"+pre+city+"_cellXHour.csv")));
		
		Map<String,String> mncT = mncT();
		
		Map<String,Integer> count = new HashMap<String,Integer>();
		
		
		String line;
		while((line=br.readLine())!=null) {
			if(!line.contains(",")) continue;
			String[] split = line.split(",");
			 String mnt = split[1];
			int numdays = Integer.parseInt(split[3]);
			if(numdays > 4) continue;
			String country = mncT.get(mnt.substring(0,3));
			if(country == null) System.out.println(mnt.substring(0,3));
			Integer c = count.get(country);
			if(c == null) c = 0;
			count.put(country, c+1);
		}
		br.close();
		
		int tot =0;
		for(int c: count.values())
			tot+=c;
		
		System.out.println(tot);
		
		LinkedHashMap<String, Integer> ordered = Sort.sortHashMapByValuesD(count,Collections.reverseOrder());
		for(String country : ordered.keySet()) {
			Logger.logln(country+" = "+1.0*ordered.get(country)/tot);
		}
	}
	
	
	private static Map<String,String> mncT() throws Exception {
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
