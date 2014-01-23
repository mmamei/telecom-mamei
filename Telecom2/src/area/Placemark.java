package area;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import network.NetworkCell;
import network.NetworkMap;
import network.NetworkMapFactory;

import org.gps.utils.LatLonPoint;
import org.gps.utils.LatLonUtils;

import utils.Config;
import utils.FileUtils;
import utils.GeomUtils;
import utils.Logger;
import visual.kml.KML;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;



public class Placemark {
	static NetworkMap NM = NetworkMapFactory.getNetworkMap();
	private static Map<String,Placemark> PLACEMARKS = null;
	
	public String region;
	public String name;
	public double[] center;
	public LatLonPoint center_point;
	private double radius;
	public Set<String> cellsAround;
	public boolean ring = false;
	
	public Placemark(String region, String name, double[] point, double radius) {
		this.region = region;
		this.name = name;
		this.center = point;
		center_point = new LatLonPoint(center[0],center[1]);
		this.radius = radius;
		this.cellsAround = getCellsAround();
	}
	
	public String toString() {
		String n = ring? "ring_"+name : name;
		return n+"_"+(int)radius;
	}
	
	public Placemark clone() {
		Placemark c = new Placemark(region,name,center,radius);
		if(ring) c.changeRadiusRing(radius);
		return c;
	}
	
	public boolean equals(Object o) {
		Placemark op = (Placemark)o;
		return name.equals(op.name) && radius == op.radius && ring == op.ring;
	}
	
	public double getR() {
		return radius;
	}
	
	public void changeRadius(double r) {
		ring = false;
		this.radius = r;
		this.cellsAround = getCellsAround();
	}
	
	public void changeRadiusRing(double r) {
		ring = true;
		this.radius = r;
		this.cellsAround = getCellsAroundRing();
	}
	
	
	public double getArea() {
		Geometry u = null;
		for(String c: cellsAround) {
			NetworkCell nc = NM.get(Long.parseLong(c));
			Polygon p = GeomUtils.getCircle(nc.getBarycentreLongitude(),nc.getBarycentreLatitude(),nc.getRadius());
			if(u == null) u = p;
			else u = u.union(p);
		}
		double area = u == null ? 0 : GeomUtils.geoArea(u);
		return area;
	}
	
	
	
	
	
	public double getSumRadii() {
		double a = 0;
		for(String c: cellsAround) {
			double r = NM.get(Long.parseLong(c)).getRadius();
			a = a + r;
		}
		return a;
	}
	
	public double getMaxDist() {
		double min_dist = Double.MAX_VALUE;
		double max_dist = 0;
		
		for(String c: cellsAround) {
			NetworkCell nc = NM.get(Long.parseLong(c));
			double d = LatLonUtils.getHaversineDistance(nc.getPoint(),center_point);
			
			max_dist = Math.max(max_dist, d + nc.getRadius());
			
			d = d - nc.getRadius();
			if(d < 0) d = 0;
			min_dist = Math.min(min_dist, d);
		}
		
		if(ring) return 1500 - min_dist >= 0 ? 1500 - min_dist : 0;
		else return max_dist;
	}
	
	
	public double getNumCells() {
		return cellsAround.size();
	}
	
	
	
	
	//double[][] bbox = new double[][]{{7.494789211677311, 44.97591738081519},{7.878659418860384, 45.16510171374535}};
	public double[][] getBBox() {
		double[][] bbox = new double[2][2];
		
		LatLonPoint bottom_left = LatLonUtils.getPointAtDistance(center_point, (180+45), radius);
		LatLonPoint top_right = LatLonUtils.getPointAtDistance(center_point, 45, radius);
		
		bbox[0][0] = bottom_left.getLongitude();
		bbox[0][1] = bottom_left.getLatitude();
		bbox[1][0] = top_right.getLongitude();
		bbox[1][1] = top_right.getLatitude();
		return bbox;
	}
	
	
	public static Placemark getPlacemark(String placemark) {
		if(PLACEMARKS == null) initPlacemaks();
		return PLACEMARKS.get(placemark);
	}
	
	public static void initPlacemaks() {
		PLACEMARKS = new HashMap<String,Placemark>();
		try {
		BufferedReader br = new BufferedReader(new FileReader(Config.getInstance().placemarks_file));
		String line;
		while((line = br.readLine())!=null) {
			if(line.startsWith("//") || line.trim().length() < 3) continue;
			String[] el = line.split(",");
			String region = el[0].trim();
			String name = el[1].trim();
			double lat = Double.parseDouble(el[2].trim());
			double lon = Double.parseDouble(el[3].trim());
			double r = Double.parseDouble(el[4].trim());
			PLACEMARKS.put(name, new Placemark(region,name, new double[]{lat,lon},r));
		}
		br.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private Set<String> getCellsAround() { 
		Set<String> cellsAround = new HashSet<String>();
		double bbox = 1;
		double[] ll = new double[]{center[0]-bbox,center[1]-bbox};
		double[] tr = new double[]{center[0]+bbox,center[1]+bbox};
		Set<NetworkCell> ncells = NM.getCellsIn(ll, tr);
		//for(NetworkCell nc: ncells) {
		for(NetworkCell nc: NM.getAll()) {
			if( (LatLonUtils.getHaversineDistance(nc.getPoint(),center_point) - nc.getRadius()) < radius )
				cellsAround.add(String.valueOf(nc.getCellac()));
		}
		return cellsAround;
	}
	
	
	private Set<String> getCellsAroundRing() { 
		Set<String> cellsAround = new HashSet<String>();
		double bbox = 1;
		double[] ll = new double[]{center[0]-bbox,center[1]-bbox};
		double[] tr = new double[]{center[0]+bbox,center[1]+bbox};
		Set<NetworkCell> ncells = NM.getCellsIn(ll, tr);
		for(NetworkCell nc: ncells) {
			double d = LatLonUtils.getHaversineDistance(nc.getPoint(),center_point) - nc.getRadius();
			if(d < 1500 && d > radius)
				cellsAround.add(String.valueOf(nc.getCellac()));
		}
		return cellsAround;
	}
	
	public boolean contains(long celllac) {
		return cellsAround.contains(String.valueOf(celllac));
	}
	
	public boolean contains(String celllac) {
		return cellsAround.contains(celllac);
	}
	
	public int numCells() {
		return cellsAround.size();
	}
	
	public void printKML(String file) throws Exception {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		KML kml = new KML();
		String name = file.substring(file.lastIndexOf("/")+1,file.lastIndexOf("."));
		kml.printHeaderFolder(out, name);
			
		for(String cell: cellsAround) 
			out.println(NM.get(Long.parseLong(cell)).toKml());
		
		
		out.println("<Style id=\"placemark\">");
		out.println("<IconStyle>");
		out.println("<color>ff00ff00</color>");
		out.println("<Icon>");
		out.println("<href>http://maps.google.com/mapfiles/kml/shapes/target.png</href>");
		out.println("</Icon>");
		out.println("</IconStyle>");
		out.println("</Style>");
			
		out.println("<Placemark>");
		String n = ring ? "ring_"+name : name;
		out.println("<name>"+n+"</name>");
		out.println("<styleUrl>#placemark</styleUrl>");
		out.println("<Point>");
		out.println("<coordinates>"+center[1]+","+center[0]+",0</coordinates>");
		out.println("</Point>");
		out.println("</Placemark>");
		
		//out.println(NM.get(Long.parseLong("4018584023")).toKml());
		
		kml.printFooterFolder(out);
		out.close();
	} 
	
	
	public static final int MAX_R = 1500;
	public static final int MIN_R = -500;
	public static final int STEP = 100;
	public static void main(String[] args) throws Exception {
		//Map<String,Double> bestRadius = PlacemarkRadiusExtractor.readBestR(true);	
		initPlacemaks();
		NetworkMap nm = NetworkMapFactory.getNetworkMap();
		Placemark p = PLACEMARKS.get("Venezia");
		System.out.println(p.getNumCells());
		System.out.println(p.contains("4018584023"));
		System.out.println(nm.get(Long.parseLong("4018584023")));
		String f = FileUtils.getFileS("Placemark")+"/"+p.name+".kml";
		System.out.println(f);
		p.printKML(f);
		
		Logger.logln("Done!");
	}	
}
