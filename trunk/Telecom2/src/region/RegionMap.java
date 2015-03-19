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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gps.utils.LatLonPoint;
import org.gps.utils.LatLonUtils;

import utils.Colors;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.GeomUtils;
import utils.Logger;
import visual.kml.KML;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

import dataset.file.DataFactory;

public class RegionMap implements Serializable {
	
	protected String name;
	protected Map<String,RegionI> rm;
	private transient Map<Long,float[]> cache_intersection;
	private transient Map<String,RegionI> cache_closest;
	private transient Map<Integer,RegionI> int2region = null;
	
	public RegionMap(String name) {
		this.name = name;
		rm = new HashMap<String,RegionI>();
		cache_intersection = new HashMap<Long,float[]>();
		cache_closest = new HashMap<String,RegionI>();
	}
	
	
	public void addAll(Map<String,RegionI> m) {
		rm.putAll(m);
	}
	
	public void add(RegionI r) {
		rm.put(r.getName(), r);
	}
	
	public String getName() {
		return name;
	}
	
	public int getNumRegions() {
		return rm.size();
	}
	
	public RegionI getRegion(String name) {
		return rm.get(name);
	}
	
	public RegionI getRegion(int i) {
		if(int2region == null) {
			int2region = new HashMap<Integer,RegionI>();
			int k=0;
			for(RegionI r: rm.values()) {
				int2region.put(k, r);
				k++;
			}	
		}
		return int2region.get(i);
	}
	
	public Collection<RegionI> getRegions(){
		return rm.values();
	}
	
	public static final double earth_radius = 6372.795477598; // km
	public static final double search_radius = 10; // km
	public static final double deg_radius = Math.toDegrees(search_radius/earth_radius);
	
	public RegionI get(double lon, double lat) {
		Geometry p = GeomUtils.getCircle(lon, lat, 100);
		for(RegionI r: rm.values()) {
			if(p.intersects(r.getGeom())) return r;
		}
		
		return null;		
	}
	
	
	public static final boolean CACHE_INTERSECTION = true;
	public float[] computeAreaIntersection(long celllac, long time) {
		
		float[] area_intersection = null;
		if(CACHE_INTERSECTION) {
			if(cache_intersection == null) cache_intersection = new HashMap<Long,float[]>();
			area_intersection = cache_intersection.get(celllac);
			if(area_intersection != null) return area_intersection;
		}
		area_intersection = new float[this.getNumRegions()];
		RegionMap nm = DataFactory.getNetworkMapFactory().getNetworkMap(time);
		
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		//System.out.println("********** "+c.getTime());
		
		RegionI nc = nm.getRegion(""+celllac);
		
		//System.out.println(nc);
		
		Polygon circle = GeomUtils.getCircle(nc.getLatLon()[1], nc.getLatLon()[0], nc.getRadius());
		double ca = Math.PI * Math.pow(nc.getRadius(),2);
		int i=0;
		for(RegionI r: this.getRegions()) {
			
			//System.out.println(r.getGeom()+" ==> "+nc.getLatLon()[1]+","+nc.getLatLon()[0]);
			
			Geometry a = r.getGeom().intersection(circle);
			area_intersection[i] = (float)(GeomUtils.geoArea(a)/ca);
			i++;
		}
		
		// normailze to 1
		float sum = 0;
		for(float f: area_intersection)
			sum += f;
		
		
		
		for(i=0; i<area_intersection.length;i++) {
			area_intersection[i] = area_intersection[i] / sum;
			//if(area_intersection[i] > 0) System.out.println(area_intersection[i]);
		}
		
		if(CACHE_INTERSECTION) cache_intersection.put(celllac, area_intersection);
		return area_intersection;
	}
	
	
	public RegionI getClosest(String celllac, long time) {
		if(cache_closest == null) cache_closest = new HashMap<String,RegionI>();
		RegionI reg = cache_closest.get(celllac);
		if(reg != null) return reg;
		
		RegionI closest = null;
		float max_intersection = 0;
		RegionMap nm = DataFactory.getNetworkMapFactory().getNetworkMap(time);
		RegionI nc = nm.getRegion(""+celllac);
		Polygon circle = GeomUtils.getCircle(nc.getLatLon()[1], nc.getLatLon()[0], nc.getRadius());
		double ca = Math.PI * Math.pow(nc.getRadius(),2);
		for(RegionI r: this.getRegions()) {
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
		for(RegionI r: getRegions()) {
			e.expandToInclude(r.getGeom().getEnvelopeInternal());
		}
		return e;
	}
	
	
	public List<RegionI> getOverlappingRegions(Geometry g) {
		List<RegionI> overlapping_regions = new ArrayList<RegionI>();
		for(RegionI r: rm.values()) {
			if(r.getGeom().intersects(g))
				overlapping_regions.add(r);
		}
		return overlapping_regions;
	}
	
	
	public Set<RegionI> getRegionsIn(double[] ll, double[] tr) {
		Set<RegionI> cells = new HashSet<RegionI>();
		
		double lon,lat;
		for(RegionI nc : rm.values()) {
			lat = nc.getLatLon()[0];
			lon = nc.getLatLon()[1];
			if(ll[0] < lat && lat < tr[0] && ll[1] < lon && lon < tr[1])
				cells.add(nc);
		}
		return cells;
	}
	
	
	public void printKML() throws Exception  {
		File d = new File(Config.getInstance().base_folder+"/RegionMap");
		d.mkdirs();
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(d.getAbsolutePath()+"/"+name+".kml")));
		KML kml = new KML();
		kml.printHeaderFolder(out, name);
		int index = 0;
		for(RegionI r: rm.values()) {
			out.println(r.toKml(Colors.RANDOM_COLORS[index],Colors.RANDOM_COLORS[index]));
			index++;
			if(index >= Colors.RANDOM_COLORS.length) index = 0;
		}
		kml.printFooterFolder(out);
		out.close();
	}

	
	public void printKML(String file, double[] ll, double[] tr) throws Exception {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		KML kml = new KML();
		String name = file.substring(file.lastIndexOf("/")+1,file.lastIndexOf("."));
		kml.printHeaderFolder(out, name);
		Set<RegionI> cells = getRegionsIn(ll,tr);
		for(RegionI nc : cells)
			out.println(nc.toKml(""));
		kml.printFooterFolder(out);
		out.close();
	}
	
	
	// radius in meters
	public void printKML(String file, double[] center, double radius) throws Exception {
		double d = Math.toDegrees(radius/1000/earth_radius);
		double[] ll = new double[]{center[0]-d,center[1]-d};
		double[] tr = new double[]{center[0]+d,center[1]+d};
		printKML(file,ll,tr);
	}
	
	public String getKMLBorders() {
		StringBuffer sb = new StringBuffer();
		int index = 0;
		for(RegionI r: rm.values()) {
			sb.append(r.toKml("01ffffff","ffffffff")+"\n");
			index++;
			if(index >= Colors.RANDOM_COLORS.length) index = 0;
		}
		return sb.toString();
	}
	
	public String toKml(String color) {
		StringBuffer sb = new StringBuffer();
		int index = 0;
		for(RegionI r: rm.values()) {
			sb.append(r.toKml(color,color)+"\n");
			index++;
			if(index >= Colors.RANDOM_COLORS.length) index = 0;
		}
		return sb.toString();
	}
	
	
	
	public double getAvgRegionRadiusAround(LatLonPoint c, double radius){
		
		double dist = 0;
		double count = 0;
		for(RegionI nc : rm.values()) {
			if(LatLonUtils.getHaversineDistance(nc.getCenterPoint(), c) < radius) {
				dist += nc.getRadius();
				count++;
			}
		}
		return dist/count;
	}
	
	
	public int getNumRegions(LatLonPoint c, double radius) {
		int count = 0;
		for(RegionI nc : rm.values()) {
			if(LatLonUtils.getHaversineDistance(nc.getCenterPoint(), c) < radius) {
				count++;
			}
		}
		return count;
	}
	
	
	public static void main(String[] args) throws Exception {
		process("FIX_IvoryCoast");
		Logger.logln("Done!");
	}
	
	public static void process(String region) throws Exception {
		
		File input_obj_file = new File(Config.getInstance().base_folder+"/RegionMap/"+region+".ser");
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
