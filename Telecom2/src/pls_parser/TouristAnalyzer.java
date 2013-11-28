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
import area.Placemark;
import area.region.Region;
import area.region.RegionMap;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

public class  TouristAnalyzer extends BufferAnalyzer {
	
	public static final int TIM = 0;
	public static final int ROAMING = 1;
	public static final int ALL = 2;
	public static final String[] U_SEGMENT = new String[]{"TIM","ROAMING","ALL"};
	
	private Set<String> user_set;
	private RegionMap map;
	
	Map<String,Double> space_density;
	Map<String,Double> time_density;
	
	Placemark p;
	int min_days, max_days;
	int user_segment;
	
	public TouristAnalyzer(String user_file, String region_map, Placemark p, int min_days, int max_days, int user_segment) throws Exception {
		this.p = p;
		this.min_days = min_days;
		this.max_days = max_days;
		this.user_segment = user_segment;
		map = (RegionMap)CopyAndSerializationUtils.restore(FileUtils.getFile(region_map));
		
		
		user_set = new HashSet<String>();
		BufferedReader br = FileUtils.getBR(user_file);
		String line;
		// 32b0aac184bab5813b9e294b789635b1e935d081b5b432772f16c9fddb4b5,22201,1,1372602476086,1372602476086,Sun Jun 30 16:27:56 CEST 2013,Sun Jun 30 16:27:56 CEST 2013
		while((line = br.readLine())!=null) {
			String[] e = line.split(",");
			if(user_segment == TIM && !e[1].equals("22201")) continue;
			if(user_segment == ROAMING && e[1].equals("22201")) continue;
			Long st = Double.valueOf(e[3]).longValue();
			Long et = Double.valueOf(e[4]).longValue();
			int days = (int)((et - st) / (1000*3600*24));
			if(min_days > days || days > max_days) continue;
			
			user_set.add(e[0]);
		}
		br.close();
		
		space_density = new HashMap<String,Double>();
		time_density = new HashMap<String,Double>();
	}

	String[] fields;
	String username;
	String imsi;
	String celllac;
	long timestamp;
	
	NetworkMap nm = NetworkMapFactory.getNetworkMap();
	
	
	/*
	 * this cache stores
	 * for a given networkcell, a map associating 
	 * for each region the fraction of the area of the circle matching with the region
	 */
	
	Map<String,Map<String,Double>> cache_intersection = new HashMap<String,Map<String,Double>>();
	
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
			area_intersection = new HashMap<String,Double>();
			NetworkCell nc = nm.get(Long.parseLong(celllac));
			Polygon circle = GeomUtils.getCircle(nc.getBarycentreLongitude(), nc.getBarycentreLatitude(), nc.getRadius());
			double ca = Math.PI * Math.pow(nc.getRadius(),2);
			for(Region r: map.getRegions()) {
				boolean overlaps = r.getGeom().overlaps(circle);
				if(overlaps) {
					Geometry a = r.getGeom().intersection(circle);
					area_intersection.put(r.getName(), a.getArea()/ca);
				}
			}
			cache_intersection.put(celllac, area_intersection);
		}
		
		for(String rname : area_intersection.keySet()) {
			Double x = space_density.get(rname);
			if(x == null) x = 0.0;
			space_density.put(rname, x + area_intersection.get(rname));
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
	
	

	public void finish() {
		String dir = "C:"+Config.getInstance().base_dir+"/TouristAnalyzer";
		new File(dir).mkdirs();
		CopyAndSerializationUtils.save(new File(dir+"/"+p.name+"_"+min_days+"_"+max_days+"_"+U_SEGMENT[user_segment]+"_space.ser"), space_density);
		CopyAndSerializationUtils.save(new File(dir+"/"+p.name+"_"+min_days+"_"+max_days+"_"+U_SEGMENT[user_segment]+"_time.ser"), time_density);
	}
	
	public static void main(String[] args) throws Exception {
		int us = ROAMING;
		int min_days = 1;
		int max_days = 5;
		TouristAnalyzer ba = new TouristAnalyzer("UserEventCounterDetailed/Venezia_trim3.csv","RegionMap/Venezia.ser",Placemark.getPlacemark("Venezia"),min_days, max_days,us);
	    PLSParser.parse(ba);
	    ba.finish();
		Logger.logln("Done");
	}
}
