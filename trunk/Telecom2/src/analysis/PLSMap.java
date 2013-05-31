package analysis;

import java.util.Calendar;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


/*
 * USED in combination with PLSBehaviorInAnArea...
 */

class PLSMap {
	Map<String,Set<String>> usr_counter;
	Map<String,Integer> pls_counter;
	Calendar startTime = null;
	Calendar endTime = null;
	
	public PLSMap() {
		this.usr_counter = new TreeMap<String,Set<String>>();
		this.pls_counter  = new TreeMap<String,Integer>();
	}
	
	public int getHours() {
		System.out.println(startTime.getTime());
		System.out.println(endTime.getTime());
		return (int)Math.ceil((1.0*(endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 3600000));
	}
	
	public String[] getDomain() {
		Calendar cal = (Calendar)startTime.clone();
		String[] domain = new String[getHours()];
		for(int i=0; i<domain.length;i++) {
			domain[i] = getLabel(cal);
			cal.add(Calendar.HOUR_OF_DAY, 1);
		}
		return domain;
	}
	static final String[] DAYS = new String[]{"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
	private String getLabel(Calendar cal) {
		//return "-"+cal.get(Calendar.DAY_OF_MONTH)+":"+DAYS[cal.get(Calendar.DAY_OF_WEEK)-1]+"-";
		return "["+cal.get(Calendar.DAY_OF_MONTH)+"-"+DAYS[cal.get(Calendar.DAY_OF_WEEK)-1]+":"+cal.get(Calendar.HOUR_OF_DAY)+"]";
	}
}
