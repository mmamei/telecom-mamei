package analysis.densityANDflows;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.stat.regression.SimpleRegression;

import utils.Colors;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;
import visual.GraphScatterPlotter;
import visual.Kml;
import area.region.ParserDatiISTAT;
import area.region.Region;
import area.region.RegionMap;
import area.region.SpaceGrid;

public class PopulationDensity {
	public static void main(String[] args) throws Exception {
		String region = "TorinoGrid20";
		String kind_of_place = "SATURDAY_NIGHT";
		String exclude_kind_of_place = "HOME";
		
		File input_obj_file = new File(Config.getInstance().base_dir+"/cache/"+region+".ser");
		if(!input_obj_file.exists()) {
			System.out.println(input_obj_file+" does not exist... run the region parser first!");
			System.exit(0);
		}
		
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(input_obj_file); 
		Map<String,UserPlaces> up = UserPlaces.readUserPlaces(Config.getInstance().base_dir+"/PlaceRecognizer/file_pls_piem_users_above_2000/results.csv");
		
		
		Map<String,Double> density = process(rm,up,kind_of_place,exclude_kind_of_place);
		String description = kind_of_place;
		
		
		
		printKML(density,rm,description,true);
		
		
	
		Logger.logln("Done!");
	}
	
	
	public static Map<String,Double> process(RegionMap rm, Map<String,UserPlaces> up, String kind_of_place, String exclude_kind_of_place) {
		
		Map<String,Double> density = new HashMap<String,Double>();
		
		for(UserPlaces p: up.values()) {
			List<double[]> lkop = p.places.get(kind_of_place);
			List<double[]> lnokop = p.places.get(exclude_kind_of_place);
			List<double[]> r = exclude_nopkop(rm,lkop,lnokop);
			if(r != null)
			for(double[] ll: r) {
				Region reg = rm.get(ll[0], ll[1]);
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
	
	
	public static List<double[]> exclude_nopkop(RegionMap rm, List<double[]> kop, List<double[]> nokop) {
		if(kop==null) return null;
		if(nokop==null) return kop;
		List<double[]> r = new ArrayList<double[]>();
		for(double[] p1: kop) {
			boolean found = false;
			Region r1 = rm.get(p1[0],p1[1]);
			for(double[] p2 : nokop) {
				Region r2 = rm.get(p2[0],p2[1]);;
				if(r1!=null && r2!=null && r1.equals(r2)) found = true;
			}
			if(!found) r.add(p1);
		}
		return r;
	}
	
	public static void printKML(Map<String,Double> den, RegionMap rm , String desc, boolean logscale) throws Exception {
		
		
		Map<String,Double> density = new HashMap<String,Double>();
		for(String r: den.keySet())
			density.put(r, den.get(r).doubleValue());
		
		// convert to the log scale,
		if(logscale) 
			for(String name: density.keySet()) 
				density.put(name, Math.max(0, Math.log10(density.get(name))));	
		
		
		//compute the maximum value in density
		double max = 0;
		for(double v: density.values()) 
			max = Math.max(max, v);
		
		String dir = Config.getInstance().base_dir+"/PopulationDensity";
		File d = new File(dir);
		if(!d.exists()) d.mkdirs();
		
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(dir+"/"+rm.getName()+"_"+desc+".kml")));
		Kml kml = new Kml();
		kml.printHeaderFolder(out, rm.getName());
		
		for(Region r: rm.getRegions()) {
			double val = density.get(r.getName())==null? 0 : density.get(r.getName());
			int index = Colors.HEAT_COLORS.length - 1 - (int)(val/max * (Colors.HEAT_COLORS.length-1));
			String description = desc+" DENSITY = "+(logscale ? Math.pow(10, val) : val);
			out.println(r.toKml("ff"+Colors.rgb2kmlstring(Colors.HEAT_COLORS[index]),description));
		}
		
		kml.printFooterFolder(out);
		out.close();
		
		Logger.logln("Done");
	}
}
