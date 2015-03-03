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

import org.gps.utils.LatLonPoint;
import org.gps.utils.LatLonUtils;

import region.RegionI;
import region.RegionMap;
import dataset.file.DataFactory;

public abstract class Bow {
	
	
	
	public static Bow getInstance(String bow_kind) {
		if(bow_kind.equals("OneDocXDay")) return new OneDocXDay();
		else if(bow_kind.equals("OneDocXDayMov")) return new OneDocXDayMov();
		else if(bow_kind.equals("OneDocXDayMultiPoint")) return new OneDocXDayMultiPoint();
		return null;
	}
	
	
	
	private static final SimpleDateFormat DF = new SimpleDateFormat("yyyy-mm-HH");
	protected static int MIN_PLACES = 3;
	
	public Map<String,List<String>> process(String[] events, int startIndex, RegionMap rm) throws Exception {
		
		Set<String> places = new HashSet<String>();
		List<TimePlace> tps = new ArrayList<TimePlace>();
		
		Map<String,LatLonPoint> r2p = computeRegionRefPoint(events,startIndex,rm);
		Map<String,Integer> h2h = computeHourRef(events,startIndex,rm);
		
		
		for(int i=startIndex;i<events.length;i++) {
			// 2013-5-23:Sun:13:4018542484
			String[] x = events[i].split(":");
			int h = Integer.parseInt(x[2]);
			String celllac = x[3];
			RegionI r = rm.getClosest(celllac, DF.parse(x[0]).getTime());
			String rname = r == null ? "EXT" : r.getName();
			
			h = h2h.get(String.valueOf(h));

			//LatLonPoint p = r == null ? null : r.getCenterPoint(); // with this each user is associated to a point in the center of the cell
			LatLonPoint p = r2p.get(rname); // with this each user is associated to a point in the barycenter of the user' CDR in that region.
			
			tps.add(new TimePlace(x[0],x[1],h,p));
			places.add(rname);
		}
		
		if(places.size() < MIN_PLACES) return null;
		return process(tps);
	}
	
	
	
	/**************************************************************************************************************************************************/
	
	/*
	 * The two method below: computeRegionRefPoint and computeHourRef are very important.
	 * Basically they cluster events both in space and time to create word repetitions to be exploited by LDA.
	 * So, thanks to computeRegionRefPoint all the events generated from a region get the same coordinates (leading to repetitions in space)
	 * Thanks to  computeHourRef all the events generated from the same time slice get the same hour (leading to repetitions in time).
	 * It is worth noting that since LDA operates on individual users, also these methods create repetitions on each user (it is not important that the words of a user are different from the words of another user).
	 */
	
	
	/*
	 * this method computes the reference point of each region. 
	 * Instead of using the center of the region, we consider the barycenter of user events in that region.
	 * Since LDA is applied to individual users. This does not affect the behavior of LDA. Words *of each user individually*
	 * will be repeated as if considering the center of the region.
	 * 
	 * The resulting map associated for each region name, the center point
	 */
	
	
	private Map<String,LatLonPoint> computeRegionRefPoint(String[] events, int startIndex, RegionMap rm) {
		Map<String,LatLonPoint> result = null;
		try {
			// region name --> list of coordinates (lat, lon) therein
			// it is useful to easilty compunte the mean afterwards
			Map<String,List<double[]>> r2l = new HashMap<String,List<double[]>>();
			for(int i=startIndex;i<events.length;i++) {
				// 2013-5-23:Sun:13:4018542484
				String[] x = events[i].split(":");
				int h = Integer.parseInt(x[2]);
				String celllac = x[3];
				RegionI region = rm.getClosest(celllac,DF.parse(x[0]).getTime());
				if(region == null) continue;
				List<double[]> l = r2l.get(region.getName());
				if(l == null) {
					l = new ArrayList<double[]>();
					r2l.put(region.getName(), l);
				}
				RegionMap nm =  DataFactory.getNetworkMapFactory().getNetworkMap(DF.parse(x[0]).getTime());
				l.add(nm.getRegion(celllac).getLatLon());
			}
			
			result = new HashMap<String,LatLonPoint>();
			for(String rname: r2l.keySet()) {
				List<double[]> l = r2l.get(rname);
				double[] latlon = new double[2];
				for(double[] p: l) {
					latlon[0] += p[0];
					latlon[1] += p[1];
				}
				latlon[0] /= l.size();
				latlon[1] /= l.size();
				result.put(rname, new LatLonPoint(latlon[0],latlon[1]));
			}		
		}catch(Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return result;
	}
	
	
	/* This method creates a map associating to a String representing an hour, another reference hour, used to induce repetitions in words.
	 * A simple version of this mapping could be
	 * [0,1,2,3,4,5] --> 3 (i.e., nigh hour)
	 * [6,7,8,9] --> 8 (i.e., morning)
	 * [10,11,12,13,14,15] --> 13 (i.e., lunch time)
	 * [16,17,18,19,20] --> 19 (i.e., dinner time)
	 * [21,22,23] --> 22 (i.e., evening).
	 * After this mapping events generated at 7 or 8 would seem to have been both generated at 8 thus repetitions.
	 * 
	 * We use a String key (rather than Integer key) for the map, as in principle different regions could have a different mapping. 
	 * For example I could cluster all the events genrated on a region an take the average time in there.
	 * 
	 */
	
	protected Map<String,Integer> computeHourRef(String[] events, int startIndex, RegionMap rm) {
		Map<String,Integer> hmap = new HashMap<String,Integer>();
		for(int h=0;h<24;h++) {
			if(0 <= h && h <= 5) hmap.put(String.valueOf(h), 3);
			if(6 <= h && h <= 9) hmap.put(String.valueOf(h), 8);
			if(10 <= h && h <= 13) hmap.put(String.valueOf(h), 11);
			if(14 <= h && h <= 16) hmap.put(String.valueOf(h), 15);
			if(17 <= h && h <= 20) hmap.put(String.valueOf(h), 19);
			if(21 <= h && h <= 23) hmap.put(String.valueOf(h), 22);
		}
		return hmap;
	}
	
	
	// This method returns a map for each day returns the list of words describing that day
	protected abstract Map<String,List<String>> process(List<TimePlace> tps);
	
	// This method parses a topic string, and returns a list of words and associated probabilities
	public abstract List<Map.Entry<String,Double>> parsePWZ(String topic);
	
	// This method gives a kml representation of a word/probability
	public abstract String word2KML(Map.Entry<String,Double> wp);
	
	
	/**************************************************************************************************************************************************/
	/**************************************************************************************************************************************************/
	/**************************************************************************************************************************************************/
	/**************************************************************************************************************************************************/
	/**************************************************************************************************************************************************/
	/**************************************************************************************************************************************************/
	/**************************************************************************************************************************************************/
	/**************************************************************************************************************************************************/
	
	
	
	
	static final DecimalFormat F = new DecimalFormat("##.####",new DecimalFormatSymbols(Locale.US));
	protected class TimePlace {
		String day;
		String dow;
		int h;
		LatLonPoint p;
		
		TimePlace(String day,String dow, int h,LatLonPoint p) {
			this.day = day;
			this.dow = dow;
			this.h = h;
			this.p=p;
		}
				
		double sdist(TimePlace o) {
			if(p==null || o.p==null) return 0;
			return LatLonUtils.getHaversineDistance(p,o.p);
		}
		
		
		int tdist(TimePlace prev) {
			return h - prev.h;
		}
		
		String getGeo() {
			return p == null ? "EXT,EXT" : F.format(p.getLongitude())+","+F.format(p.getLatitude());
		}
		
	}
}
