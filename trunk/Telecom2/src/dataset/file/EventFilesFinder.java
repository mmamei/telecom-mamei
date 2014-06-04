package dataset.file;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import region.RegionMap;
import utils.Logger;
import dataset.DataFactory;
import dataset.EventFilesFinderI;

/*
 * This class identifies the proper PLS folder (if any) to process the event
 */

  class EventFilesFinder implements EventFilesFinderI {
	
	
	private SimpleDateFormat F1 = new SimpleDateFormat("yyyy-MM-dd-hh");
	private SimpleDateFormat F2 = new SimpleDateFormat("yyyy/MMM/dd",Locale.US);
	
	private Map<String,RegionMap> maps;
	private Map<String,List<String>> mapt;
	
	EventFilesFinder() {
		maps = DataFactory.getPLSCoverageSpace().getPlsCoverage();
		mapt = DataFactory.getPLSCoverageTime().computeAll();
	}
	
	
	public String find(String sday, String shour, String eday, String ehour, double lon1, double lat1, double lon2, double lat2) {
		try {
			Calendar c1 = Calendar.getInstance();
			Calendar c2 = Calendar.getInstance();
			c1.setTime(F1.parse(sday+"-"+shour));
			c2.setTime(F1.parse(eday+"-"+ehour));
			return find(c1,c2,lon1,lat1,lon2,lat2);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
		
	}
	
	public String find(Calendar cs, Calendar ce, double lon1, double lat1, double lon2, double lat2) {
		try {
			
			String sday = F2.format(cs.getTime());
			String eday = F2.format(ce.getTime());
			
			double clat = (lat1 + lat2)/2;
			double clon = (lon1 + lon2)/2;
			
			String dir = null;
			
			// check spatial constraints
			
			for(String r: maps.keySet()) {
				
				//System.out.println("testing "+r+", ("+clon+","+clat+")");
				
				
				if(maps.get(r).get(clon, clat) != null) {
					if(dir==null) dir = r;
					else Logger.logln("Warning: Multiple matching regions!");
				}
			}
			
			if(dir == null) {
				Logger.logln("Selected event is out of PLS coverage area in space");
				return null;
			}
			
			//System.out.println(dir);
			
			// check temporal constraints
			List<String> dmap = mapt.get(dir);
			/*
			for(String d: dmap)
				if(d.startsWith("2014"))
				System.out.println(d);
			System.out.println(sday+" - "+eday);
			*/
			if(dmap.contains(sday) && dmap.contains(eday))
				return dir;
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	
	public static void main(String[] args) {
		

		
		EventFilesFinder eff = new EventFilesFinder();
		//String dir = eff.find("2012-03-20","19","2012-03-20","23",7.641453,45.109536,7.641453,45.109536);
		//String dir = eff.find("2012-05-06","19","2012-05-06","23",9.124,45.4781,9.124,45.4781);
		String dir = eff.find("2011-12-06","19","2011-12-06","23",-3.9808,5.2927,-3.9808,5.2927); // abidjan
		
		
		System.out.println(dir);
	}
}
