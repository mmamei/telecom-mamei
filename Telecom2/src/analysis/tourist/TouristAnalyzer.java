package analysis.tourist;

import java.io.BufferedReader;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pls_parser.BufferAnalyzer;
import pls_parser.PLSParser;
import utils.CopyAndSerializationUtils;
import utils.FileUtils;
import utils.Logger;
import area.region.RegionMap;

public class  TouristAnalyzer extends BufferAnalyzer {
	
	private static final int TIM = 0;
	private static final int ROAMING = 1;
	private static final int ALL = 2;
	
	
	private Set<String> user_set;
	private RegionMap map;
	
	Map<String,Double> space_density;
	Map<String,Double> time_density;
	
	
	public TouristAnalyzer(String user_file, String region_map, int max_days, int user_segment) throws Exception {
		map = (RegionMap)CopyAndSerializationUtils.restore(FileUtils.getFile(region_map));
		user_set = new HashSet<String>();
		BufferedReader br = FileUtils.getBR(user_file);
		String line;
		// 32b0aac184bab5813b9e294b789635b1e935d081b5b432772f16c9fddb4b5,22201,1,1372602476086,1372602476086,Sun Jun 30 16:27:56 CEST 2013,Sun Jun 30 16:27:56 CEST 2013
		while((line = br.readLine())!=null) {
			String[] e = line.split(",");
			if(user_segment == TIM && !e[1].equals("22201")) continue;
			if(user_segment == ROAMING && e[1].equals("22201")) continue;
			
			Long st = Long.parseLong(e[3]);
			Long et = Long.parseLong(e[4]);
			int days = (int)((et - st) / (1000*3600*24));
			if(days > max_days) continue;
			
			user_set.add(e[0]);
		}
		br.close();
		
		space_density = new HashMap<String,Double>();
		time_density = new HashMap<String,Double>();
	}

	String[] fields;
	String username;
	String imsi;
	String celllac;
	long timestamp;
	
	public void analyze(String line) {
		fields = line.split("\t");
		username = fields[0];
		imsi = fields[1];
		celllac = fields[2];
		timestamp = Long.parseLong(fields[3]);
		
		if(!user_set.contains(username)) return;
		
		
		
		
	}
	
	
	Calendar cal = new GregorianCalendar();
	private String getTimeKey(long timestamp) {
		cal.setTimeInMillis(timestamp);
		return cal.get(Calendar.YEAR)+"-"+cal.get(Calendar.MONTH)+"-"+cal.get(Calendar.DAY_OF_MONTH)+":"+cal.get(Calendar.HOUR_OF_DAY);
	}
	
	

	public void finish() {
		
		
		
		
		
	}
	
	public static void main(String[] args) throws Exception {
		TouristAnalyzer ba = new TouristAnalyzer("UserEventCounterDetailed/Venezia_trim3.csv","RegionMap/Venezia.ser",7,ROAMING);
	    PLSParser.parse(ba);
		ba.finish();
		Logger.logln("Done");
	}
}
