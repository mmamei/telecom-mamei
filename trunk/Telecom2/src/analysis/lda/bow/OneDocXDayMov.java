package analysis.lda.bow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import analysis.lda.CreateBagOfWords;
import analysis.lda.CreateTopicModel;


public class OneDocXDayMov extends Bow {
	
	
	public static final int MIN_DIST_FOR_LONG_TRIP = 2000; // min distance to be considerd long trip
	public static final int MIN_LONG_TRIPS = 30;  
	
	public  Map<String,List<String>> process(List<TimePlace> tps) {
		Map<String,List<String>> dailyPatterns = new TreeMap<String,List<String>>();
		TimePlace last = tps.get(0);
		
		int long_trip = 0;
		
		for(int i=1; i<tps.size();i++) {
			TimePlace tp = tps.get(i);
			
			boolean tc = last.day.equals(tp.day);
			boolean sc = tp.tdist(last) > 1 || tp.sdist(last) >= MIN_DIST_FOR_LONG_TRIP;
			
			if(tp.sdist(last) >= MIN_DIST_FOR_LONG_TRIP)
				long_trip ++;
			
			if(tc && sc) {
				
				String key = tp.dow+"-"+tp.day;
				
				List<String> d = dailyPatterns.get(key);
				if(d == null) {
					d = new ArrayList<String>();
					dailyPatterns.put(key, d);
				}
				String w = HP[last.h]+","+last.getGeo()+"-"+HP[tp.h]+","+tp.getGeo();
				d.add(w);
				last = tp;
			}
			if(!tc) last = tp;		
		}
		
		if(long_trip < MIN_LONG_TRIPS) return null;
		
		return dailyPatterns;
	}	
}
