package analysis.lda.bow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import analysis.lda.CreateBagOfWords;
import analysis.lda.TopicModel;


public class OneDocXDayMov extends Bow {
	
	public static final String[] HP = new String[]{"N","N","N","N","N","N","N","M", 
		   										   "M","M","M","M","M","M","A","A", 
		   										   "A","A","A","E","E","E","E","N"};
	
	
	
	public  Map<String,List<String>> process(List<TimePlace> tps) {
		Map<String,List<String>> dailyPatterns = new TreeMap<String,List<String>>();
		TimePlace last = tps.get(0);
		
		int long_trip = 0;
		
		for(int i=1; i<tps.size();i++) {
			TimePlace tp = tps.get(i);
			
			boolean tc = last.day.equals(tp.day);
			boolean sc = dist(tp.rname,last.rname) > 3;
			
			if(dist(tp.rname,last.rname) > 3)
				long_trip ++;
			
			if(tc && sc) {
				String key = tp.dow+"-"+tp.day;
				List<String> d = dailyPatterns.get(key);
				if(d == null) {
					d = new ArrayList<String>();
					dailyPatterns.put(key, d);
				}
				d.add(HP[last.h]+"-"+last.rname+"->"+HP[tp.h]+"-"+tp.rname);
				//d.add(last.rname+"->"+tp.rname);
				last = tp;
			}
			if(!tc) last = tp;		
		}
		
		if(long_trip < 40) return null;
		
		return dailyPatterns;
	}	
}
