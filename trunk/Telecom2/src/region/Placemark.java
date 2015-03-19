package region;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import org.gps.utils.LatLonPoint;
import org.gps.utils.LatLonUtils;

import utils.Config;
import utils.GeomUtils;
import utils.Logger;
import visual.kml.KML;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

import dataset.file.DataFactory;



public class Placemark extends RegionI {
	
	private RegionMap nm;
	private double radius;
	public Set<String> cellsAround;
	public boolean ring = false;
	
	public Placemark(String name, double[] centerLatLon, double r) {
		this.name = name;
		this.centerLatLon = centerLatLon;
		this.radius = r;
		this.nm = DataFactory.getNetworkMapFactory().getNetworkMap(Config.getInstance().pls_start_time);
		this.cellsAround = getCellsAround();
	}
	
	public Placemark(String name, Set<String> celllacs) {
		this.name = name;
		this.nm = DataFactory.getNetworkMapFactory().getNetworkMap(Config.getInstance().pls_start_time);
		this.cellsAround = celllacs;
		centerLatLon = new double[2];
		int cont = 0;
		for(String celllac: celllacs) {
			RegionI r = nm.getRegion(celllac);
			if(r!=null) {
				centerLatLon[0] += r.centerLatLon[0];
				centerLatLon[1] += r.centerLatLon[1];
				cont++;
			}
			else {
				Logger.logln(celllac+" not found!");
			}
		}
		centerLatLon[0] = centerLatLon[0] / cont;
		centerLatLon[1] = centerLatLon[1] / cont;
		radius = this.getMaxDist();
	}
	
	
	public double[][] getBboxLonLat() {
		LatLonPoint c = this.getCenterPoint();
		LatLonPoint ll = LatLonUtils.getPointAtDistance(c, 225, radius*Math.sqrt(2));
		LatLonPoint tr = LatLonUtils.getPointAtDistance(c, 45, radius*Math.sqrt(2));
		return new double[][]{{ll.getLongitude(),ll.getLatitude()},{tr.getLongitude(),tr.getLatitude()}};
	}
	
	public Geometry getGeom() {
		return GeomUtils.getCircle(getLatLon()[1], getLatLon()[0], radius);
	}
	
	
	public String toString() {
		String n = ring? "ring_"+name : name;
		return n+"_"+(int)radius;
	}
	
	public Placemark clone() {
		Placemark c = new Placemark(name,centerLatLon,radius);
		if(ring) c.changeRadiusRing(radius);
		return c;
	}
	
	public boolean equals(Object o) {
		Placemark op = (Placemark)o;
		return name.equals(op.name) && radius == op.radius && ring == op.ring;
	}
	
	public double getRadius() {
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
			RegionI nc = nm.getRegion(c);
			Polygon p = GeomUtils.getCircle(nc.getLatLon()[1],nc.getLatLon()[0],nc.getRadius());
			if(u == null) u = p;
			else u = u.union(p);
		}
		double area = u == null ? 0 : GeomUtils.geoArea(u);
		return area;
	}
	
	public double getSumRadii() {
		double a = 0;
		for(String c: cellsAround) {
			double r = nm.getRegion(c).getRadius();
			a = a + r;
		}
		return a;
	}
	
	public double getMaxDist() {
		double min_dist = Double.MAX_VALUE;
		double max_dist = 0;
		LatLonPoint center_point = getCenterPoint();
		for(String c: cellsAround) {
			RegionI nc = nm.getRegion(c);
			double d = LatLonUtils.getHaversineDistance(nc.getCenterPoint(),center_point);
			
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
	
	
	
	public static Placemark getPlacemark(String pname) {
		Placemark p = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(Config.getInstance().placemarks_file));
			String line;
			while((line = br.readLine())!=null) {
				if(line.startsWith("//") || line.trim().length() < 3) continue;
				String[] el = line.split(",");
				String name = el[0].trim();
				if(name.equals(pname)) {
					
					if(el[1].equals("coord"))
						p = new Placemark(pname,new double[]{Double.parseDouble(el[2].trim()), Double.parseDouble(el[3].trim())},Double.parseDouble(el[4].trim()));
					else if(el[1].equals("lac-cid")) {
						Set<String> celllacs = new HashSet<String>();
						int cont = 0;
						for(int i=2; i<el.length;i=i+2) {
							long lac = Long.parseLong(el[i]);
							long cell_id = Long.parseLong(el[i+1]);
							String celllac = String.valueOf(lac*65536+cell_id);
							celllacs.add(celllac);
							cont ++;
						}
						System.out.println(cont+" cells in "+pname);
						p = new Placemark(pname,celllacs);
					}
					else {
						Logger.logln("Format not supported! Check placemarks.csv file!");
						System.exit(0);
					}
					break;
				}
			}
			br.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return p;
	}
	
	
	
	
	
	private Set<String> getCellsAround() { 
		Set<String> cellsAround = new HashSet<String>();
		double bbox = 1;
		double[] ll = new double[]{getLatLon()[0]-bbox,getLatLon()[1]-bbox};
		double[] tr = new double[]{getLatLon()[0]+bbox,getLatLon()[1]+bbox};
		//Set<RegionI> ncells = nm.getRegionsIn(ll, tr);
		LatLonPoint center_point = getCenterPoint();
		for(RegionI nc: nm.getRegions()) {
			//Polygon c = GeomUtils.getCircle(nc.getBarycentreLongitude(), nc.getBarycentreLatitude(), nc.getRadius());
			//if(c.getEnvelope().overlaps(this.g.getEnvelope()))
				//cellsAround.add(String.valueOf(nc.getCellac()));
			if( (LatLonUtils.getHaversineDistance(nc.getCenterPoint(),center_point) - nc.getRadius()) < radius )
				cellsAround.add(String.valueOf(nc.getName()));
		}
		return cellsAround;
	}
	
	
	private Set<String> getCellsAroundRing() { 
		Set<String> cellsAround = new HashSet<String>();
		double bbox = 1;
		double[] ll = new double[]{getLatLon()[0]-bbox,getLatLon()[1]-bbox};
		double[] tr = new double[]{getLatLon()[0]+bbox,getLatLon()[1]+bbox};
		Set<RegionI> ncells = nm.getRegionsIn(ll, tr);
		LatLonPoint center_point = getCenterPoint();
		for(RegionI nc: ncells) {
			double d = LatLonUtils.getHaversineDistance(nc.getCenterPoint(),center_point) - nc.getRadius();
			if(d < 1500 && d > radius)
				cellsAround.add(String.valueOf(nc.getName()));
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
			out.println(nm.getRegion(cell).toKml("#7f770077","#ff770077"));
		out.println(super.toKml("ff00ff00"));
		
		kml.printFooterFolder(out);
		out.close();
	} 
	
	
	public static final int MAX_R = 1500;
	public static final int MIN_R = -500;
	public static final int STEP = 100;
	public static void main(String[] args) throws Exception {
		//Map<String,Double> bestRadius = PlacemarkRadiusExtractor.readBestR(true);	
		//initPlacemaks();
		//NetworkMap nm = NetworkMapFactory.getNetworkMap();
		
		Config.getInstance().pls_start_time = new GregorianCalendar(2014,Calendar.AUGUST,1,0,0,0);
		Config.getInstance().pls_end_time = new GregorianCalendar(2014,Calendar.AUGUST,31,23,59,59);
		Placemark p = getPlacemark("LecceBigBig");
		
		//System.out.println(p.getNumCells());
		//System.out.println(p.contains("4018584023"));
		//System.out.println(nm.get(Long.parseLong("4018584023")));
		File dir =new File(Config.getInstance().base_folder+"/Placemark");
		dir.mkdirs();
		String f = dir+"/"+p.name+".kml";
		System.out.println(f);
		p.printKML(f);
		
			
		Logger.logln("Done!");
	}	
}
