package analysis.lda.bow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import region.RegionMap;


public class OneDocXDayMultiPoint extends OneDocXDay {
	
	OneDocXDayMultiPoint() {	
	}
	
	@Override
	protected Map<String,Integer> computeHourRef(String[] events, int startIndex, RegionMap rm) {
		Map<String,Integer> hmap = new HashMap<String,Integer>();
		for(int h=0;h<24;h++) 
			hmap.put(String.valueOf(h), h);
		return hmap;
	}
	
	
	@Override
	public  Map<String,List<String>> process(List<TimePlace> tps) {
		Map<String,List<String>> dailyPatterns = new TreeMap<String,List<String>>();
		for(TimePlace tp : tps) {
			String key = tp.dow+"-"+tp.day;
			
			List<String> d = dailyPatterns.get(key);
			if(d == null) {
				d = new ArrayList<String>();
				dailyPatterns.put(key, d);
			}
						 for(int i=0; i<4;i++) d.add(tp.h+"-"+tp.getGeo());
			if(tp.h > 0) for(int i=0; i<2;i++) d.add(tp.h-1+"-"+tp.getGeo());
			if(tp.h < 23) for(int i=0; i<2;i++) d.add(tp.h+1+"-"+tp.getGeo());
			if(tp.h > 1) for(int i=0; i<1;i++) d.add(tp.h-2+"-"+tp.getGeo());
			if(tp.h < 22) for(int i=0; i<1;i++) d.add(tp.h+2+"-"+tp.getGeo());
		}
		return dailyPatterns;
	}
}
