package analysis.lda.bow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import analysis.lda.CreateBagOfWords;
import analysis.lda.TopicModel;


public class OneDocXTimePeriodMov extends Bow {
	
	public static final String[] HP = new String[]{"N","N","N","N","N","N","N","M", 
		   										   "M","M","M","M","M","M","A","A", 
		   										   "A","A","A","E","E","E","E","N"};
	
	
	
	public  Map<String,List<String>> process(List<TimePlace> tps) {
		Map<String,List<String>> dailyPatterns = new TreeMap<String,List<String>>();
		TimePlace last = tps.get(0);
		for(int i=1; i<tps.size();i++) {
			TimePlace tp = tps.get(i);
			
			boolean tc = last.day.equals(tp.day);
			boolean sc = dist(tp.rname,last.rname) > 2;
			
			String key = tp.day+"-"+tp.dow+"-"+HP[tp.h];
			
			if(tc && sc) {
				List<String> d = dailyPatterns.get(key);
				if(d == null) {
					d = new ArrayList<String>();
					dailyPatterns.put(key, d);
				}
				//d.add(HP[last.h]+"-"+last.rname+"->"+HP[tp.h]+"-"+tp.rname);
				d.add(last.rname+"->"+tp.rname);
				last = tp;
			}
			if(!tc) last = tp;		
		}
		return dailyPatterns;
	}
	
	
	public int dist(String rname1, String rname2) {
		String[] x1 = rname1.split("-");
		String[] x2 = rname2.split("-");
		int i1 = Integer.parseInt(x1[0]);
		int j1 = Integer.parseInt(x1[1]);
		int i2 = Integer.parseInt(x2[0]);
		int j2 = Integer.parseInt(x2[1]);
		return Math.abs(i1-i2) + Math.abs(j1-j2);
		
	}
}
