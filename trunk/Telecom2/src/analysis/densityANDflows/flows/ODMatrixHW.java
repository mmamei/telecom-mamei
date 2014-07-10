package analysis.densityANDflows.flows;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dataset.EventFilesFinderI;
import dataset.file.DataFactory;
import region.CreatorRegionMapGrid;
import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;
import analysis.Constraints;
import analysis.densityANDflows.density.UserPlaces;

public class ODMatrixHW {
	public static void main(String[] args) throws Exception {
		//String regionMap = "FIX_Piemonte.ser";
		String regionMap = "grid5";
		String places_file = Config.getInstance().base_folder+"/PlaceRecognizer/file_pls_piem_users_200_100/results.csv";
		ODMatrixHW od = new ODMatrixHW();
		String js = od.runAll(places_file, regionMap, "",0,0,0,0);
		System.out.println(js);
		Logger.log("Done!");
	}
	
	public String runAll(String places_file, String regionMap, String sconstraints,double minlat,double minlon, double maxlat,double maxlon) {
		return runAll(places_file,regionMap,new Constraints(sconstraints),minlat,minlon,maxlat,maxlon);
	}
	
	public String runAll(String places_file, String regionMap, Constraints constraints,double minlat,double minlon, double maxlat,double maxlon) {
		
		try {			
			String region = places_file.substring("C:/BASE/PlaceRecognizer/".length(),places_file.indexOf("_users"));
			
			Map<String,UserPlaces> up = UserPlaces.readUserPlaces(places_file);
			
			
			// load the region map
			RegionMap rm = null;
			if(regionMap.startsWith("grid")) {
				/*
				double minlon = Double.MAX_VALUE;
				double minlat = Double.MAX_VALUE;
				double maxlon = -Double.MAX_VALUE;
				double maxlat = -Double.MAX_VALUE;
				// get user places bbox
				for(UserPlaces x: up.values()) 
				for(List<double[]> l: x.lonlat_places.values()) 
				for(double[] lonlat: l) {
					minlon = Math.min(minlon, lonlat[0]);
					minlat = Math.min(minlat, lonlat[1]);
					maxlon = Math.max(maxlon, lonlat[0]);
					maxlat = Math.max(maxlat, lonlat[1]);
				}	
				*/		
				double[][] lonlat_bbox = new double[][]{{minlon,minlat},{maxlon,maxlat}};
				int size = Integer.parseInt(regionMap.substring("grid".length()));
				rm = CreatorRegionMapGrid.process("grid", lonlat_bbox, size);
			}
			else rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/"+regionMap));
						
			
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
			return ODMatrixVisual.draw("ODMatrixHW_"+region,list_od,false,region);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}