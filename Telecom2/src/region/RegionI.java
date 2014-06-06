package region;

import java.io.Serializable;

import org.gps.utils.LatLonPoint;

import utils.GeomUtils;

import com.vividsolutions.jts.geom.Geometry;

public abstract class RegionI implements Serializable {
	protected String name;
	protected String description;		
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
	
	public String getDescription(){
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
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
		return toKml(color,color);
	}
	
	public String toKml(String areacolor, String bordercolor) {
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
				"<description><![CDATA["+description+"]]></description>" +
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
