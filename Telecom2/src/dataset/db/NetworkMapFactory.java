package dataset.db;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import region.RegionMap;
import dataset.NetworkMapFactoryI;
import dataset.db.insert.NetworkTable;

class NetworkMapFactory implements NetworkMapFactoryI {
	
	
	private static NetworkMapFactory nmf;
	private static final SimpleDateFormat F = new SimpleDateFormat("dd/MM/yyyy");
	private static Map<String,RegionMap> mapnet = new HashMap<String,RegionMap>();
	
	private NetworkMapFactory() {
	}
	
	static NetworkMapFactory getInstance() {
		if(nmf == null) 
			nmf = new  NetworkMapFactory();
		return nmf;
	}
	
	
	
	private String getCalString(String f) {
		//dfl_network_20130516.bin
		String yyyy = f.substring(12,16);
		String MM = f.substring(16,18);
		String dd = f.substring(18,20);
		return dd+"/"+MM+"/"+yyyy;
	}

	
	public Calendar getCalendar(String f) {
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
	
	public RegionMap getNetworkMap(long time) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		return getNetworkMap(cal);
	}
	
	
	public RegionMap getNetworkMap(Calendar calendar) {
		
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
		NetworkMapFactory nmf = getInstance();
		Calendar cal = new GregorianCalendar(2013,8,01);
		RegionMap nm = nmf.getNetworkMap(cal);
		System.out.println(nm.getName());
		System.out.println(nm.getRegion("4050919751"));
	}
}
