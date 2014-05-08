package region;

import java.io.Serializable;

import org.gps.utils.LatLonPoint;

import utils.GeomUtils;

import com.vividsolutions.jts.geom.Geometry;

public abstract class RegionI implements Serializable {
	protected String name;
	protected double[] centerLatLon;
	
	
	
	public boolean equals(Object o) {
		return name.equals(((RegionI)o).name);
	}
	public int hashCode(){
		return name.hashCode();
	}
	
	public String getName() {
		return name;
	}
	public double[] getLatLon() {
		return centerLatLon;
	}
	
	public LatLonPoint getCenterPoint() {
		return new LatLonPoint(centerLatLon[0],centerLatLon[1]);
	}
	
	
	public abstract Geometry getGeom();

	public abstract double getRadius();
	
	public abstract double[][] getBboxLonLat();
	
	
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
				"<coordinates>"+GeomUtils.geom2Kml(getGeom())+"</coordinates>" +
				"</LinearRing>" +
				"</outerBoundaryIs>" +
				"</Polygon>" +
				"</Placemark>";
	}
}
