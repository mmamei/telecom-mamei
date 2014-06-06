package region;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;
import visual.kml.KML;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.triangulate.VoronoiDiagramBuilder;



public class CreatorRegionMapVoronoi {
	
	
	public static void main(String[] args) throws Exception {
		String city = "Firenze";
		process(city);
		RegionMap.process(city);
		Logger.logln("Done!");
	}
	
	
	public static void process(String city) throws Exception {
		
		RegionMap base = new RegionMap(city+"Base");
		
		BufferedReader br = new BufferedReader(new FileReader(new File("C:/DATASET/GEO/"+city+"/areas.txt")));
		String line;
		while((line = br.readLine()) != null) {
			String coordinates = br.readLine();	
			base.add(new Region(line,coordinates));
		}
		br.close();
		
		
		List<String> names = new ArrayList<String>();
		List<double[]> coordinates = new ArrayList<double[]>();
		
		br = new BufferedReader(new FileReader(new File("C:/DATASET/GEO/"+city+"/placemarks.txt")));
		while((line = br.readLine()) != null) {
			names.add(line);
			String[] e = br.readLine().split(",");	
			coordinates.add(new double[]{Double.parseDouble(e[0]),Double.parseDouble(e[1])});
		}
		br.close();
		
	
		
		// print the kml placemarks in kml
		PrintWriter out = new PrintWriter(new FileWriter(new File(Config.getInstance().base_folder+"/RegionMap")+"/"+city+"_places.kml"));
		KML kml = new KML();
		kml.printHeaderDocument(out, city+"_places");
		for(int i=0; i<names.size();i++) {
			out.println("<Placemark>" +
				    "<name>"+names.get(i)+"</name>" +
				    "<description></description>" +
				    "<Point>" +	
				    "<coordinates>"+coordinates.get(i)[0]+","+coordinates.get(i)[1]+",0</coordinates>" +
				    "</Point>" +
				    "</Placemark>");
		}
		kml.printFooterDocument(out);
		out.close();
		
		
		VoronoiDiagramBuilder v = new VoronoiDiagramBuilder();
		
		v.setClipEnvelope(base.getEnvelope());
		GeometryFactory fact = new GeometryFactory();
		
		Coordinate[] coord = new Coordinate[coordinates.size()];
		for(int i=0; i<coord.length;i++) {
			coord[i] = new Coordinate(coordinates.get(i)[0],coordinates.get(i)[1]);
		}
		
		
		
		MultiPoint mpt = fact.createMultiPoint(coord);
		v.setSites(mpt);
	
		
		Geometry voronoi = v.getDiagram(fact);
		
		RegionMap rm = new RegionMap(city);
		
		for(int i=0; i<voronoi.getNumGeometries();i++) {
			Geometry x = voronoi.getGeometryN(i);
			
			Geometry point = null;
			String name = "";
			// get the name
			for(int k=0; k<mpt.getNumPoints();k++)
				if(x.contains(mpt.getGeometryN(k))) {
						point = mpt.getGeometryN(k);
						name = names.get(k);
						break;
				}
			//out.println(Voronoi.geom2Kml(name,(Polygon)x,Colors.RANDOM_COLORS[i % Colors.RANDOM_COLORS.length]));
			//rm.add(new Region(name,x));
			
			for(RegionI r : base.getRegions()) {
				if(x.intersects(r.getGeom())) {
				//if(r.getGeom().contains(point)) {
					Geometry y  = x.intersection(r.getGeom());
					rm.add(new Region(r.getName()+"_"+name,y));
				}
			}
		}
		
		for(RegionI b: base.getRegions()) {
			boolean found = false;
			for(int k=0; k<mpt.getNumPoints();k++) {
				Geometry point = mpt.getGeometryN(k);
				if(b.getGeom().contains(point)) {found = true; break;}
			}
			if(!found) rm.add(b);
		}
		

		File f = new File(Config.getInstance().base_folder+"/RegionMap");
		CopyAndSerializationUtils.save(new File(f.getAbsolutePath()+"/"+city+".ser"), rm);
	}
}
