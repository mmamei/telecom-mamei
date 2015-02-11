package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import visual.kml.KML;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class GeomUtils {
	
	
	public static void main(String[] args) throws Exception {
		double radius = 100;
		Polygon c1 = getCircle(10.39,45.335,radius);
		Polygon c2 = getCircle(10.39,45.335,50);
		Geometry c = c1.intersection(c1.difference(c2));
		System.out.println(geoArea(c1));
		System.out.println(geoArea(c2));
		System.out.println(geoArea(c1) - geoArea(c2));
		System.out.println(geoArea(c));
		PrintWriter out = new PrintWriter(new FileWriter(new File("test_geom.kml")));
		KML kml = new KML();
		kml.printHeaderDocument(out, "test_geom");
		out.println(jtsGeometry2KML(c,"990000ff"));
		kml.printFooterDocument(out);
		out.close();	
		
	}
	
	public static void main2(String[] args) throws Exception {
		double radius = 500;
		Polygon circle = getCircle(10.39,45.335,radius);
		
		String x1 = "10.41,45.32,0 "
				  + "10.41,45.35,0 "
				  + "10.37,45.35,0 "
				  + "10.37,45.32,0 "
				  + "10.41,45.32,0";
		
		String x2 = "10.41,45.32,0 "
				  + "10.41,45.35,0 "
				  + "10.45,45.35,0 "
				  + "10.45,45.32,0 "
				  + "10.41,45.32,0";
		
		Polygon[] a = new Polygon[]{(Polygon)new WKTReader().read("POLYGON (("+kml2OpenGis(x1)+"))"),
								    (Polygon)new WKTReader().read("POLYGON (("+kml2OpenGis(x2)+"))")};
		
		float[] area_intersection = new float[a.length];
		double ca = Math.PI * Math.pow(radius,2);
		System.out.println("Circle Area = "+ca+" == "+geoArea(circle));
		for(int i=0; i<a.length;i++) {
			Geometry y = a[i].intersection(circle);
			area_intersection[i] = (float)(geoArea(y)/ca);
		}
		
		
		for(int i=0; i<area_intersection.length;i++) {
			System.out.println(area_intersection[i]);
		}
		
		System.out.println("Done");
		
		
		PrintWriter out = new PrintWriter(new FileWriter(new File("test_geom.kml")));
		KML kml = new KML();
		kml.printHeaderDocument(out, "test_geom");
		out.println(jtsGeometry2KML(circle,"990000ff"));
		out.println(jtsGeometry2KML(a[0],"9900ff00"));
		out.println(jtsGeometry2KML(a[1],"99ff0000"));
		kml.printFooterDocument(out);
		out.close();	
	}
	
	
	public static Polygon getCircle(double lon, double lat, double r) {
		String coord = kml2OpenGis(createPolygon(lon,lat,r,11,0,360));
		Polygon circle = null;
		try {
			circle = (Polygon) new WKTReader().read("POLYGON (("+coord+"))");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return circle;
	}
	
	
	public static String kml2OpenGis(String kml) {
		if(kml.equals("")) return "";
		String ogis = kml.replaceAll(",", ";");
		ogis = ogis.replaceAll(";0 ", ",");
		ogis = ogis.replaceAll(";", " ");
		ogis = ogis.substring(0, ogis.length()-1);
		return ogis;
	}
	
	
	public static String openGis2Kml(String opengis) {
		//System.out.println(opengis);
		//POLYGON ((10.41 45.32, 10.41 45.35, 10.37 45.35, 10.37 45.32, 10.41 45.32))
		String kml = opengis.substring(opengis.indexOf("((")+2,opengis.indexOf("))"));
		kml = kml.replaceAll("\\(", "");
		kml = kml.replaceAll("\\)", "");
		kml = kml.replaceAll(", ", ",0;");
		kml = kml.replaceAll(" ", ",");
		kml = kml.replaceAll(";", " ");
		kml = kml + ",0";
		return kml;
	}
	
	
	
	public static Geometry openGis2Geom(String opengis) {
		Geometry g = null;
		try {
			if(opengis.startsWith("POLYGON")) g = new WKTReader().read(opengis);
			else g = new WKTReader().read("POLYGON (("+opengis+"))");
		} catch(Exception e) {
			e.printStackTrace();
		}
		return g;
	}
	
	
	public static String geom2Kml(Geometry g) {
		Coordinate[] cs = g.getCoordinates();
		StringBuffer sb = new StringBuffer();
		for(Coordinate c: cs) 
			sb.append(c.x+","+c.y+",0 ");
		return sb.toString();
	}
	
	
	public static String lonLat2Kml(double[][] borderLonLat) {
		StringBuffer sb = new StringBuffer();
		for(double[] ll: borderLonLat) 
			sb.append(ll[0]+","+ll[1]+",0 ");
		return sb.toString();
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
	
	
	public static String jtsGeometry2KML(Geometry g, String color) {
		StringBuffer sb = new StringBuffer();
		
		sb.append("<Style id=\""+color+"\">");
		sb.append("<PolyStyle>");
		sb.append("<color>"+color+"</color>");
		sb.append("</PolyStyle>");
		sb.append("</Style>");
		
		sb.append("<Placemark>");
		sb.append("<styleUrl>#"+color+"</styleUrl>");
		sb.append("<Polygon>");
		sb.append("<tessellate>1</tessellate>");
		sb.append("<outerBoundaryIs>");
		sb.append("<LinearRing>");
		sb.append("<coordinates>");
		sb.append(openGis2Kml(g.toString()));
		sb.append("</coordinates>");
		sb.append("</LinearRing>");
		sb.append("</outerBoundaryIs>");
		sb.append("</Polygon>");
		sb.append("</Placemark>");
		return sb.toString();	
	}
	
	
	public static final double EARTH_RADIUS_METERS = 6371000;
	public static double geoArea(Geometry p) {
		Coordinate[] cs = p.getCoordinates();
		double[] lat = new double[cs.length];
		double[] lon = new double[cs.length];
		for(int i=0; i<cs.length;i++) {
			lon[i] = Math.toRadians(cs[i].x);
			lat[i] = Math.toRadians(cs[i].y);
		}
		return sphericalPolygonArea(lat,lon,EARTH_RADIUS_METERS);
	}

	/// <summary>
	/// Compute the Area of a Spherical Polygon
	/// </summary>
	/// <param name="lat">the latitudes of all vertices(in radian)</param>
	/// <param name="lon">the longitudes of all vertices(in radian)</param>
	/// <param name="r">spherical radius</param>
	/// <returns>Returns the area of a spherical polygon</returns>
	private static double sphericalPolygonArea( double[ ] lat , double[ ] lon , double r ) {
		double lam1 = 0, lam2 = 0, beta1 =0, beta2 = 0, cosB1 =0, cosB2 = 0;
	    double hav = 0;
	    double sum = 0;
	    
	    for( int j = 0 ; j < lat.length ; j++ ) {
	    	int k = j + 1;
	    	if( j == 0 ) {
	    		lam1 = lon[j];
	    		beta1 = lat[j];
	    		lam2 = lon[j + 1];
	    		beta2 = lat[j + 1];
	    		cosB1 = Math.cos( beta1 );
	    		cosB2 = Math.cos( beta2 );
	    	}
	    	else {
	    		k = ( j + 1 ) % lat.length;
	    		lam1 = lam2;
	    		beta1 = beta2;
	    		lam2 = lon[k];
	    		beta2 = lat[k];
	    		cosB1 = cosB2;
	    		cosB2 = Math.cos( beta2 );
	    	}
	    	if( lam1 != lam2 ) {
	    		hav = haversine( beta2 - beta1 ) + cosB1 * cosB2 * haversine( lam2 - lam1 );
	    		double a = 2 * Math.asin( Math.sqrt( hav ) );
	    		double b = Math.PI / 2 - beta2;
	    		double c = Math.PI / 2 - beta1;
	    		double s = 0.5 * ( a + b + c );
	    		double t = Math.tan( s / 2 ) * Math.tan( ( s - a ) / 2 ) *  
	            Math.tan( ( s - b ) / 2 ) * Math.tan( ( s - c ) / 2 );

	    		double excess = Math.abs( 4 * Math.atan( Math.sqrt( Math.abs( t ) ) ) );

	    		if( lam2 < lam1 ) 
	    			excess = -excess;

	    		sum += excess;
	    	}
	    }
	    return Math.abs( sum ) * r * r;
	}
	
	/// <summary>
	/// Haversine function : hav(x) = (1-cos(x))/2
	/// </summary>
	/// <param name="x"></param>
	/// <returns>Returns the value of Haversine function</returns>
	private static double haversine( double x ) {
		return ( 1.0 - Math.cos(x) ) / 2.0;
	}
}
