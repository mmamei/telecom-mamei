package analysis;

import java.util.Calendar;
import java.util.Map;
import java.util.Set;

class PLSMap {
	Map<String,Set<String>> usr_counter;
	Map<String,Integer> pls_counter;
	Calendar startTime;
	Calendar endTime;
	
	public PLSMap(Map<String,Set<String>> usr_counter, Map<String,Integer> pls_counter, Calendar startTime, Calendar endTime) {
		this.usr_counter = usr_counter;
		this.pls_counter = pls_counter;
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	public int getHours() {
		System.out.println(startTime.getTime());
		System.out.println(endTime.getTime());
		return (int)Math.ceil((1.0*(endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 3600000));
	}
}
