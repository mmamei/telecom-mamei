package visual.kml;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class KMLCircle {
	
public List<String> STYLES;
	
	private static final DecimalFormat D_FORMATTER_COARSE = new DecimalFormat("0.00");
	
	public static void main(String[] args) throws Exception {
		KMLCircle kml_circle = new KMLCircle();
		File fileOutput = new File("CircleTest.kml"); 
		PrintWriter out = new PrintWriter(new FileWriter(fileOutput));
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("<kml xmlns=\"http://earth.google.com/kml/2.2\">");
		out.println("<Document>");
		out.println(kml_circle.draw(10.90909805036947,44.63374880581724, 330, 10, 0, 360, "ciao", "7700ffff","7700ffff","marco"));
		out.println("</Document>");
		out.println("</kml>");
		out.close();
		System.out.println("Done");
	}


	public KMLCircle() {
		STYLES = new ArrayList<String>();
	}
	
	
	public String draw(double c_lon, double c_lat, double radius_form, int skip, int start_angle, int end_angle, String name, String color, String bcolor, String description) {
		
		StringBuffer sb = new StringBuffer();
		sb.append(drawStyle(color,bcolor));
		
		sb.append("<Placemark>\n");
		sb.append("<name>"+name+"</name>\n");
		
		sb.append("<description><![CDATA[");
		sb.append(description);
		sb.append("]]></description>");
		
		sb.append("<styleUrl>#"+color+"</styleUrl>\n");
		
		sb.append(drawPlacemarkContent(c_lon, c_lat, radius_form, skip, start_angle, end_angle, name, color, description));
		
		sb.append("</Placemark>\n");
		return sb.toString();	
	}
	
	public String drawPlacemarkContent(double c_lon, double c_lat, double radius_form, int skip,int start_angle, int end_angle, String name, String color, String description) {
		StringBuffer sb = new StringBuffer();
		
		
		sb.append(createPolygon(c_lon, c_lat, radius_form, skip, start_angle, end_angle));
		
		
		return sb.toString();
	}
	
	public String drawStyle(String color, String bcolor) {
		StringBuffer sb = new StringBuffer();
		if(!STYLES.contains(color)) {
			STYLES.add(color);
			sb.append("<Style id=\""+color+"\">\n");
			sb.append("<LineStyle>\n");
			sb.append("<color>"+bcolor+"</color>\n");
			sb.append("</LineStyle>\n");
			sb.append("<PolyStyle>");
			sb.append("<color>"+color+"</color>\n");
			sb.append("</PolyStyle>\n");
			sb.append(pluginStyle());
			sb.append("</Style>\n");
		}
		return sb.toString();
	}

	public String pluginStyle() {
		return "";
	}
	
	public String createPolygon(double c_lon, double c_lat, double radius_form, int skip, int start_angle, int end_angle) {
		
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
		sb.append("<Polygon>\n");
		sb.append("<tessellate>1</tessellate>\n");
		sb.append("<outerBoundaryIs>\n");
		sb.append("<LinearRing>\n");
		sb.append("<coordinates>\n");
		
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
			sb.append( Math.toDegrees(lon_rad) + ","+Math.toDegrees(lat_rad) + ",0 \n");
		}
		sb.append(c_lon+","+c_lat+ ",0 \n");
		// output footer
		sb.append("</coordinates>\n");
		sb.append("</LinearRing>\n");
		sb.append("</outerBoundaryIs>\n");
		sb.append("</Polygon>\n");
		return sb.toString();
	}

}
