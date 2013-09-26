package visual.kml;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.gps.utils.LatLonPoint;
import org.gps.utils.LatLonUtils;

public class KMLArrow {
	
	// line arrow
	
	public static String printArrow(double lon1, double lat1, double lon2, double lat2, double size, String color, boolean directed) {
		
	
		LatLonPoint p1 = new LatLonPoint(lat1,lon1);
		LatLonPoint p2 = new LatLonPoint(lat2,lon2);
		double slope = LatLonUtils.getSlope(p2, p1);
		double dist = directed? 100 : 0; //LatLonUtils.getHaversineDistance(p1, p2) / 8;
		LatLonPoint tip1 = LatLonUtils.getPointAtDistance(p2, slope+10, dist);
		LatLonPoint tip2 = LatLonUtils.getPointAtDistance(p2, slope-10, dist);
		LatLonPoint p2shorter = LatLonUtils.getPointAtDistance(p2, slope, dist/2);
		
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("<Style id=\""+color+"\">");
		sb.append("<LineStyle>");
		sb.append("<width>"+size+"</width>");
		sb.append("<color>"+color+"</color>");
		sb.append("</LineStyle>");
		sb.append("<PolyStyle>");
		sb.append("<color>"+color+"</color>");
		sb.append("</PolyStyle>");
		sb.append("</Style>");
		
		
		sb.append("<Placemark>");
		sb.append("<styleUrl>#"+color+"</styleUrl>");
		sb.append("<LineString>");
		sb.append("<extrude>1</extrude>");
		sb.append("<coordinates>");
		sb.append(lon1+","+lat1+",0 "+p2shorter.getLongitude()+","+p2shorter.getLatitude()+",0");
		sb.append("</coordinates>");
		sb.append("</LineString>");		
		sb.append("</Placemark>");
		
		
		sb.append("<Placemark>");
		sb.append("<styleUrl>#"+color+"</styleUrl>");
		sb.append("<Polygon>");
		sb.append("<outerBoundaryIs>");
		sb.append("<LinearRing>");
		sb.append("<coordinates>");
		sb.append(tip1.getLongitude()+","+tip1.getLatitude()+",0 "+lon2+","+lat2+",0 "+tip2.getLongitude()+","+tip2.getLatitude()+",0 "+tip1.getLongitude()+","+tip1.getLatitude()+",0");
		sb.append("</coordinates>");
		sb.append("</LinearRing>");
		sb.append("</outerBoundaryIs>");
		sb.append("</Polygon>");
		sb.append("</Placemark>");
		
	
		
		return sb.toString();
	}
	
	// multi point arrow
	
	public static String printArrow(double[][] p, double size, String color, boolean directed) {
		StringBuffer sb = new StringBuffer();
		
		sb.append("<Style id=\""+color+"\">");
		sb.append("<LineStyle>");
		sb.append("<width>"+size+"</width>");
		sb.append("<color>"+color+"</color>");
		sb.append("</LineStyle>");
		sb.append("<PolyStyle>");
		sb.append("<color>"+color+"</color>");
		sb.append("</PolyStyle>");
		sb.append("</Style>");
		
		
		sb.append("<Placemark>");
		sb.append("<styleUrl>#"+color+"</styleUrl>");
		sb.append("<LineString>");
		sb.append("<extrude>1</extrude>");
		sb.append("<coordinates>");
		
		for(int i=0; i<p.length;i++)
			sb.append(p[i][1]+","+p[i][0]+",0 ");
		
		sb.append("</coordinates>");
		sb.append("</LineString>");		
		sb.append("</Placemark>");
		
		
		if(directed) {
			
			double lat1 = p[p.length-2][0];
			double lon1 = p[p.length-2][1];
			
			double lat2 = p[p.length-1][0];
			double lon2 = p[p.length-1][1];
			
			LatLonPoint p1 = new LatLonPoint(lat1,lon1);
			LatLonPoint p2 = new LatLonPoint(lat2,lon2);
			double slope = LatLonUtils.getSlope(p2, p1);
			double dist = 100;//LatLonUtils.getHaversineDistance(p1, p2) / 8;
			LatLonPoint tip1 = LatLonUtils.getPointAtDistance(p2, slope+10, dist);
			LatLonPoint tip2 = LatLonUtils.getPointAtDistance(p2, slope-10, dist);
			
			sb.append("<Placemark>");
			sb.append("<styleUrl>#"+color+"</styleUrl>");
			sb.append("<Polygon>");
			sb.append("<outerBoundaryIs>");
			sb.append("<LinearRing>");
			sb.append("<coordinates>");
			sb.append(tip1.getLongitude()+","+tip1.getLatitude()+",0 "+lon2+","+lat2+",0 "+tip2.getLongitude()+","+tip2.getLatitude()+",0 "+tip1.getLongitude()+","+tip1.getLatitude()+",0");
			sb.append("</coordinates>");
			sb.append("</LinearRing>");
			sb.append("</outerBoundaryIs>");
			sb.append("</Polygon>");
			sb.append("</Placemark>");
		}
		
		return sb.toString();
	}	
	
	
}
