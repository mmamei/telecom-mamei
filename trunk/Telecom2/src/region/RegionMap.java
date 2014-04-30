package region;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import network.NetworkCell;
import network.NetworkMap;
import network.NetworkMapFactory;
import utils.Colors;
import utils.CopyAndSerializationUtils;
import utils.FileUtils;
import utils.GeomUtils;
import utils.Logger;
import visual.kml.KML;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

public class RegionMap implements Serializable {
	
	private String name;
	private Map<String,Region> rm;
	
	public RegionMap(String name) {
		this.name = name;
		rm = new HashMap<String,Region>();
	}
	
	public void add(Region r) {
		rm.put(r.getName(), r);
	}
	
	public String getName() {
		return name;
	}
	
	public int getNumRegions() {
		return rm.size();
	}
	
	public Region getRegion(String name) {
		return rm.get(name);
	}
	
	private transient Map<Integer,Region> int2region = null;
	public Region getRegion(int i) {
		if(int2region == null) {
			int2region = new HashMap<Integer,Region>();
			int k=0;
			for(Region r: rm.values()) {
				int2region.put(k, r);
				k++;
			}	
		}
		return int2region.get(i);
	}
	
	public Collection<Region> getRegions(){
		return rm.values();
	}
	
	public static final double earth_radius = 6372.795477598; // km
	public static final double search_radius = 10; // km
	public static final double deg_radius = Math.toDegrees(search_radius/earth_radius);
	
	public Region get(double lon, double lat) {
		Geometry p = GeomUtils.getCircle(lon, lat, 100);
		for(Region r: rm.values()) {
			if(p.intersects(r.getGeom())) return r;
		}
		
		return null;		
	}
	
	private static transient Map<Long,float[]> cache_intersection = new HashMap<Long,float[]>();
	
	public float[] computeAreaIntersection(long celllac, long time) {
		float[] area_intersection = cache_intersection.get(celllac);
		if(area_intersection != null) return area_intersection;
		area_intersection = new float[this.getNumRegions()];
		NetworkMap nm = NetworkMapFactory.getNetworkMap(time);
		NetworkCell nc = nm.get(celllac);
		Polygon circle = GeomUtils.getCircle(nc.getBarycentreLongitude(), nc.getBarycentreLatitude(), nc.getRadius());
		double ca = Math.PI * Math.pow(nc.getRadius(),2);
		int i=0;
		for(Region r: this.getRegions()) {
			Geometry a = r.getGeom().intersection(circle);
			area_intersection[i] = (float)(GeomUtils.geoArea(a)/ca);
			i++;
		}
		
		// normailze to 1
		float sum = 0;
		for(float f: area_intersection)
			sum += f;
		for(i=0; i<area_intersection.length;i++)
			area_intersection[i] = area_intersection[i] / sum;
		
		cache_intersection.put(celllac, area_intersection);
		
		return area_intersection;
	}
	
	private static transient Map<Long,Region> cache_closest = new HashMap<Long,Region>();
	public Region getClosest(long celllac, long time) {
		Region reg = cache_closest.get(celllac);
		if(reg != null) return reg;
		
		Region closest = null;
		float max_intersection = 0;
		NetworkMap nm = NetworkMapFactory.getNetworkMap(time);
		NetworkCell nc = nm.get(celllac);
		Polygon circle = GeomUtils.getCircle(nc.getBarycentreLongitude(), nc.getBarycentreLatitude(), nc.getRadius());
		double ca = Math.PI * Math.pow(nc.getRadius(),2);
		for(Region r: this.getRegions()) {
			Geometry a = r.getGeom().intersection(circle);
			float area = (float)(GeomUtils.geoArea(a)/ca);
			if(area > max_intersection) {
				max_intersection = area;
				closest = r;
			}
		}
		cache_closest.put(celllac, closest);
		
		return closest;
	}
	
	
	public Envelope getEnvelope() {
		Envelope e = new Envelope();
		for(Region r: getRegions()) {
			e.expandToInclude(r.getGeom().getEnvelopeInternal());
		}
		return e;
	}
	
	
	public List<Region> getOverlappingRegions(Geometry g) {
		List<Region> overlapping_regions = new ArrayList<Region>();
		for(Region r: rm.values()) {
			if(r.getGeom().intersects(g))
				overlapping_regions.add(r);
		}
		return overlapping_regions;
	}
	
	
	
	
	public void printKML() throws Exception  {
		File d = FileUtils.createDir("BASE/RegionMap");
		
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(d.getAbsolutePath()+"/"+name+".kml")));
		KML kml = new KML();
		kml.printHeaderFolder(out, name);
		int index = 0;
		for(Region r: rm.values()) {
			out.println(r.toKml(Colors.RANDOM_COLORS[index],Colors.RANDOM_COLORS[index],r.getName()));
			index++;
			if(index >= Colors.RANDOM_COLORS.length) index = 0;
		}
		kml.printFooterFolder(out);
		out.close();
	}
	
	
	public String getKMLBorders() {
		StringBuffer sb = new StringBuffer();
		int index = 0;
		for(Region r: rm.values()) {
			sb.append(r.toKml("01ffffff","ffffffff",r.getName())+"\n");
			index++;
			if(index >= Colors.RANDOM_COLORS.length) index = 0;
		}
		return sb.toString();
	}
	
	public String toKml(String color) {
		StringBuffer sb = new StringBuffer();
		int index = 0;
		for(Region r: rm.values()) {
			sb.append(r.toKml(color,color,r.getName())+"\n");
			index++;
			if(index >= Colors.RANDOM_COLORS.length) index = 0;
		}
		return sb.toString();
	}
	
	
	
	public static void main(String[] args) throws Exception {
		process("Venezia");
		Logger.logln("Done!");
	}
	
	public static void process(String region) throws Exception {
		
		File input_obj_file = FileUtils.getFile("RegionMap/"+region+".ser");
		if(!input_obj_file.exists()) {
			System.out.println(input_obj_file+" does not exist... run the region parser first!");
			System.exit(0);
		}
			
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(input_obj_file); 
		Logger.logln(region+" has "+rm.getNumRegions()+" regions");
		rm.printKML();
		/*
		System.out.println(rm.get(8.46050279447007,44.67433775848695).getName()); // should be ACQUI TERME
		System.out.println(rm.get(7.3591814195141,44.51233268378211).getName()); // should be BUSCA
		System.out.println(rm.get(7.547977490302962,44.60851381725961).getName()); // should be MANTA
		System.out.println(rm.get(7.777669845628534,45.2910107499951).getName()); // should be SAN GIORGIO CANAVESE
		System.out.println(rm.get(7.777542202427386,45.29121254507205).getName()); // should be LUSIGLIè
		*/		
	}
	
}
