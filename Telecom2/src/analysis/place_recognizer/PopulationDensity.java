package analysis.place_recognizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import utils.Colors;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;
import visual.Kml;
import area.Region;
import area.RegionMap;

public class PopulationDensity {
	public static void main(String[] args) throws Exception {
		String region = "Piemonte";
		String kind_of_place = "HOME";
		
		File input_obj_file = new File(Config.getInstance().base_dir+"/cache/"+region+".ser");
		if(!input_obj_file.exists()) {
			System.out.println(input_obj_file+" does not exist... run the region parser first!");
			System.exit(0);
		}
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(input_obj_file); 
		Map<String,Double> density = process(rm,kind_of_place);
		printKML(density,rm,kind_of_place,true);
		Logger.logln("Done!");
	}
	
	public static Map<String,Double> process(RegionMap rm, String kind_of_place) throws Exception {
		
		Map<String,Double> density = new HashMap<String,Double>();
		
		BufferedReader br = new BufferedReader(new FileReader(Config.getInstance().base_dir+"/PlaceRecognizer/file_pls_piem_users_above_2000/results.csv"));
		String line;
		String[] elements;
		while((line = br.readLine())!=null) {
			if(line.contains(","+kind_of_place+",")) {
				line = line.substring(line.indexOf(kind_of_place)+kind_of_place.length()+1, line.length());
				elements = line.split(",");
				for(String c: elements) {
					double lon = Double.parseDouble(c.substring(0,c.indexOf(" ")));
					double lat = Double.parseDouble(c.substring(c.indexOf(" ")+1));
					Region reg = rm.get(lon, lat);
					if(reg == null) 
						Logger.logln(lon+","+lat+" is outside "+rm.getName());
					else {
						Double val = density.get(reg.getName());
						if(val == null) val = 0.0;
						val += 1.0 / elements.length;
						density.put(reg.getName(), val);
					}
				}
			}
		}
		br.close();
		return density;
	}
	
	public static void printKML(Map<String,Double> density, RegionMap rm , String kop, boolean logscale) throws Exception {
	
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
		
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(dir+"/"+rm.getName()+"_"+kop+".kml")));
		Kml kml = new Kml();
		kml.printHeaderFolder(out, rm.getName());
		
		for(Region r: rm.getRegions()) {
			Double val = density.get(r.getName());
			if(val == null) val = 0.0;
			int index = Colors.HEAT_COLORS.length - 1 - (int)(val/max * (Colors.HEAT_COLORS.length-1));
			String desc = kop+" DENSITY = "+(logscale ? Math.pow(10, val) : val);
			out.println(r.toKml("ff"+Colors.rgb2kmlstring(Colors.HEAT_COLORS[index]),desc));
		}
		
		kml.printFooterFolder(out);
		out.close();
		
		Logger.logln("Done");
	}
}
