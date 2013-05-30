package area;

import java.io.Serializable;

public class Region implements Serializable {
	private String name;
	private String kmlcoordinates;
	private double[][] bbox;
	private double[] center;
	
	public Region(String name, String kmlcoordinates) {
		this.name = name;
		this.kmlcoordinates = kmlcoordinates;
		computeBBoxAndCenter();
	}
	
	private void computeBBoxAndCenter() {
		double minlon = 0, maxlon = 0, minlat = 0, maxlat = 0;
		bbox = new double[][]{{minlon,minlat},{maxlon,maxlat}};
		center = new double[]{(minlon+maxlon)/2,(minlat+maxlat)/2};
	}
	
	
	public String kmlToOpenGISCoordinates() {
		String openGIScoordinates = kmlcoordinates.replaceAll(",", ";");
		openGIScoordinates = openGIScoordinates.replaceAll(";0 ", ",");
		openGIScoordinates = openGIScoordinates.replaceAll(";", " ");
		openGIScoordinates = openGIScoordinates.substring(0, openGIScoordinates.length()-1);
		return openGIScoordinates;
	}

	
	public String toKml(String color) {
		return "<Style id=\""+color+"\">" +
				"<LineStyle>" +
				"<color>"+color+"</color>" +
				"</LineStyle>" +
				"<PolyStyle>" +
				"<color>"+color+"</color>" +
				"</PolyStyle>" +
				"</Style>" +
				"<Placemark>" +
				"<name>"+name+"</name>" +
				"<styleUrl>#"+color+"</styleUrl>" +
				"<Polygon>" +
				"<outerBoundaryIs>" +
				"<LinearRing>" +
				"<coordinates>"+kmlcoordinates+"</coordinates>" +
				"</LinearRing>" +
				"</outerBoundaryIs>" +
				"</Polygon>" +
				"</Placemark>";
	}
}
