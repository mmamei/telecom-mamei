package analysis.lda.bow;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.gps.utils.LatLonPoint;
import org.gps.utils.LatLonUtils;

import area.region.Region;
import area.region.RegionMap;

public abstract class Bow {
	
	public static final String[] HP = new String[]{"N","N","N","N","N","N","N","M", 
		   										   "M","M","M","M","M","M","A","A", 
		   										   "A","A","A","E","E","E","E","E"};

	static final Map<String,Integer> TM = new HashMap<String,Integer>();
	static {
		TM.put("N", 0);
		TM.put("M", 1);
		TM.put("A", 2);
		TM.put("E", 3);
	}
	
	
	protected static int MIN_PLACES = 3;
	
	public Map<String,List<String>> process(String[] events, int startIndex, RegionMap rm) {
		
		Set<String> places = new HashSet<String>();
		List<TimePlace> tps = new ArrayList<TimePlace>();
		
		for(int i=startIndex;i<events.length;i++) {
			// 2013-5-23:Sun:13:4018542484
			String[] x = events[i].split(":");
			int h = Integer.parseInt(x[2]);
			long celllac =Long.parseLong(x[3]);
			Region r = rm.getClosest(celllac);
			tps.add(new TimePlace(x[0],x[1],h,r));
			places.add(r == null ? "EXT" : r.getName() );
		}
		
		if(places.size() < MIN_PLACES) return null;
		return process(tps);
	}
	
	
	protected abstract Map<String,List<String>> process(List<TimePlace> tps);
	
	
	
	
	/*******************************************************************************/
	static final DecimalFormat F = new DecimalFormat("##.####",new DecimalFormatSymbols(Locale.US));
	protected class TimePlace {
		String day;
		String dow;
		int h;
		Region r;
		LatLonPoint p;
		
		TimePlace(String day,String dow, int h, Region r) {
			this.day = day;
			this.dow = dow;
			this.h = h;
			this.r = r;
			p = new LatLonPoint(r.getCenterLat(),r.getCenterLon());
		}
		
		double sdist(TimePlace o) {
			return LatLonUtils.getHaversineDistance(p,o.p);
		}
		
		
		
		int tdist(TimePlace prev) {
			int tp = TM.get(HP[prev.h]);
			int ta = TM.get(HP[h]);
			return ta - tp;
		}
		
		
		String getRegionName() {
			return r == null ? "EXT" : r.getName();
		}
		
		String getGeo() {
			return r == null ? "EXT" : F.format(r.getCenterLon())+","+F.format(r.getCenterLat());
		}
		
	}
}
