package analysis.densityANDflows.flows;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;
import visual.html.ArrowsGoogleMaps;
import visual.kml.KML;
import visual.kml.KMLArrow;
import analysis.densityANDflows.density.UserPlaces;
import area.region.Region;
import area.region.RegionMap;

public class ODMatrix {
	public static void main(String[] args) throws Exception {
		
		String region = "Piemonte";//"TorinoGrid20";
		File input_obj_file = new File(Config.getInstance().base_dir+"/cache/"+region+".ser");
		if(!input_obj_file.exists()) {
			System.out.println(input_obj_file+" does not exist... run the region parser first!");
			System.exit(0);
		}
		
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(input_obj_file); 
		
		
		Map<String,UserPlaces> up = UserPlaces.readUserPlaces(Config.getInstance().base_dir+"/PlaceRecognizer/file_pls_piem_users_above_2000/results.csv");
		
		
		Map<Move,Double> list_od = new HashMap<Move,Double>();
		
		for(UserPlaces p: up.values()) {
			List<double[]> homes = p.places.get("HOME");
			List<double[]> works = p.places.get("WORK");
			if(homes != null && works !=null) {
				double z = 1.0 / homes.size() * works.size();
				for(double[] h: homes)
				for(double[] w: works) {
					
					Region rh = rm.get(h[0], h[1]);
					Region rw = rm.get(w[0], w[1]);

					if(rh!=null && rw!=null) {
						Move m = new Move(rh,rw);
						Double c = list_od.get(m);
						c = c == null ? z : c+z;
						list_od.put(m, c);
					}
				}
			}
		}
		
		// prepare for drawing
		draw(region,list_od);
		
		Logger.log("Done!");
	}
	
	
	public static void draw(String title, Map<Move,Double> list_od) throws Exception {
		List<double[][]> points = new ArrayList<double[][]>();
		List<Double> w = new ArrayList<Double>();
		
		for(Move m: list_od.keySet()) {
			double weight = list_od.get(m);
			if(!m.sameSourceAndDestination() && weight>5) {
				double[] p1 = new double[]{m.s.getCenterLat(),m.s.getCenterLon()};
				double[] p2 = new double[]{m.d.getCenterLat(),m.d.getCenterLon()};
				points.add(new double[][]{p1,p2});
				w.add(weight);
			}
		}
		
		
		String dir = Config.getInstance().base_dir+"/ODMatrix";
		File d = new File(dir);
		if(!d.exists()) d.mkdirs();
		
		ArrowsGoogleMaps.draw(dir+"/od"+title+".html","OD-HOME-WORK",points,w);
		printKML(dir+"/od"+title+".kml","OD-HOME-WORK",points,w);
	}
	
	
	public static void printKML(String file, String title, List<double[][]> points, List<Double> weights) throws Exception {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		KML kml = new KML();
		kml.printHeaderFolder(out, title);
		
		for(int i=0; i<points.size();i++) {
			double[][] p = points.get(i);
			double w = weights.get(i);
			out.println(KMLArrow.printArrow(p[0][1], p[0][0], p[1][1], p[1][0], w, "#ff0000ff"));
		}
		
		kml.printFooterFolder(out);
		out.close();
	}
	
}