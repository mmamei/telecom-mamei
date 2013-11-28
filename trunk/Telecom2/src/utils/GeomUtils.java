package utils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.util.GeometricShapeFactory;

public class GeomUtils {
	
	
	public static void main(String[] args) {
		getCircle(10,10,200);
	}
	
	
	public static Polygon getCircle(double lon, double lat, double r) {
		String coord = kmlToOpenGISCoordinates(createPolygon(lon,lat,r,10,0,360));
		Polygon circle = null;
		try {
			circle = (Polygon) new WKTReader().read("POLYGON (("+coord+"))");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return circle;
	}
	
	
	public static String kmlToOpenGISCoordinates(String kmlcoordinates) {
		if(kmlcoordinates.equals("")) return "";
		String openGIScoordinates = kmlcoordinates.replaceAll(",", ";");
		openGIScoordinates = openGIScoordinates.replaceAll(";0 ", ",");
		openGIScoordinates = openGIScoordinates.replaceAll(";", " ");
		openGIScoordinates = openGIScoordinates.substring(0, openGIScoordinates.length()-1);
		return openGIScoordinates;
	}
	
	private static String createPolygon(double c_lon, double c_lat, double radius_form, int skip, int start_angle, int end_angle) {
		
		double lat1, long1;
		double d_rad;
		double radial, lat_rad, dlon_rad, lon_rad;
	
		// convert coordinates to radians
		lat1 = Math.toRadians(c_lat);
		long1 = Math.toRadians(c_lon);
	
		//Earth measures 
		//Year Name	a (meters) b (meters) 1/f Where Used 
		//1980 International 6,378,137 6,356,752 298.257 Worldwide 
		d_rad = radius_form/6378137;
		
		StringBuffer sb = new StringBuffer();
		// loop through the array and write path linestrings
		for(int i=start_angle; i<=end_angle; i=i+skip) {
			//delta_pts = 360/(double)num_points;
			//radial = Math.toRadians((double)i*delta_pts);
			radial = Math.toRadians((double)i);
			
			//This algorithm is limited to distances such that dlon < pi/2
			lat_rad = Math.asin(Math.sin(lat1)* Math.cos(d_rad) + Math.cos(lat1)* Math.sin(d_rad)* Math.cos(radial));
			dlon_rad = Math.atan2(Math.sin(radial)* Math.sin(d_rad)* Math.cos(lat1), Math.cos(d_rad)- Math.sin(lat1)* Math.sin(lat_rad));
			lon_rad = ((long1 + dlon_rad + Math.PI) % (2*Math.PI)) - Math.PI;
			
			//write results
			sb.append( Math.toDegrees(lon_rad) + ","+Math.toDegrees(lat_rad) + ",0 ");
		}
		
		radial = Math.toRadians((double)start_angle);
		//This algorithm is limited to distances such that dlon < pi/2
		lat_rad = Math.asin(Math.sin(lat1)* Math.cos(d_rad) + Math.cos(lat1)* Math.sin(d_rad)* Math.cos(radial));
		dlon_rad = Math.atan2(Math.sin(radial)* Math.sin(d_rad)* Math.cos(lat1), Math.cos(d_rad)- Math.sin(lat1)* Math.sin(lat_rad));
		lon_rad = ((long1 + dlon_rad + Math.PI) % (2*Math.PI)) - Math.PI;
		
		//write results
		sb.append( Math.toDegrees(lon_rad) + ","+Math.toDegrees(lat_rad) + ",0 ");
		
		//sb.append(c_lon+","+c_lat+ ",0");
		// output footer
		return sb.toString();
	}
}
