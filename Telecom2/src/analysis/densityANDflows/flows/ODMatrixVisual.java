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

import utils.Config;
import utils.Logger;
import visual.html.ArrowsGoogleMaps;
import visual.kml.KML;
import visual.kml.KMLArrow;

public class ODMatrixVisual {
		
	
	public static void draw(String title, Map<Move,Double> list_od, boolean directed) throws Exception {
		
		
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
		
		
		List<double[][]> points = new ArrayList<double[][]>();
		List<Double> w = new ArrayList<Double>();
		
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for(double x: list_od.values()) 
			stats.addValue(x);


		double p25 = stats.getPercentile(25); 
		double p50 = stats.getPercentile(50); 
		double p75 = stats.getPercentile(75); 

		
		Logger.logln("Percentiles:");
		Logger.logln("25th percentile = "+p25);
		Logger.logln("50th percentile = "+p50);
		Logger.logln("75th percentile = "+p75);
	
		
		for(Move m: list_od.keySet()) {
			double weight = list_od.get(m);
			if(!m.sameSourceAndDestination() && weight > p25) {
				double[] p1 = new double[]{m.s.getCenterLat(),m.s.getCenterLon()};
				double[] p2 = new double[]{m.d.getCenterLat(),m.d.getCenterLon()};
				points.add(new double[][]{p1,p2});
				
				if(weight < p50) w.add(1.0);
				else if(weight < p75) w.add(2.0);
				else w.add(3.0);	
			}
		}
		
		
		String dir = Config.getInstance().base_dir+"/ODMatrix";
		File d = new File(dir);
		if(!d.exists()) d.mkdirs();
		
		ArrowsGoogleMaps.draw(dir+"/"+title+".html",title,points,w,directed);
		printKML(dir+"/"+title+".kml",title,points,w,directed);
	}
	
	
	public static void printKML(String file, String title, List<double[][]> points, List<Double> weights, boolean directed) throws Exception {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		KML kml = new KML();
		kml.printHeaderFolder(out, title);
		
		for(int i=0; i<points.size();i++) {
			double[][] p = points.get(i);
			double w = weights.get(i);
			out.println(KMLArrow.printArrow(p[0][1], p[0][0], p[1][1], p[1][0], w, "#ff0000ff",directed));
		}
		
		kml.printFooterFolder(out);
		out.close();
	}
	
}