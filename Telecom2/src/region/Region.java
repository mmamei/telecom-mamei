package region;

import java.io.Serializable;

import org.gps.utils.LatLonPoint;

import utils.GeomUtils;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

public class Region extends RegionI {
	private double[][] bbox;
	private Geometry g;
	
	
	public Region(String name, String kmlcoordinates) {
		this(name, GeomUtils.openGis2Geom(GeomUtils.kml2OpenGis(kmlcoordinates)));
	}
	
	public Region(String name, double[][] borderLonLat) {
		this(name, GeomUtils.openGis2Geom(GeomUtils.kml2OpenGis(GeomUtils.lonLat2Kml(borderLonLat))));
	}
	
	public Region(String name, Geometry g) {
		this.g = g;
		this.name = name.replaceAll("\\\\", "");
		
		double minlon = Double.MAX_VALUE, maxlon = -Double.MAX_VALUE, minlat = Double.MAX_VALUE, maxlat = -Double.MAX_VALUE;
		String kmlcoordinates = GeomUtils.geom2Kml(g);
		String x = GeomUtils.kml2OpenGis(kmlcoordinates);
		if(x.equals("")) return;
		String[] coord = x.split(",");
		for(String c: coord) {
			double lat = Double.parseDouble(c.substring(0,c.indexOf(" ")));
			double lon = Double.parseDouble(c.substring(c.indexOf(" ")+1));
			
			minlon = Math.min(minlon, lon);
			minlat = Math.min(minlat, lat);
			maxlon = Math.max(maxlon, lon);
			maxlat = Math.max(maxlat, lat);
		}
		
		// questo codice si pu� sostituire con get envelope! test
		
		bbox = new double[][]{{minlon,minlat},{maxlon,maxlat}};
		centerLatLon = new double[]{(minlon+maxlon)/2,(minlat+maxlat)/2};
	}
	
	
	public Geometry getGeom() {
		return g;
	}
}
