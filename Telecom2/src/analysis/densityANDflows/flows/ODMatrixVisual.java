package analysis.densityANDflows.flows;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import utils.Config;
import utils.Logger;
import visual.html.ArrowsGoogleMaps;
import visual.kml.KML;
import visual.kml.KMLArrow;
 
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.PointList;

public class ODMatrixVisual {
		

	public static final String TRASPORTATION_MODE = EncodingManager.CAR;
	
    
    // main for testing purposes
    public static void main(String[] args) throws Exception {
    	String ghLoc = "C:/DATASET/osm/piem";
	    String testOsm = "C:/DATASET/osm/piem/piem.pbf";
    	GraphHopper gh = new GraphHopper().setInMemory(true, true).setEncodingManager(new EncodingManager(TRASPORTATION_MODE)).setGraphHopperLocation(ghLoc).setOSMFile(testOsm);
		gh.setPreciseIndexResolution(10000); // to be set about the grid size
		gh.importOrLoad();
		
		double start_lat = 45.077157;
		double start_lon = 7.629951;
		
		double end_lat = 44.968199;
		double end_lon = 7.621368;
		
		GHResponse ph = gh.route(new GHRequest(start_lat,start_lon,end_lat,end_lon));
        
		if(ph.isFound()) {
	        PointList list = ph.getPoints();
	        for(int i=0; i<list.getSize();i++) {
	        	double lat = list.getLatitude(i);
	        	double lon = list.getLongitude(i);
	        	System.out.println(lat+","+lon);
	        }
		}
    }
    
    
    
    
    public static String draw(String title, Map<Move,Double> list_od, boolean directed, String region) throws Exception {
    	
    	List<double[][]> points = new ArrayList<double[][]>();
		List<Double> w = new ArrayList<Double>();	
		List<String> colors = new ArrayList<String>();
		DescriptiveStatistics stats = new DescriptiveStatistics();
		
    	
    	Map<String,Double> map = getSegmentOD(list_od,directed,region);
    	
    	
		for(Double x: map.values()) 
			stats.addValue(x);
		double p25 = stats.getPercentile(25); 
		double max = stats.getMax();
		
		
		
		for(String k: map.keySet()) {
			double weight = map.get(k);
			if(weight > p25) {
				points.add(toCoord(k));
				w.add(10 * weight / max);
				colors.add("#ff0000");
			}
		}
		
		
		String dir = Config.getInstance().web_kml_folder;
		File d = new File(dir);
		if(!d.exists()) d.mkdirs();
		
	
		printKML(dir+"/od_tmp.kml",title,points,w,colors,false);
		return ArrowsGoogleMaps.draw(dir+"/"+title+".html",title,points,w,colors,false);
    }
    
    
    // this is the same method as before but changes color for incoming/outgoing routes
    public static void draw(String title, Map<Move,Double> incoming_od, Map<Move,Double> outgoing_od, boolean directed, String region) throws Exception {
    	Map<String,Double> in_map = getSegmentOD(incoming_od,directed,region);
    	Map<String,Double> out_map = getSegmentOD(outgoing_od,directed,region);
    	
    	DescriptiveStatistics stats = new DescriptiveStatistics();
		for(Double x: in_map.values()) 
			stats.addValue(x);
		for(Double x: out_map.values()) 
			stats.addValue(x);
		double p25 = stats.getPercentile(25); 
		double max = stats.getMax();
		
		List<double[][]> points = new ArrayList<double[][]>();
		List<Double> w = new ArrayList<Double>();	
		List<String> colors = new ArrayList<String>();
		
		for(String k: in_map.keySet()) {
			double weight = in_map.get(k);
			if(weight > p25) {
				points.add(toCoord(k));
				w.add(10 * weight / max);
				colors.add("#ff0000");
			}
		}
		for(String k: out_map.keySet()) {
			double weight = out_map.get(k);
			if(weight > p25) {
				points.add(toCoord(k));
				w.add(10 * weight / max);
				colors.add("#0000ff");
			}
		}
		
		
		String dir = "BASE/ODMatrix";
		File d = new File(dir);
		if(!d.exists()) d.mkdirs();
		
		ArrowsGoogleMaps.draw(dir+"/"+title+".html",title,points,w,colors,false);
		printKML(dir+"/"+title+".kml",title,points,w,colors,false);
    }
    
    
    
	private static Map<String,Double> getSegmentOD(Map<Move,Double> list_od, boolean directed, String region) throws Exception {
		
		String n = region.substring("file_pls_".length());
		String ghLoc = "C:/DATASET/osm/"+n;
	    String testOsm = "C:/DATASET/osm/"+n+"/"+n+".pbf";
		
		GraphHopper gh = new GraphHopper().setInMemory(true, true).setEncodingManager(new EncodingManager(TRASPORTATION_MODE)).setGraphHopperLocation(ghLoc).setOSMFile(testOsm);
		gh.setPreciseIndexResolution(10000); // to be set about the grid size
		gh.importOrLoad();
		
		if(!directed) {
			Map<Move,Double> list_od_undirected = new HashMap<Move,Double>();
			// change the list_od so that a --> b and b-->a are merged together
			for(Move m: list_od.keySet()) {
				Move m2 = new Move(m.s,m.d,false);
				Double v2 = list_od_undirected.get(m2);
				if(v2 == null) v2 = 0.0;
				v2 += list_od.get(m);
				list_od_undirected.put(m2, v2);
			}
			list_od = list_od_undirected;
		}
		
		
		//List<double[][]> points = new ArrayList<double[][]>();
		//List<Double> w = new ArrayList<Double>();
		Map<String,Double> map = new HashMap<String,Double>();
		
	
		for(Move m: list_od.keySet()) {
			double weight = list_od.get(m);
			if(!m.sameSourceAndDestination()) {
				double[] p1 = new double[]{m.s.getLatLon()[0],m.s.getLatLon()[1]};
				double[] p2 = new double[]{m.d.getLatLon()[0],m.d.getLatLon()[1]};
				double[][] route;
				GHResponse ph = gh.route(new GHRequest(m.s.getLatLon()[0],m.s.getLatLon()[1],m.d.getLatLon()[0],m.d.getLatLon()[1]));
		        
				if(ph.isFound()) {
					route = new double[ph.getPoints().getSize()][2];
			        PointList list = ph.getPoints();
			        for(int i=0; i<list.getSize();i++) {
			        	route[i][0] = list.getLatitude(i);
			        	route[i][1] = list.getLongitude(i);
			        }
				}
				else { 
					// if graphhopper does not return valid path, just use the straight line between points.
					Logger.logln("!!! NO ROUTE BETWEEN: "+ m + " USE STRAIGHT LINE INSTEAD");
					route = new double[][]{p1,p2};
				}
				
				for(int i=1; i<route.length;i++) {
					double[][] segment = new double[2][2];
					segment[0][0] = route[i-1][0];
					segment[0][1] = route[i-1][1];
					segment[1][0] = route[i][0];
					segment[1][1] = route[i][1];
					String k = toKey(segment);
					Double w = map.get(k);
					if(w == null) map.put(k, weight);
					else map.put(k, w + weight);
				}
			}
		}
		
		return map;
	}
	
	
	
	
	private static String toKey(double[][] segment) {
		String k =  segment[0][0]+","+segment[0][1]+","+segment[1][0]+","+segment[1][1];
		return k;
	}
	private static double[][] toCoord(String k) {
		String[] e = k.split(",");
		double[][] segment = new double[2][2];
		segment[0][0] = Double.parseDouble(e[0]);
		segment[0][1] = Double.parseDouble(e[1]);
		segment[1][0] = Double.parseDouble(e[2]);
		segment[1][1] = Double.parseDouble(e[3]);
		return segment;
	}
	
	
	private static void printKML(String file, String title, List<double[][]> points, List<Double> weights, List<String> colors, boolean directed) throws Exception {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		KML kml = new KML();
		kml.printHeaderFolder(out, title);
		kml.printFolder(out, "incoming");
		for(int i=0; i<points.size();i++) {
			double[][] p = points.get(i);
			double w = weights.get(i);
			if(i>0 && !colors.get(i).equals(colors.get(i-1))) {
				kml.closeFolder(out);
				kml.printFolder(out, "outgoing");
			}
			//out.println(KMLArrow.printArrow(p[0][1], p[0][0], p[1][1], p[1][0], w, "#ff0000ff",directed));
			out.println(KMLArrow.printArrow(p, w, html2kmlColor(colors.get(i)),directed));
		}
		kml.closeFolder(out);
		kml.printFooterFolder(out);
		out.close();
	}
	
	private static String html2kmlColor(String rgb) {
    	String r = rgb.substring(1,3);
    	String g = rgb.substring(3,5);
    	String b = rgb.substring(5);
    	return "#ff"+b+g+r;
    }
	
}