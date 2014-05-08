package region;

import java.io.Serializable;

import org.gps.utils.LatLonPoint;

import utils.GeomUtils;

import com.vividsolutions.jts.geom.Geometry;

public abstract class RegionI implements Serializable {
	public String name;
	public double[] centerLatLon;
	
	
	public LatLonPoint getCenterPoint() {
		return new LatLonPoint(centerLatLon[0],centerLatLon[1]);
	}
	
	public boolean equals(Object o) {
		return name.equals(((RegionI)o).name);
	}
	public int hashCode(){
		return name.hashCode();
	}
	
	public double getCenterLon() {
		return centerLatLon[1];
	}
	public double getCenterLat() {
		return centerLatLon[0];
	}
	
	public abstract Geometry getGeom();

	public String getName() {
		return name;
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
				"<coordinates>"+GeomUtils.geom2Kml(getGeom())+"</coordinates>" +
				"</LinearRing>" +
				"</outerBoundaryIs>" +
				"</Polygon>" +
				"</Placemark>";
	}
}
