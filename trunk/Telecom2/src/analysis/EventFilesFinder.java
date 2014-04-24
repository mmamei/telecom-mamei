package analysis;

import java.util.HashMap;
import java.util.Map;

import pls_parser.AnalyzePLSCoverageSpace;
import pls_parser.AnalyzePLSCoverageTime;
import utils.Logger;
import area.region.RegionMap;

/*
 * This class identifies the proper PLS folder (if any) to process the event
 */

public class EventFilesFinder {
	
	private Map<String,RegionMap> maps;
	private Map<String,Map<String,String>> mapt;
	
	public EventFilesFinder() {
		maps = new AnalyzePLSCoverageSpace().getPlsCoverage();
		mapt = new AnalyzePLSCoverageTime().computeAll();
	}
	
	public String find(String sday, String shour, String eday, String ehour, double lon1, double lat1, double lon2, double lat2) {
		try {
			
			
			sday = convertDay(sday);
			eday = convertDay(eday);
			
			
			System.out.println(sday);
			System.out.println(eday);
			
			double clat = (lat1 + lat2)/2;
			double clon = (lon1 + lon2)/2;
			
			String dir = null;
			
			// check spatial constraints
			
			for(String r: maps.keySet()) {
				
				System.out.println("testing "+r+", ("+clon+","+clat+")");
				
				if(maps.get(r).get(clon, clat) != null) {
					if(dir==null) dir = r;
					else Logger.logln("Warning: Multiple matching regions!");
				}
			}
			
			if(dir == null) {
				Logger.logln("Selected event is out of PLS coverage area in space");
				return null;
			}
			
			// check temporal constraints
			
			boolean okstart = false;
			boolean okend = false;
			Map<String,String> dmap = mapt.get(dir);
			for(String day: dmap.keySet()) {
				if(day.equals(sday) && dmap.get(day).contains(shour+"-")) okstart = true;		
				if(day.equals(eday) && dmap.get(day).contains(ehour+"-")) okend = true;		
			}
			
			if(!okstart || !okend) {
				Logger.logln("Selected event is out of PLS coverage area in time");
				return null;
			}
						
			return dir;
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	static final Map<String,String> htmlMontMmap = new HashMap<String,String>();
	static {
		htmlMontMmap.put("01", "Jan"); htmlMontMmap.put("02", "Feb"); htmlMontMmap.put("03", "Mar"); htmlMontMmap.put("04", "Apr");
		htmlMontMmap.put("05", "May"); htmlMontMmap.put("06", "Jun"); htmlMontMmap.put("07", "Jul"); htmlMontMmap.put("08", "Aug");  
		htmlMontMmap.put("09", "Sep"); htmlMontMmap.put("10", "Oct"); htmlMontMmap.put("11", "Nov"); htmlMontMmap.put("12", "Dec"); 
	}
	
	private String convertDay(String day) {
		String[] s = day.split("-");
		return s[0]+"/"+htmlMontMmap.get(s[1])+"/"+s[2];
	}
	
	
	public static void main(String[] args) {
		
		EventFilesFinder eff = new EventFilesFinder();
		String dir = eff.find("2014-03-10","4","2014-03-10","7",11.2477,43.7629,11.2491,43.7620);
		System.out.println(dir);
	}
}
