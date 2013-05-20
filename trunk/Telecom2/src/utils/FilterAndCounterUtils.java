package utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import analysis.PlsEvent;


public class FilterAndCounterUtils {
	
	public static List<PlsEvent> filterMultipleEventsInTheSameHour (List<PlsEvent> events) {
		List<PlsEvent> result = new ArrayList<PlsEvent>();
		
		Set<String> dh = new HashSet<String>();
		
		for(PlsEvent e: events){
			Calendar cal = e.getCalendar();
			String key = cal.get(Calendar.YEAR)+"-"+cal.get(Calendar.MONTH)+"-"+cal.get(Calendar.DAY_OF_MONTH)+"-"+cal.get(Calendar.HOUR_OF_DAY);
			if(!dh.contains(key)) {
				dh.add(key);
				result.add(e);
			}
		}
		return result;
	}
	
	
	public static int getNumDays(List<PlsEvent> events) {
		Set<String> days = new HashSet<String>();
		for(PlsEvent e: events){
			Calendar cal = e.getCalendar();
			String key = cal.get(Calendar.YEAR)+"-"+cal.get(Calendar.MONTH)+"-"+cal.get(Calendar.DAY_OF_MONTH);
			days.add(key);
		}
		return days.size();
	}
	
	
	public static List<PlsEvent> smooth(List<PlsEvent> pe) {
		List<PlsEvent> s = PlsEvent.clone(pe);
		for(int i=0; i<s.size();i++) {
			for(int j=0; j<i;j++) {
				if(s.get(i).getCellac() == s.get(j).getCellac()) {
					// loop found 
					int dt = (int)((s.get(i).getTimeStamp() - s.get(j).getTimeStamp()) / 60000);
					if(dt < 120) {
						// remove loop
						for(int k=j+1; k<i;k++)
							s.get(k).setCellac(s.get(j).getCellac());
					}
				}
			}
		}
		return s;
	}
}
