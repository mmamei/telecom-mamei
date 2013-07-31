package network;

import java.io.Serializable;

import org.gps.utils.LatLonPoint;

import visual.kml.KMLCircle;

public class NetworkCell implements Serializable {

	private String cellName;		// Nome Cella
	private int barycentre;			// Distanza in metri dal sito della cella
	private long lac;				// LAC: Location Area Code
	private long cell_id;			// Identifica la cella
	private String param5;			// Sconosciuto
	private String param6;			// Sconosciuto
	private String param7;			// Sconosciuto
	private String param8;			// Sconosciuto
	private String param9;			// Sconosciuto
	private double barycentre_lat;	// Latitudine del baricentro nel sistema WGS84
	private double barycentre_lon;	// Longitudine del baricentro nel sistema WGS84
	private double radius;			// Raggio in metri della cella, supponendola come un cerchio centrato nel baricentro
	
	public NetworkCell(String line, String separator){
		this(line.split(separator));
	}
	
	public NetworkCell(String [] splitted){
		cellName = splitted[0];
		barycentre = Integer.parseInt(splitted[1]);
		lac = Long.parseLong(splitted[2]);
		cell_id = Long.parseLong(splitted[3]);
		param5 = splitted[4];
		param6 = splitted[5];
		param7 = splitted[6];
		param8 = splitted[7];
		param9 = splitted[8];
		barycentre_lat = Double.parseDouble(splitted[9]);
		barycentre_lon = Double.parseDouble(splitted[10]);
		radius = Float.parseFloat(splitted[11]);
	}
	

	public double getBarycentreLatitude(){
		return barycentre_lat;
	}

	public double getBarycentreLongitude(){
		return barycentre_lon;
	}
	
	public long getCellac(){
		return (lac*65536+cell_id);
	}
	
	public long getCellID(){
		return cell_id;
	}

	public String getCellName(){
		return cellName;
	}
	
	public long getLac(){
		return lac;
	}
	
	public LatLonPoint getPoint(){
		return new LatLonPoint(barycentre_lat, barycentre_lon);
	}
	
	public double getRadius(){
		return radius;
	}

	
	
	public String toKml(){
		return toKml("770077",cellName,"Cellac = "+getCellac());
	}
	
	public String toKml(String color, String name, String desc) {
		String incolor = "#7f"+color;
		String bcolor = "#ff"+color;
		return new KMLCircle().draw(barycentre_lon, barycentre_lat, radius, 10, 0, 360, name, incolor, bcolor, desc);
	}

	
	public String toString(){
		return cellName+","+barycentre+","+lac+","+cell_id+","+param5+","+param6+","+param7+","+param8+","+param9+
				","+barycentre_lat+","+barycentre_lon+","+radius;
	}
}
