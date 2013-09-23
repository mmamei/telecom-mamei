package analysis.densityANDflows.flows;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.PointList;

import utils.Config;
import utils.Logger;
import visual.html.ArrowsGoogleMaps;
import visual.kml.KML;
import visual.kml.KMLArrow;

public class ODMatrixVisual2 {
		
	
	private static final String ghLoc = "C:/DATASET/graph_hopper";
    private static final String testOsm = "C:/DATASET/graph_hopper/piemonte.pbf";
	
	public static void draw(String title, Map<Move,Double> list_od, boolean directed) throws Exception {
		
		GraphHopper gh = new GraphHopper().setInMemory(true, true).setEncodingManager(new EncodingManager("CAR")).setGraphHopperLocation(ghLoc).setOSMFile(testOsm);
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
				double[] p1 = new double[]{m.s.getCenterLat(),m.s.getCenterLon()};
				double[] p2 = new double[]{m.d.getCenterLat(),m.d.getCenterLon()};
				double[][] route;
				GHResponse ph = gh.route(new GHRequest(m.s.getCenterLat(),m.s.getCenterLon(),m.d.getCenterLat(),m.d.getCenterLon()));
		        
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
		
		
	
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for(Double x: map.values()) 
			stats.addValue(x);
		double p25 = stats.getPercentile(25); 
		double p50 = stats.getPercentile(50); 
		double p75 = stats.getPercentile(75); 
		double max = stats.getMax();
		
		List<double[][]> points = new ArrayList<double[][]>();
		List<Double> w = new ArrayList<Double>();		
		for(String k: map.keySet()) {
			double weight = map.get(k);
			if(weight > p25) {
				points.add(toCoord(k));
				/*
				if(weight < p50) w.add(1.0);
				else if(weight < p75) w.add(4.0);
				else w.add(8.0);	
				*/
				w.add(10 * weight / max);
			}
		}
		
		
		String dir = Config.getInstance().base_dir+"/ODMatrix";
		File d = new File(dir);
		if(!d.exists()) d.mkdirs();
		
		ArrowsGoogleMaps.draw(dir+"/"+title+".html",title,points,w,false);
		printKML(dir+"/"+title+".kml",title,points,w,false);
	}
	
	
	
	
	public static String toKey(double[][] segment) {
		String k =  segment[0][0]+","+segment[0][1]+","+segment[1][0]+","+segment[1][1];
		return k;
	}
	public static double[][] toCoord(String k) {
		String[] e = k.split(",");
		double[][] segment = new double[2][2];
		segment[0][0] = Double.parseDouble(e[0]);
		segment[0][1] = Double.parseDouble(e[1]);
		segment[1][0] = Double.parseDouble(e[2]);
		segment[1][1] = Double.parseDouble(e[3]);
		return segment;
	}
	
	
	public static void printKML(String file, String title, List<double[][]> points, List<Double> weights, boolean directed) throws Exception {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		KML kml = new KML();
		kml.printHeaderFolder(out, title);
		
		for(int i=0; i<points.size();i++) {
			double[][] p = points.get(i);
			double w = weights.get(i);
			//out.println(KMLArrow.printArrow(p[0][1], p[0][0], p[1][1], p[1][0], w, "#ff0000ff",directed));
			out.println(KMLArrow.printArrow(p, w, "#ff0000ff",directed));
		}
		
		kml.printFooterFolder(out);
		out.close();
	}
	
}