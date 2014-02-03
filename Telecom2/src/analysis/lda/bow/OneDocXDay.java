package analysis.lda.bow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class OneDocXDay extends Bow {
	
	public static final String[] HP = new String[]{"N","N","N","N","N","N","N","M", 
		   										   "M","M","M","M","M","M","A","A", 
		   										   "A","A","A","E","E","E","E","N"};
	
	
	
	public  Map<String,List<String>> process(List<TimePlace> tps) {
		Map<String,List<String>> dailyPatterns = new TreeMap<String,List<String>>();
		for(TimePlace tp : tps) {
			List<String> d = dailyPatterns.get(tp.day+"-"+tp.dow);
			if(d == null) {
				d = new ArrayList<String>();
				dailyPatterns.put(tp.day+"-"+tp.dow, d);
			}
			d.add(HP[tp.h]+"-"+tp.rname);
		}
		return dailyPatterns;
	}
}
