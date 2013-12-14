package pls_parser;

import java.io.BufferedReader;
import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import network.NetworkCell;
import network.NetworkMap;
import network.NetworkMapFactory;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.FileUtils;
import utils.GeomUtils;
import utils.Logger;
import analysis.tourist.TouristActivity;
import analysis.tourist.TouristData;
import area.Placemark;
import area.region.Region;
import area.region.RegionMap;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;


/*
 * 
 PRE DELETE. THIS CLASS IN NOW USELESS
 * 
 */

public class  TouristAnalyzer2 extends BufferAnalyzer {

	private static final boolean TIME_DETAIL = false;
	
	
	private Set<String> user_set;
	private RegionMap map;
	
	Map<String,Double> sd;
	Map<String,Double> time_density;
	
	Placemark p;
	
	
	NetworkMap nm = NetworkMapFactory.getNetworkMap();
	Map<String,Map<String,Double>> cache_intersection = new HashMap<String,Map<String,Double>>();
	
	public TouristAnalyzer2(String user_file, String region_map, Placemark p) throws Exception {
		this.p = p;
		map = (RegionMap)CopyAndSerializationUtils.restore(FileUtils.getFile(region_map));
		sd = new HashMap<String,Double>();
		for(Region r: map.getRegions())
			sd.put(r.getName(), 0.0);
		
		time_density = new HashMap<String,Double>();
		
		
		
		user_set = new HashSet<String>();
		BufferedReader br = FileUtils.getBR(user_file);
		String line;
		// 32b0aac184bab5813b9e294b789635b1e935d081b5b432772f16c9fddb4b5,22201,1,1372602476086,1372602476086,Sun Jun 30 16:27:56 CEST 2013,Sun Jun 30 16:27:56 CEST 2013
		
		
		while((line = br.readLine())!=null) {
			String[] e = line.split(",");
			if(TouristActivity.skip(e[1],Integer.parseInt(e[2]),Integer.parseInt(e[3]))) continue;
			user_set.add(e[0]);
			
			//if(e[2].equals("2"))
			//System.out.println(e[0]+" -- "+e[2]);
			
			for(int i=4;i<e.length;i++) {
				// 2013-5-23:Sun:13:4018542484
				
				
				String[] x = e[i].split(":");
				String cellac = x[3];
				Map<String,Double> area_intersection = cache_intersection.get(cellac);
				if(area_intersection == null) {
					area_intersection = computeIntersection(cellac,map);		
					cache_intersection.put(cellac, area_intersection);
				}
				
				for(String rname : area_intersection.keySet()) {
					double v = area_intersection.get(rname);
					sd.put(rname, sd.get(rname) + v);
				}
			}
			
		}
		br.close();
		finish();
		
		System.exit(0);
	}

	
	
	private Map<String,Double> computeIntersection(String cellac, RegionMap map) {
		Map<String,Double>  area_intersection = new HashMap<String,Double>();
		float[] x = TouristData.computeAreaIntersection(cellac, map);
		for(int i=0; i<x.length;i++) {
			if(x[i] > 0) {
				area_intersection.put(map.getRegion(i).getName(),(double)x[i]);
			}
		}
		return area_intersection;
	}
	
	
	
	
	

	String[] fields;
	String username;
	String imsi;
	String celllac;
	long timestamp;
	
	
	
	
	/*
	 * this cache stores
	 * for a given networkcell, a map associating 
	 * for each region the fraction of the area of the circle matching with the region
	 */
	
	
	
	public void analyze(String line) {
		fields = line.split("\t");
		username = fields[0];
		imsi = fields[1];
		celllac = fields[2];
		timestamp = Long.parseLong(fields[3]);
		
		if(!user_set.contains(username)) return;
		if(!p.contains(celllac)) return;
		
		Map<String,Double> area_intersection = cache_intersection.get(celllac);
		if(area_intersection == null) {
			area_intersection = computeIntersection(celllac,map);		
			cache_intersection.put(celllac, area_intersection);
		}
		
		for(String rname : area_intersection.keySet()) {
			Double x = sd.get(rname);
			if(x == null) x = 0.0;
			sd.put(rname, x + area_intersection.get(rname));
		}
		
		// update the time distribution
		String k = getTimeKey(timestamp);
		Double x = time_density.get(k);
		if(x == null) x = 0.0;
		time_density.put(k, x+1);
	}
	
	
	
	
	Calendar cal = new GregorianCalendar();
	private String getTimeKey(long timestamp) {
		cal.setTimeInMillis(timestamp);
		return cal.get(Calendar.YEAR)+"-"+cal.get(Calendar.MONTH)+"-"+cal.get(Calendar.DAY_OF_MONTH)+":"+cal.get(Calendar.HOUR_OF_DAY);
	}
	
	
	static final String[] MONTHS = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	public void finish() {
		
		String time_suffix = "";
		if(TIME_DETAIL) {
			int sday =  getStartTime().get(Calendar.DAY_OF_MONTH);
			int smonth = getStartTime().get(Calendar.MONTH);
			int syear = getStartTime().get(Calendar.YEAR);
			
			int eday =  getEndTime().get(Calendar.DAY_OF_MONTH);
			int emonth = getEndTime().get(Calendar.MONTH);
			int eyear = getEndTime().get(Calendar.YEAR);
			
			time_suffix = "["+sday+"-"+MONTHS[smonth]+"-"+syear+"_"+eday+"-"+MONTHS[emonth]+"-"+eyear+"]";
		}
		
		
		String dir = "C:"+Config.getInstance().base_dir+"/TouristAnalyzer";
		new File(dir).mkdirs();
		CopyAndSerializationUtils.save(new File(dir+"/"+p.name+"_"+TouristActivity.MIN_DAYS+"_"+TouristActivity.MAX_DAYS+"_"+TouristActivity.U_SEGMENT[TouristActivity.U_SEG]+time_suffix+"_space.ser"), sd);
		CopyAndSerializationUtils.save(new File(dir+"/"+p.name+"_"+TouristActivity.MIN_DAYS+"_"+TouristActivity.MAX_DAYS+"_"+TouristActivity.U_SEGMENT[TouristActivity.U_SEG]+time_suffix+"_time.ser"), time_density);
	}
	
	public static void main(String[] args) throws Exception {
		Placemark p = Placemark.getPlacemark("Venezia");
		TouristAnalyzer2 ba = new TouristAnalyzer2("UserEventCounter/"+p.name+"_cellacXhour.csv","RegionMap/"+p.name+".ser",p);
	    PLSParser.parse(ba);
	    ba.finish();
	    //TouristActivity.process(p.name, 1, 5, ROAMING);
	    
		Logger.logln("Done");
	}
}
