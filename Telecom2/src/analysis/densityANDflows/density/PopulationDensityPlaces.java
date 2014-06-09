package analysis.densityANDflows.density;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gps.utils.LatLonPoint;
import org.gps.utils.LatLonUtils;

import region.CreatorRegionMapGrid;
import region.Placemark;
import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;
import visual.html.HeatMapGoogleMaps;
import visual.kml.KMLHeatMap;
import analysis.PLSSpaceDensity;
import dataset.DataFactory;
import dataset.EventFilesFinderI;
import dataset.file.UserEventCounterCellacXHour;

public class PopulationDensityPlaces {
	
	
	public static void main(String[] args) throws Exception {
		PopulationDensityPlaces pdp = new PopulationDensityPlaces();
		pdp.runAll("BASE/PlaceRecognizer/file_pls_piem_users_above_2000/results.csv", "FIX_Piemonte.ser", "HOME", "SATURDAY_NIGHT");
		pdp.runAll("BASE/PlaceRecognizer/file_pls_piem_users_above_2000/results.csv", "FIX_Piemonte.ser", "HOME", null);
		Logger.logln("Done!");
	}
	
	
	public String runAll(String places_file, String regionMap, String kind_of_place, String exclude_kind_of_place) {
		try {
		Map<String,UserPlaces> up = UserPlaces.readUserPlaces(places_file);
		
	
		// load the region map
		RegionMap rm = null;
		if(regionMap.startsWith("grid")) {
			
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
			double[][] lonlat_bbox = new double[][]{{minlon,minlat},{maxlon,maxlat}};
			int size = Integer.parseInt(regionMap.substring("grid".length()));
			rm = CreatorRegionMapGrid.process("grid", lonlat_bbox, size);
		}
		else rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/"+regionMap));
		
	
		
		Map<String,Double> space_density = computeSpaceDensity(rm,up,kind_of_place,exclude_kind_of_place);
		String title = rm.getName()+"-"+kind_of_place+"-"+exclude_kind_of_place;	
		plotSpaceDensity(title, space_density, rm,0);
		
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public void plotSpaceDensity(String city, Map<String,Double> space_density, RegionMap rm, double threshold) throws Exception {
		File d = new File(Config.getInstance().web_kml_folder);
		d.mkdirs();
		KMLHeatMap.drawHeatMap(d.getAbsolutePath()+"/"+city+"_"+rm.getName()+".kml",space_density,rm,city,false);
		HeatMapGoogleMaps.draw(d.getAbsolutePath()+"/"+city+"_"+rm.getName()+".html", city, space_density, rm, threshold);
	}
	
	
	
	
	public Map<String,Double> computeSpaceDensity(RegionMap rm, Map<String,UserPlaces> up, String kind_of_place, String exclude_kind_of_place) {
		
		Map<String,Double> density = new HashMap<String,Double>();
		
		for(UserPlaces p: up.values()) {
			List<double[]> lkop = p.lonlat_places.get(kind_of_place);
			List<double[]> lnokop = p.lonlat_places.get(exclude_kind_of_place);
			List<double[]> r = exclude_nopkop(rm,lkop,lnokop);
			if(r != null)
			for(double[] ll: r) {
				RegionI reg = rm.get(ll[0], ll[1]);
				if(reg == null) 
					Logger.logln(ll[0]+","+ll[1]+" is outside "+rm.getName());
				else {
					Double val = density.get(reg.getName());
					if(val == null) val = 0.0;
					val += 1.0;
					density.put(reg.getName(), val);
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
