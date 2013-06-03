package area;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import network.NetworkCell;
import network.NetworkMap;

import org.gps.utils.LatLonPoint;
import org.gps.utils.LatLonUtils;

import analysis.presence_at_event.ReleventCellsExtractor;

import utils.Config;
import utils.Logger;
import visual.Kml;



public class Placemark {
	
	private static Map<String,Placemark> PLACEMARKS = null;
	
	public String name;
	public double[] center;
	public LatLonPoint center_point;
	public double radius;
	public Set<String> cellsAround;
	
	public Placemark(String name, double[] point, double radius) {
		this.name = name;
		this.center = point;
		center_point = new LatLonPoint(center[0],center[1]);
		this.radius = radius;
		this.cellsAround = getCellsAround();
	}
	
	public Placemark clone() {
		return new Placemark(name,center,radius);
	}
	
	public boolean equals(Object o) {
		Placemark op = (Placemark)o;
		return name.equals(op.name) && radius == op.radius;
	}
	
	
	
	public void changeRadius(double r) {
		this.radius = r;
		this.cellsAround = getCellsAround();
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
			String[] el = line.split(",");
			String name = el[0].trim();
			double lat = Double.parseDouble(el[1].trim());
			double lon = Double.parseDouble(el[2].trim());
			double r = Double.parseDouble(el[3].trim());
			PLACEMARKS.put(name, new Placemark(name, new double[]{lat,lon},r));
		}
		br.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private Set<String> getCellsAround() { 
		Set<String> cellsAround = new HashSet<String>();
		NetworkMap nm = NetworkMap.getInstance();
		double bbox = 1;
		double[] ll = new double[]{center[0]-bbox,center[1]-bbox};
		double[] tr = new double[]{center[0]+bbox,center[1]+bbox};
		Set<NetworkCell> ncells = nm.getCellsIn(ll, tr);
		for(NetworkCell nc: ncells) 
			if( (LatLonUtils.getHaversineDistance(nc.getPoint(),center_point) - nc.getRadius()) < radius )
				cellsAround.add(String.valueOf(nc.getCellac()));
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
		Kml kml = new Kml();
		String name = file.substring(file.lastIndexOf("/")+1,file.lastIndexOf("."));
		kml.printHeaderFolder(out, name);
			
		
		NetworkMap nm = NetworkMap.getInstance();
		for(String cell: cellsAround) 
			out.println(nm.get(Long.parseLong(cell)).toKml());
		
		
		out.println("<Style id=\"placemark\">");
		out.println("<IconStyle>");
		out.println("<color>ff00ff00</color>");
		out.println("<Icon>");
		out.println("<href>http://maps.google.com/mapfiles/kml/shapes/target.png</href>");
		out.println("</Icon>");
		out.println("</IconStyle>");
		out.println("</Style>");
			
		out.println("<Placemark>");
		out.println("<name>"+name+"</name>");
		out.println("<styleUrl>#placemark</styleUrl>");
		out.println("<Point>");
		out.println("<coordinates>"+center[1]+","+center[0]+",0</coordinates>");
		out.println("</Point>");
		out.println("</Placemark>");
		
		kml.printFooterFolder(out);
		out.close();
	} 
	
	
	
	public static void main(String[] args) throws Exception {
		initPlacemaks();
		for(String name: PLACEMARKS.keySet()) {
			Placemark x = Placemark.getPlacemark(name);
			x.changeRadius(-300);
			String dir = Config.getInstance().base_dir+"/Placemark";
			File d = new File(dir);
			if(!d.exists()) d.mkdirs();
			x.printKML(dir+"/"+x.name+"_"+x.radius+".kml");	
		}
		Logger.logln("Done!");
	}
	
}
