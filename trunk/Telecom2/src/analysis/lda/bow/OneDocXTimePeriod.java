package analysis.lda.bow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;





public class OneDocXTimePeriod extends Bow {
	
	public static final String[] HP = new String[]{"N","N","N","N","N","N","N","M", 
		   										   "M","M","M","M","M","M","A","A", 
		   										   "A","A","A","E","E","E","E","N"};

	
	public  Map<String,List<String>> process(List<TimePlace> tps) {
		Map<String,List<String>> tpPatterns = new HashMap<String,List<String>>();
		for(TimePlace tp : tps) {
			String key = tp.day+"-"+tp.dow+"-"+HP[tp.h];
			List<String> d = tpPatterns.get(key);
			if(d == null) {
				d = new ArrayList<String>();
				tpPatterns.put(key, d);
			}
			d.add(tp.rname);
		}
		return tpPatterns;
	}
}
