package region.network;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import region.RegionMap;
import dataset.db.insert.NetworkTable;

public class NetworkMapFactory {
	private static final SimpleDateFormat F = new SimpleDateFormat("dd/MM/yyyy");
	
	private static Map<String,RegionMap> mapnet = new HashMap<String,RegionMap>();
	
	private static String getCalString(String f) {
		//dfl_network_20130516.bin
		String yyyy = f.substring(12,16);
		String MM = f.substring(16,18);
		String dd = f.substring(18,20);
		return dd+"/"+MM+"/"+yyyy;
	}

	
	public static Calendar getCalendar(String f) {
		Calendar c = null;
		try{
		    c = Calendar.getInstance();
			c.setTime(F.parse(getCalString(f)));
		}catch(Exception e) {
			System.err.println("--- "+f);
			e.printStackTrace();
		}
		return c;
	}
	
	public static RegionMap getNetworkMap(long time) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		return getNetworkMap(cal);
	}
	
	
	public static RegionMap getNetworkMap(Calendar calendar) {
		
		String table = NetworkTable.findClosestNetworkTable(calendar);
		//Logger.logln("!!!! Using network table "+table);
		RegionMap nm = mapnet.get(table);
		if(nm == null) {
				nm = NetworkTable.getNetworkMap(calendar);
				mapnet.put(table, nm);
		}
		return nm;
	}
	
	
	public static void main(String[] args) {
		
		Calendar cal = new GregorianCalendar(2013,8,01);
		RegionMap nm = getNetworkMap(cal);
		System.out.println(nm.getName());
		System.out.println(nm.getRegion("4050919751"));
	}
}
