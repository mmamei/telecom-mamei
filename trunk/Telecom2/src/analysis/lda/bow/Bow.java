package analysis.lda.bow;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import network.NetworkCell;
import network.NetworkMap;
import network.NetworkMapFactory;

import org.gps.utils.LatLonPoint;
import org.gps.utils.LatLonUtils;

import utils.Config;
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
	
	private static final SimpleDateFormat DF = new SimpleDateFormat("yyyy-mm-HH");
	protected static int MIN_PLACES = 3;
	
	public Map<String,List<String>> process(String[] events, int startIndex, RegionMap rm) throws Exception {
		
		Set<String> places = new HashSet<String>();
		List<TimePlace> tps = new ArrayList<TimePlace>();
		
		for(int i=startIndex;i<events.length;i++) {
			// 2013-5-23:Sun:13:4018542484
			String[] x = events[i].split(":");
			int h = Integer.parseInt(x[2]);
			long celllac =Long.parseLong(x[3]);
			Region r = rm.getClosest(celllac, DF.parse(x[0]).getTime());
			String rname = r == null ? "EXT" : r.getName();
			tps.add(new TimePlace(x[0],x[1],h,rname,rm,events));
			places.add(rname);
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
		String rname;
		LatLonPoint p;
		
		TimePlace(String day,String dow, int h, String rname,RegionMap rm, String[] events) {
			this.day = day;
			this.dow = dow;
			this.h = h;
			this.rname = rname;
			try {
				p = getCenter(rm,events);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
		
		private LatLonPoint getCenter(RegionMap rm, String[] events) throws Exception {
			double lon = 0;
			double lat = 0;
			int cont = 0;
			for(int i=5;i<events.length;i++) {
				// 2013-5-23:Sun:13:4018542484
				String[] x = events[i].split(":");
				int h = Integer.parseInt(x[2]);
				long celllac =Long.parseLong(x[3]);
				if(rm.getClosest(celllac,DF.parse(x[0]).getTime()).getName().equals(rname)) {
					NetworkMap nm = NetworkMapFactory.getNetworkMap(DF.parse(x[0]).getTime());
					NetworkCell nc = nm.get(celllac);
					lon += nc.getBarycentreLongitude();
					lat += nc.getBarycentreLatitude();
					cont ++;
				}
			}
			if(cont == 0) return null;
			return new LatLonPoint(lat/cont,lon/cont);
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
			return rname;
		}
		
		String getGeo() {
			return p == null ? "EXT" : F.format(p.getLongitude())+","+F.format(p.getLatitude());
		}
		
	}
}
