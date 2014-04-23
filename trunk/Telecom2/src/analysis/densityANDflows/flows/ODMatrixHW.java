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

public class ODMatrixHW {
	public static void main(String[] args) throws Exception {
		
		String region = "Piemonte";//"TorinoGrid20";
		File input_obj_file = new File("BASE/cache/"+region+".ser");
		if(!input_obj_file.exists()) {
			System.out.println(input_obj_file+" does not exist... run the region parser first!");
			System.exit(0);
		}
		
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(input_obj_file); 
		
		
		Map<String,UserPlaces> up = UserPlaces.readUserPlaces("BASE/PlaceRecognizer/file_pls_piem_users_above_2000/results.csv");
		
		
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
		ODMatrixVisual.draw("ODMatrixHW_"+region,list_od,false);
		
		Logger.log("Done!");
	}
}