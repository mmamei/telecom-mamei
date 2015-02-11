package visual.kml;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.gps.utils.LatLonPoint;
import org.gps.utils.LatLonUtils;


// ****************************************************************************************
// Tesi di Luca Bonfatti, 2013
// ****************************************************************************************

public class KMLArrowCurved {
	
	
	public static final double HI = 6;
	public static final double MED = 3;
	public static final double LOW = 1;
	
	
	public static String printArrow (double lon1, double lat1, double lon2, double lat2, int size, String color, boolean directed) {
		return printArrow(lon1,lat1,lon2,lat2,size,color,HI,directed);
	}
	
	public static String printArrow (double lon1, double lat1, double lon2, double lat2, int size, String color, double curvature, boolean directed) {
		double concavity = lon1 > lon2 ? 1 : -1;
		double d = Math.sqrt(((lat1-lat2)*(lat1-lat2))+((lon1-lon2)*(lon1-lon2)));
		double a = curvature * concavity;
		double b=-a*d;
		double teta=Math.toDegrees(Math.atan((lat2-lat1)/(lon2-lon1)));
		
		StringBuffer sb = new StringBuffer();
	
		sb.append("<Style id=\""+color+size+"\">\n");
		sb.append("<LineStyle>\n");
		sb.append("<color>"+color+"</color>\n");
		sb.append("<width>"+size+"</width>\n");
		sb.append("</LineStyle>\n");
		sb.append("<PolyStyle>\n");
		sb.append("<color>"+color+"</color>\n");
		sb.append("<width>"+size+"</width>\n");
		sb.append("</PolyStyle>\n");
		sb.append("</Style>\n");
		
		sb.append("<Placemark>\n");
		sb.append("<name></name>\n");
		sb.append("<styleUrl>#"+color+size+"</styleUrl>\n");
		sb.append("<LineString>\n");
		sb.append("<extrude>1</extrude>\n");
		sb.append("<tessellate>1</tessellate>\n");
		sb.append("<coordinates>\n");
		double x = 0;
		double y = 0;
		double l1,l2,l3,l4;
		if(lon1<=lon2) {
			l1=lon1;
			l2=lat1;
			l3=lon2;
			l4=lat2;
		}
		else {
			l1=lon2;
			l2=lat2;
			l3=lon1;
			l4=lat1;
		}
		
		sb.append(l1+","+l2+",0 ");
		for(double i=d/10.0;i<d;i=i+(d/10.0)){
			x = l1+i*Math.cos(Math.toRadians(teta)) - (a*i*i+b*i)*Math.sin(Math.toRadians(teta));
		    y = l2+(a*i*i+b*i)*Math.cos(Math.toRadians(teta))+i*Math.sin(Math.toRadians(teta));
		    sb.append(x+","+y+",0 ");
		}
		sb.append(l3+","+l4+",0\n");
		
		sb.append("</coordinates>\n");
		sb.append("</LineString>\n");
		sb.append("</Placemark>\n");
		
		// print arrow tip
		if(directed) {
			LatLonPoint p1 = new LatLonPoint(lat1,lon1);
			LatLonPoint p2 = new LatLonPoint(lat2,lon2);
			double slope = LatLonUtils.getSlope(p2, p1);
			double dist = 100; //LatLonUtils.getHaversineDistance(p1, p2) / 8;
			LatLonPoint tip1 = LatLonUtils.getPointAtDistance(p2, slope+10, dist);
			LatLonPoint tip2 = LatLonUtils.getPointAtDistance(p2, slope-10, dist);
			
			sb.append("<Placemark>\n");
			sb.append("<styleUrl>#"+color+size+"</styleUrl>\n");
			sb.append("<Polygon>\n");
			sb.append("<outerBoundaryIs>\n");
			sb.append("<LinearRing>\n");
			sb.append("<coordinates>\n");
			sb.append(tip1.getLongitude()+","+tip1.getLatitude()+",0 "+lon2+","+lat2+",0 "+tip2.getLongitude()+","+tip2.getLatitude()+",0 "+tip1.getLongitude()+","+tip1.getLatitude()+",0\n");
			sb.append("</coordinates>\n");
			sb.append("</LinearRing>\n");
			sb.append("</outerBoundaryIs>\n");
			sb.append("</Polygon>\n");
			sb.append("</Placemark>\n");
		}
		
		return sb.toString();
	}
	
	
	public static void main(String[] args) throws Exception {
		PrintWriter out = new PrintWriter(new FileWriter(new File("arrowc.kml")));
		KML kml = new KML();
		kml.printHeaderDocument(out, "ArrowC");
		
		
		
		double lon1 = 10.92118234337734;
		double lat1 = 44.75196646802829;
		double lon2 = 11.13493327581623;
		double lat2 = 44.86936892789175;
		
		out.println(KMLArrowCurved.printArrow(lon2,lat2,lon1,lat1, 2,"ff00ff00", true));
		out.println(KMLArrowCurved.printArrow(lon1,lat1,lon2,lat2, 2,"ff00ff00", true));
		kml.printFooterDocument(out);
		out.close();
		System.out.println("Done");
	}
	
}
