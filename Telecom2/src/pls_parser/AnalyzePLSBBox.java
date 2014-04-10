package pls_parser;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import network.NetworkCell;
import network.NetworkMap;
import network.NetworkMapFactory;
import utils.Colors;
import utils.Config;
import utils.FileUtils;
import utils.Logger;
import visual.kml.KML;
import area.region.Region;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateList;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

public class AnalyzePLSBBox extends BufferAnalyzer {
	
	SimpleDateFormat f = new SimpleDateFormat("yyyy/MMM/dd",Locale.US);
	String name;
	NetworkMap nm = NetworkMapFactory.getNetworkMap();
	CoordinateList coord = new CoordinateList();
	
	public AnalyzePLSBBox() {
		super();
		Config.getInstance().pls_folder="G:/DATASET/PLS/file_pls/file_pls_fi";
		name =  "FirenzeProv";
		
		AnalyzePLSCoverage apc = new AnalyzePLSCoverage();
		Map<String,String> coverage = apc.compute();
		String first = coverage.keySet().iterator().next();
		first = first.substring(first.indexOf("-")+1);
		
		try {
			Config.getInstance().pls_start_time.setTime(f.parse(first));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public Calendar getEndTime() {
		Calendar end = getStartTime();
		end.add(Calendar.DAY_OF_YEAR, 10);
		return end;
	}
	

	public void analyze(String line) {
		String celllac = line.split("\t")[2];
		NetworkCell c = nm.get(Long.parseLong(celllac));
		if(c!=null) {
			Coordinate p = new Coordinate(c.getBarycentreLongitude(),c.getBarycentreLatitude());
			boolean far = true;
			for(int i=0; i<coord.size();i++) {
				if(coord.getCoordinate(i).distance(p) < 0.02) {
					far = false;
					break;
				}
			}
			if(far) coord.add(p,false);
		}
	}
	
	public void finish() {
		try{
			//Geometry g = new ConvexHull(coord.toCoordinateArray(),new GeometryFactory()).getConvexHull();
			System.out.println(coord.size());
			Geometry g = new GeometryFactory().createMultiPoint(coord.toCoordinateArray()).buffer(0.1);
			Region r = new Region(name,g);
			File dir = FileUtils.createDir("BASE/RegionMap");
			Logger.logln(dir.getAbsolutePath());
			PrintWriter out = new PrintWriter(new FileWriter(dir+"/"+name+".kml"));
			KML kml = new KML();
			kml.printHeaderDocument(out, name);
			out.println(r.toKml(Colors.RANDOM_COLORS[1]));
			kml.printFooterDocument(out);
			out.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	
	public static void main(String[] args) throws Exception {
		AnalyzePLSBBox ba = new AnalyzePLSBBox();
		ba.run();
		Logger.logln("Done!");
	}	
	
}
