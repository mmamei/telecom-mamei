package area.region;

import java.io.Serializable;

import utils.GeomUtils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

public class Region implements Serializable {
	private String name;
	private String kmlcoordinates;
	private double[][] bbox;
	private double[] center;
	private Geometry g;
	
	public Region(String name, String kmlcoordinates) {
		this.name = name.replaceAll("\\\\", "");
		this.kmlcoordinates = kmlcoordinates;
		process();
	}
	
	public Region(String name, double[][] borderLonLat) {
		this.name = name.replaceAll("\\\\", "");
		
		StringBuffer sb = new StringBuffer();
		for(double[] ll: borderLonLat) {
			sb.append(ll[0]+","+ll[1]+",0 ");
		}
		this.kmlcoordinates = sb.toString();
		process();
	}
	
	public Region(String name, Geometry g) {
		this.name = name.replaceAll("\\\\", "");
		Coordinate[] cs = g.getCoordinates();
		StringBuffer sb = new StringBuffer();
		for(Coordinate c: cs) 
			sb.append(c.x+","+c.y+",0 ");
		this.kmlcoordinates = sb.toString();
		
		
		double minlon = Double.MAX_VALUE, maxlon = -Double.MAX_VALUE, minlat = Double.MAX_VALUE, maxlat = -Double.MAX_VALUE;
		String x = GeomUtils.kmlToOpenGISCoordinates(kmlcoordinates);
		if(x.equals("")) return;
		String[] coord = x.split(",");
		for(String c: coord) {
			double lon = Double.parseDouble(c.substring(0,c.indexOf(" ")));
			double lat = Double.parseDouble(c.substring(c.indexOf(" ")+1));
			minlon = Math.min(minlon, lon);
			minlat = Math.min(minlat, lat);
			maxlon = Math.max(maxlon, lon);
			maxlat = Math.max(maxlat, lat);
		}
		
		bbox = new double[][]{{minlon,minlat},{maxlon,maxlat}};
		center = new double[]{(minlon+maxlon)/2,(minlat+maxlat)/2};
		
		this.g = g;
	}
	
	
	
	public double getCenterLon() {
		return center[0];
	}
	public double getCenterLat() {
		return center[1];
	}
	
	public Geometry getGeom() {
		return g;
	}
	
	public String getName() {
		return name;
	}
	
	private void process() {
		double minlon = Double.MAX_VALUE, maxlon = -Double.MAX_VALUE, minlat = Double.MAX_VALUE, maxlat = -Double.MAX_VALUE;
		String x = GeomUtils.kmlToOpenGISCoordinates(kmlcoordinates);
		if(x.equals("")) return;
		String[] coord = x.split(",");
		for(String c: coord) {
			double lon = Double.parseDouble(c.substring(0,c.indexOf(" ")));
			double lat = Double.parseDouble(c.substring(c.indexOf(" ")+1));
			minlon = Math.min(minlon, lon);
			minlat = Math.min(minlat, lat);
			maxlon = Math.max(maxlon, lon);
			maxlat = Math.max(maxlat, lat);
		}
		
		bbox = new double[][]{{minlon,minlat},{maxlon,maxlat}};
		center = new double[]{(minlon+maxlon)/2,(minlat+maxlat)/2};
		
		
		try {
			g = new WKTReader().read("POLYGON (("+x+"))");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	

	public String toKml(String color) {
		return toKml(color,color,"");
	}
	
	public String toKml(String areacolor, String bordercolor, String description) {
		String id = areacolor+"-"+bordercolor;
		return "<Style id=\""+id+"\">" +
				"<LineStyle>" +
				"<color>"+bordercolor+"</color>" +
				"</LineStyle>" +
				"<PolyStyle>" +
				"<color>"+areacolor+"</color>" +
				"</PolyStyle>" +
				"</Style>" +
				"<Placemark>" +
				"<name>"+name+"</name>" +
				"<description>"+description+"</description>" +
				"<styleUrl>#"+id+"</styleUrl>" +
				"<Polygon>" +
				"<outerBoundaryIs>" +
				"<LinearRing>" +
				"<coordinates>"+kmlcoordinates+"</coordinates>" +
				"</LinearRing>" +
				"</outerBoundaryIs>" +
				"</Polygon>" +
				"</Placemark>";
	}
	
	
	public boolean equals(Object o) {
		return name.equals(((Region)o).name);
	}
	public int hashCode(){
		return name.hashCode();
	}
	
}
