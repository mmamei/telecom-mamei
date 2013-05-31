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
}
