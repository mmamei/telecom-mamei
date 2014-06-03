package zzz_misc_code;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import utils.Colors;
import utils.Config;
import utils.Logger;
import visual.kml.KML;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.triangulate.VoronoiDiagramBuilder;


public class Voronoi {
	
	public static void main(String[] args) throws Exception {
	
		VoronoiDiagramBuilder v = new VoronoiDiagramBuilder();
		GeometryFactory fact = new GeometryFactory();
		
		
		MultiPoint mpt = fact.createMultiPoint(new Coordinate[] { 
				new Coordinate(11.2590,43.7713), 
				new Coordinate(11.2574,43.7719),
				new Coordinate(11.2589,43.7736) 
		});
		v.setSites(mpt);
		
		
		
		
		
		Geometry voronoi = v.getDiagram(fact);
		File dir = new File(Config.getInstance().base_folder+"/Voronoi");
		dir.mkdirs();
		PrintWriter out = new PrintWriter(new FileWriter(dir+"/test.kml"));
		KML kml = new KML();
		kml.printHeaderDocument(out, "test");
		for(int i=0; i<voronoi.getNumGeometries();i++) {
			String area = geom2Kml(""+i,(Polygon)voronoi.getGeometryN(i),Colors.RANDOM_COLORS[i%Colors.RANDOM_COLORS.length]);
			out.println(area);
		}
		kml.printFooterDocument(out);
		out.close();
		Logger.logln("Done!");
	}
	
	
	
	public static String geom2Kml(String name, Geometry p, String color) {
		StringBuffer sb = new StringBuffer();
		sb.append("<Style id=\""+color+"\">");
		sb.append("<LineStyle>");
		sb.append("<color>"+color+"</color>");
		sb.append("</LineStyle>");
		sb.append("<PolyStyle>");
		sb.append("<color>"+color+"</color>");
		sb.append("</PolyStyle>");
		sb.append("</Style>");
		
		sb.append("<Placemark>");
		sb.append("<name>"+name+"</name>");
		sb.append("<description>"+name+"</description>");
		sb.append("<styleUrl>#"+color+"</styleUrl>");
		sb.append("<Polygon>");
		sb.append("<tessellate>1</tessellate>");
		sb.append("<outerBoundaryIs>");
		sb.append("<LinearRing>");
		sb.append("<coordinates>\n");
		Coordinate[] cs = p.getCoordinates();
		for(Coordinate c: cs) 
			sb.append(c.x+","+c.y+",0 ");
		sb.append("\n</coordinates>");
		sb.append("</LinearRing>");
		sb.append("</outerBoundaryIs>");
		sb.append("</Polygon>");
		sb.append("</Placemark>");
		return sb.toString();
	
	}
	
}
