package analysis.densityANDflows.density;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import region.CreatorRegionMapGrid;
import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;
import visual.html.HeatMapGoogleMaps;
import visual.kml.KMLHeatMap;
import analysis.Constraints;

public class PopulationDensityPlaces {
	
	
	public static void main(String[] args) throws Exception {
		PopulationDensityPlaces pdp = new PopulationDensityPlaces();
		
		//pdp.runAll(Config.getInstance().base_folder+"/PlaceRecognizer/file_pls_piem_users_200_100/results.csv", "FIX_Piemonte.ser", "HOME", "SATURDAY_NIGHT","",0,0,0,0);
		
		
		//pdp.runAll(Config.getInstance().base_folder+"/PlaceRecognizer/file_pls_piem_users_200_10000/results_piem.csv", "torino_circoscrizioni_geo.ser", "SATURDAY_NIGHT", "HOME","",0,0,0,0);
		//pdp.runAll(Config.getInstance().base_folder+"/PlaceRecognizer/file_pls_lomb_users_200_10000/results_lomb.csv", "milano_circoscrizioni_geo.ser", "SUNDAY", "HOME","",0,0,0,0);
		
		//pdp.runAll(Config.getInstance().base_folder+"/PlaceRecognizer/fast_home_Torino.csv", "torino_circoscrizioni_geo.ser", "HOME", null,"",0,0,0,0);
		pdp.runAll(Config.getInstance().base_folder+"/PlaceRecognizer/fast_home_Milano.csv", "milano_circoscrizioni_geo.ser", "HOME", null,"",0,0,0,0);
		
		Logger.logln("Done!");
	}
	
	public String runAll(String places_file, String regionMap, String kind_of_place, String exclude_kind_of_place, String sconstraints,double minlat,double minlon, double maxlat,double maxlon) {
		return runAll(places_file,regionMap,kind_of_place,exclude_kind_of_place,new Constraints(sconstraints),minlat,minlon,maxlat,maxlon);
	}
	
	public String runAll(String places_file, String regionMap, String kind_of_place, String exclude_kind_of_place, Constraints constraints, double minlat,double minlon, double maxlat,double maxlon) {
		try {
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
			
			System.out.println("---> ("+minlon+","+minlat+") ("+maxlon+","+maxlat+")");
			*/
			double[][] lonlat_bbox = new double[][]{{minlon,minlat},{maxlon,maxlat}};
			int size = Integer.parseInt(regionMap.substring("grid".length()));
			rm = CreatorRegionMapGrid.process("grid", lonlat_bbox, size);
		}
		else rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/"+regionMap));
		
	
		
		Map<String,Double> space_density = computeSpaceDensity(rm,up,kind_of_place,exclude_kind_of_place,constraints);
		
		//for(double x: space_density.values())
		//	System.out.println(x);
		
		
		String title = rm.getName()+"-"+kind_of_place+"-"+exclude_kind_of_place;	
		plotSpaceDensity(title, space_density, rm,0);
		
		
		StringBuffer sb = new StringBuffer();
		sb.append("var heatMapData = [\n");
		for(String key: space_density.keySet()) {
			System.err.println(key);
			double[] latlon = rm.getRegion(key.toUpperCase()).getLatLon();
			sb.append("{location: new google.maps.LatLng("+latlon[0]+", "+latlon[1]+"), weight: "+space_density.get(key)+"},");
		}
		sb.append("];\n");
		
		
		return sb.toString();
		
		
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
		
		
	}
	
	
	public void plotSpaceDensity(String title, Map<String,Double> space_density, RegionMap rm, double threshold) throws Exception {
		File d = new File(Config.getInstance().web_kml_folder);
		d.mkdirs();
		KMLHeatMap.drawHeatMap(d.getAbsolutePath()+"/"+title+".kml",space_density,rm,title,true);
		HeatMapGoogleMaps.draw(d.getAbsolutePath()+"/"+title+".html", title, space_density, rm, threshold);
	}
	
	
	
	
	public Map<String,Double> computeSpaceDensity(RegionMap rm, Map<String,UserPlaces> up, String kind_of_place, String exclude_kind_of_place,Constraints constraints) {
		
		Map<String,Double> density = new HashMap<String,Double>();
		
		for(UserPlaces p: up.values()) {
			
			List<double[]> lkop = p.lonlat_places.get(kind_of_place);
			List<double[]> lnokop = p.lonlat_places.get(exclude_kind_of_place);
			List<double[]> r = exclude_nopkop(rm,lkop,lnokop);
			if(r != null)
			for(double[] ll: r) {
				RegionI reg = rm.get(ll[0], ll[1]);
				if(reg == null) {
					//Logger.logln(ll[0]+","+ll[1]+" is outside "+rm.getName());
				}
				else {
					Double val = density.get(reg.getName().toLowerCase());
					if(val == null) val = 0.0;
					val += constraints.weight(p.username);
					density.put(reg.getName().toLowerCase(), val);
				}
			}
		}
		return density;
	}
	
	
	private List<double[]> exclude_nopkop(RegionMap rm, List<double[]> kop, List<double[]> nokop) {
		if(kop==null) return null;
		if(nokop==null) return kop;
		List<double[]> r = new ArrayList<double[]>();
		for(double[] p1: kop) {
			boolean found = false;
			RegionI r1 = rm.get(p1[0],p1[1]);
			for(double[] p2 : nokop) {
				RegionI r2 = rm.get(p2[0],p2[1]);;
				if(r1!=null && r2!=null && r1.equals(r2)) found = true;
			}
			if(!found) r.add(p1);
		}
		return r;
	}
	
	
}
