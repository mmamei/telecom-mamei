package analysis.densityANDflows.flows;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;
import analysis.densityANDflows.density.UserPlaces;

public class ODMatrixHW {
	public static void main(String[] args) throws Exception {
		
		
		String regionMap = "FIX_Piemonte.ser";
		String region = regionMap.substring("FIX_".length(),regionMap.indexOf("."));
		
		RegionMap rm = (RegionMap)(RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/"+regionMap));
		Map<String,UserPlaces> up = UserPlaces.readUserPlaces(Config.getInstance().base_folder+"/PlaceRecognizer/file_pls_piem_users_200_100/results.csv");
		
		
		
		
		Map<Move,Double> list_od = new HashMap<Move,Double>();
		
		for(UserPlaces p: up.values()) {
			List<double[]> homes = p.lonlat_places.get("HOME");
			List<double[]> works = p.lonlat_places.get("WORK");
			if(homes != null && works !=null) {
				double z = 1.0 / homes.size() * works.size();
				for(double[] h: homes)
				for(double[] w: works) {
					
					RegionI rh = rm.get(h[0], h[1]);
					RegionI rw = rm.get(w[0], w[1]);

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
		ODMatrixVisual.draw("ODMatrixHW_"+region,list_od,false,"file_pls_piem");
		
		Logger.log("Done!");
	}
}