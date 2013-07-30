package analysis.densityANDflows;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import utils.Config;
import utils.Logger;
import visual.ArrowsGoogleMaps;
import area.region.SpaceGrid;

public class ODMatrixGrid {
	public static void main(String[] args) throws Exception {
		int size = 20;
		double[][] bbox = new double[][]{{7.494789211677311, 44.97591738081519},{7.878659418860384, 45.16510171374535}};
		SpaceGrid sg = new SpaceGrid(bbox[0][0],bbox[0][1],bbox[1][0],bbox[1][1],size,size);
		Map<String,UserPlaces> up = UserPlaces.readUserPlaces(Config.getInstance().base_dir+"/PlaceRecognizer/file_pls_piem_users_above_2000/results.csv");
		int[] gsize = sg.size();
		// associated "i1,j1->i2,j2" to the counter c
		// example "0,0->1,2" = 3 meaning 3 users commute from 0,0 to 1,2
		Map<String,Double> list_od = new TreeMap<String,Double>();
		
		for(UserPlaces p: up.values()) {
			List<double[]> homes = p.places.get("HOME");
			List<double[]> works = p.places.get("WORK");
			if(homes != null && works !=null) {
				double z = 1.0 / homes.size() * works.size();
				for(double[] h: homes)
				for(double[] w: works) {
					int[] hij = sg.getGridCoord(h[0], h[1]);
					int[] wij = sg.getGridCoord(w[0], w[1]);
					
					if(hij[0] >= 0 && hij[0] < gsize[0] && hij[1] >= 0 && hij[1] < gsize[1] &&
					wij[0] >= 0 && wij[0] < gsize[0] && wij[1] >= 0 && wij[1] < gsize[1]) {
						
						String key = hij[0]+","+hij[1]+"->"+wij[0]+","+wij[1];
						Double c = list_od.get(key);
						c = c == null ? z : c+z;
						list_od.put(key, c);
					}
				}
			}
		}
		
		
		// prepare for drawing
		
		String dir = Config.getInstance().base_dir+"/ODMatrixGrid";
		File d = new File(dir);
		if(!d.exists()) d.mkdirs();
		
		List<double[][]> points = new ArrayList<double[][]>();
		List<Double> w = new ArrayList<Double>();
		
		for(String k: list_od.keySet()) {
			String[] x = k.split("->");
			String[] ij1 = x[0].split(",");
			String[] ij2 = x[1].split(",");
			int i1 = Integer.parseInt(ij1[0]);
			int j1 = Integer.parseInt(ij1[1]);
			int i2 = Integer.parseInt(ij2[0]);
			int j2 = Integer.parseInt(ij2[1]);
			double weight = list_od.get(k);
			if((i1 != i2 || j1 != j2) && weight>5) {
				double[] p1 = sg.grid2LatLon(i1, j1);
				double[] p2 = sg.grid2LatLon(i2, j2);
				points.add(new double[][]{p1,p2});
				w.add(1.0);
			}
			
		}
		
		ArrowsGoogleMaps.draw(dir+"/od.html","OD-HOME-WORK",points,w);
		Logger.log("Done!");
		
			
	}
}