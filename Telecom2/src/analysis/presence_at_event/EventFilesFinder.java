package analysis.presence_at_event;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
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
			
			double clat = (lat1 + lat2)/2;
			double clon = (lon1 + lon2)/2;
			
			String dir = null;
			
			// check spatial constraints
			
			for(String r: maps.keySet()) {
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
	
	
	public static void main(String[] args) {
		
		EventFilesFinder eff = new EventFilesFinder();
		String dir = eff.find("2014/Mar/17","4","2014/Mar/17","7",11.2477,43.7629,11.2491,43.7620);
		System.out.println(dir);
	}
}
