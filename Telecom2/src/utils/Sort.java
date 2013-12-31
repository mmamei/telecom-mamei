package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Sort {
	public static LinkedHashMap sortHashMapByValuesD(Map passedMap, Comparator c) {
		   List mapKeys = new ArrayList(passedMap.keySet());
		   List mapValues = new ArrayList(passedMap.values());
		   
		   if(c == null) {
			   Collections.sort(mapValues);
			   Collections.sort(mapKeys);
		   }
		   else {
			   Collections.sort(mapValues, c);
			   Collections.sort(mapKeys, c);
		   }
		   
		   LinkedHashMap sortedMap = new LinkedHashMap();

		   for(Object val: mapValues) {
			   for(Object key: mapKeys) {
		    	   String comp1 = passedMap.get(key).toString();
		    	   String comp2 = val.toString();
	
			        if (comp1.equals(comp2)){
			            passedMap.remove(key);
			            mapKeys.remove(key);
			            sortedMap.put(key, val);
			            break;
			        }
			    }
		}
		return sortedMap;
	}
}
