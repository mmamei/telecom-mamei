package analysis.lda.bow;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import area.region.Region;
import area.region.RegionMap;

public abstract class Bow {
	
	protected static int MIN_PLACES = 10;
	
	public Map<String,List<String>> process(String[] events, int startIndex, RegionMap rm) {
		
		Set<String> places = new HashSet<String>();
		List<TimePlace> tps = new ArrayList<TimePlace>();
		
		for(int i=startIndex;i<events.length;i++) {
			// 2013-5-23:Sun:13:4018542484
			String[] x = events[i].split(":");
			int h = Integer.parseInt(x[2]);
			long celllac =Long.parseLong(x[3]);
			Region r = rm.getClosest(celllac);
			String rname = r == null ? "OUT" : r.getName().replaceAll(",", "-");
			tps.add(new TimePlace(x[0],x[1],h,rname));
			places.add(rname);
		}
		
		if(places.size() < MIN_PLACES) return null;
		return process(tps);
	}
	
	
	protected abstract Map<String,List<String>> process(List<TimePlace> tps);
	
	
	protected class TimePlace {
		String day;
		String dow;
		int h;
		String rname;
		
		TimePlace(String day,String dow, int h, String rname) {
			this.day = day;
			this.dow = dow;
			this.h = h;
			this.rname = rname;
		}
	}
}
