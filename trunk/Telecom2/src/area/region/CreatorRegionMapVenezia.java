package area.region;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import utils.Colors;
import utils.CopyAndSerializationUtils;
import utils.FileUtils;
import utils.Logger;
import visual.kml.KML;
import analysis.tourist.Voronoi;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.triangulate.VoronoiDiagramBuilder;



public class CreatorRegionMapVenezia {
	
	
	public static void main(String[] args) throws Exception {
		
		RegionMap base = new RegionMap("VeneziaBase");
		
		BufferedReader br = new BufferedReader(new FileReader(new File("C:/DATASET/GEO/venezia.txt")));
		String line;
		while((line = br.readLine()) != null) {
			String coordinates = br.readLine();	
			base.add(new Region(line,coordinates));
		}
		br.close();
		
		
		List<String> names = new ArrayList<String>();
		List<double[]> coordinates = new ArrayList<double[]>();
		
		br = new BufferedReader(new FileReader(new File("C:/DATASET/GEO/venezia2.txt")));
		while((line = br.readLine()) != null) {
			names.add(line);
			String[] e = br.readLine().split(",");	
			coordinates.add(new double[]{Double.parseDouble(e[0]),Double.parseDouble(e[1])});
		}
		br.close();
		
	
		
		VoronoiDiagramBuilder v = new VoronoiDiagramBuilder();
		
		GeometryFactory fact = new GeometryFactory();
		
		Coordinate[] coord = new Coordinate[coordinates.size()];
		for(int i=0; i<coord.length;i++) {
			coord[i] = new Coordinate(coordinates.get(i)[0],coordinates.get(i)[1]);
		}
		
		
		
		MultiPoint mpt = fact.createMultiPoint(coord);
		v.setSites(mpt);
	
		
		Geometry voronoi = v.getDiagram(fact);
		
		RegionMap rm = new RegionMap("Venezia");
		
		for(int i=0; i<voronoi.getNumGeometries();i++) {
			Geometry x = voronoi.getGeometryN(i);
			String name = "";
			// get the name
			for(int k=0; k<mpt.getNumPoints();k++)
				if(x.contains(mpt.getGeometryN(k))) {
						name = names.get(k);
						break;
				}
			//out.println(Voronoi.geom2Kml(name,(Polygon)x,Colors.RANDOM_COLORS[i % Colors.RANDOM_COLORS.length]));
			
			for(Region r : base.getRegions()) {
				if(x.intersects(r.getGeom())) {
					Geometry y  = x.intersection(r.getGeom());
					rm.add(new Region(r.getName()+"_"+name,y));
				}
			}
		}
		
		

		File f = FileUtils.getFile("RegionMap");
		CopyAndSerializationUtils.save(new File(f.getAbsolutePath()+"/Venezia.ser"), rm);
		Logger.logln("Done!");
	}
}
