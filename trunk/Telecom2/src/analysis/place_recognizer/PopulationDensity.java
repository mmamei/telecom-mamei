package analysis.place_recognizer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math.stat.regression.SimpleRegression;

import utils.Colors;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;
import visual.GraphScatterPlotter;
import visual.Kml;
import area.CityEvent;
import area.ParserDatiISTAT;
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
		
		compareWithISTAT(density,region);
	
		Logger.logln("Done!");
	}
	
	
	public static void compareWithISTAT(Map<String,Double> density, String region) throws Exception {
		Map<String,Integer> istat = ParserDatiISTAT.load(region);
		int size = 0;
		for(String r: density.keySet()) {
			int estimated = density.get(r).intValue();
			Integer groundtruth = istat.get(r);
			if(groundtruth != null) {
				size++;
				//System.out.println(r+","+estimated+","+groundtruth);
			}
		}
		
		
		double[][] result = new double[size][2];
		int i = 0;
		for(String r: density.keySet()) {
			int estimated = density.get(r).intValue();
			Integer groundtruth = istat.get(r);
			if(groundtruth != null && estimated>0 && groundtruth>0) {
				result[i][0] = Math.log10(estimated);
				result[i][1] = Math.log10(groundtruth);
				i++;
			}
		}
		
		
		SimpleRegression sr = new SimpleRegression();
		sr.addData(result);
		Logger.logln("r="+sr.getR()+", r^2="+sr.getRSquare()+", sse="+sr.getSumSquaredErrors());
		new GraphScatterPlotter("Result: "+region,"Estimated (log10)","GroundTruth (log10)",result);
		
		
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
	
	public static void printKML(Map<String,Double> den, RegionMap rm , String kop, boolean logscale) throws Exception {
		
		
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
		
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(dir+"/"+rm.getName()+"_"+kop+".kml")));
		Kml kml = new Kml();
		kml.printHeaderFolder(out, rm.getName());
		
		for(Region r: rm.getRegions()) {
			double val = density.get(r.getName())==null? 0 : density.get(r.getName());
			int index = Colors.HEAT_COLORS.length - 1 - (int)(val/max * (Colors.HEAT_COLORS.length-1));
			String desc = kop+" DENSITY = "+(logscale ? Math.pow(10, val) : val);
			out.println(r.toKml("ff"+Colors.rgb2kmlstring(Colors.HEAT_COLORS[index]),desc));
		}
		
		kml.printFooterFolder(out);
		out.close();
		
		Logger.logln("Done");
	}
}
