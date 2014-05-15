package region.network;

import org.gps.utils.LatLonPoint;
import org.gps.utils.LatLonUtils;

import region.RegionI;
import utils.GeomUtils;
import visual.kml.KMLCircle;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vividsolutions.jts.geom.Geometry;

public class NetworkCell extends RegionI {
	
	private String celllac;			// Nome Cella (id)
	private String description;		// Nome Cella
	//private int barycentre;			// Distanza in metri dal sito della cella
	private long lac;				// LAC: Location Area Code
	private long cell_id;			// Identifica la cella
	//private String param5;			// Sconosciuto
	//private String param6;			// Sconosciuto
	//private String param7;			// Sconosciuto
	//private String param8;			// Sconosciuto
	//private String param9;			// Sconosciuto
	//private double barycentre_lat;	// Latitudine del baricentro nel sistema WGS84
	//private double barycentre_lon;	// Longitudine del baricentro nel sistema WGS84
	private double radius;			// Raggio in metri della cella, supponendola come un cerchio centrato nel baricentro
	
	
	public NetworkCell(String celllac, String description, long lac, long cell_id, double barycentre_lat, double barycentre_lon, double radius){
		this.name = celllac;
		this.centerLatLon = new double[]{barycentre_lat,barycentre_lon};
		this.description = description;
		//this.barycentre = barycentre;
		this.lac = lac;
		this.cell_id = cell_id;
		//this.barycentre_lat = barycentre_lat;
		//this.barycentre_lon = barycentre_lon;
		this.radius = radius;
		this.celllac = celllac;
	}
	
	public Geometry getGeom() {
		return GeomUtils.getCircle(centerLatLon[1], centerLatLon[0], radius);
	}
	
	// the following two methods serialize the object into a bson (binary json) to be saved in mongoDB
	
	public static NetworkCell bson2NetworkCell (BasicDBObject bson) {
		String celllac = bson.getString("name");
		String description = bson.getString("description");
		long lac = bson.getLong("lac");
		long cell_id = bson.getLong("cell_id");
		BasicDBList lonlat = (BasicDBList)((BasicDBObject)bson.get("loc")).get("coordinates");
		double barycentre_lon = (Double)lonlat.get(0);
		double barycentre_lat = (Double)lonlat.get(1);
		double radius = bson.getDouble("radius");
		return new NetworkCell(celllac,description,lac,cell_id,barycentre_lat,barycentre_lon,radius);
	}
	
	
	public DBObject getBSON() {
		BasicDBObject nc = new BasicDBObject();
		nc.append("name", celllac);
		nc.append("description", description);
		nc.append("lac", lac);
		nc.append("cell_id", cell_id);
		nc.append("loc", new BasicDBObject().append("type", "Point").append("coordinates", new double[]{centerLatLon[1],centerLatLon[0]}));
		nc.append("radius", radius);
		return nc;
	}
	
	public long getCellID(){
		return cell_id;
	}
	
	public String getName() {
		return getCellac();
	}
	
	public String getCellac() {
		return celllac;
	}

	public String getDescription(){
		return description;
	}
	
	public long getLac(){
		return lac;
	}
	
	
	public double[][] getBboxLonLat() {
		LatLonPoint c = this.getCenterPoint();
		LatLonPoint ll = LatLonUtils.getPointAtDistance(c, 225, radius*Math.sqrt(2));
		LatLonPoint tr = LatLonUtils.getPointAtDistance(c, 45, radius*Math.sqrt(2));
		return new double[][]{{ll.getLongitude(),ll.getLatitude()},{tr.getLongitude(),tr.getLatitude()}};
	}
	
	public double getRadius(){
		return radius;
	}
	
	public String toString(){
		return celllac+","+description+","+lac+","+cell_id+","+getLatLon()[0]+","+getLatLon()[1]+","+radius;
	}
}
