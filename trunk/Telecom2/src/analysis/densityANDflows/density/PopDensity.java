package analysis.densityANDflows.density;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import region.RegionI;
import region.RegionMap;
import utils.CopyAndSerializationUtils;
import utils.FileUtils;
import utils.Logger;
import visual.html.HeatMapGoogleMaps;
import visual.java.GraphPlotter;
import visual.kml.KMLHeatMap;

public class PopDensity {
	
	public static final int TIM = 0;
	public static final int ROAMING = 1;
	public static final int ALL = 2;
	public static final String[] U_SEGMENT = new String[]{"TIM","ROAMING","ALL"};
	
	public static final int MIN_DAYS = 1;
	public static final int MAX_DAYS = 3;
	
	public static final int U_SEG = ROAMING;
	
	public static void main(String[] args) throws Exception {
		String city = "Venezia";
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(FileUtils.getFile("RegionMap/"+city+".ser"));
		Map<String,Double> space_density = computeSpaceDensity(rm);
		plotSpaceDensity(city+"_"+MIN_DAYS+"_"+MAX_DAYS+"_"+U_SEGMENT[U_SEG], space_density, rm,0);
		Logger.logln("Done!");
	}
		
	
	public static void plotSpaceDensity(String city, Map<String,Double> space_density, RegionMap rm, double threshold) throws Exception {
		File d = FileUtils.createDir("BASE/TouristActivity");
		KMLHeatMap.drawHeatMap(d.getAbsolutePath()+"/"+city+".kml",space_density,rm,city,false);
		HeatMapGoogleMaps.draw(d.getAbsolutePath()+"/"+city+".html", city, space_density, rm, threshold);
	}
	
	
	public static Map<String,Double> computeSpaceDensity(RegionMap rm) throws Exception {
		String city = rm.getName();
		
		
		File f = FileUtils.getFile("BASE/Tourist/"+city+".csv");
		if(f == null) {
			Logger.logln("Run TouristData4Analysis first!");
			System.exit(0);
		}
		BufferedReader br = new BufferedReader(new FileReader(f));
		
		
		Map<String,Double> sd = new HashMap<String,Double>();
		for(RegionI r: rm.getRegions())
			sd.put(r.getName(), 0.0);
		
		String line;
		while((line=br.readLine())!=null) {
			String[] p = line.split(",");
			if(skip(p[1],Integer.parseInt(p[2]),Integer.parseInt(p[3]))) continue;
			for(int i=4;i<p.length;i++) {
				String[] x = p[i].split(":");
				String rname = rm.getRegion(Integer.parseInt(x[2])).getName();
				double v = Double.parseDouble(x[3]);	
				sd.put(rname,sd.get(rname)+v);
			}
		}
		br.close();
		
		for(String k : sd.keySet()) {
			double val = sd.get(k);
			double area = rm.getRegion(k).getGeom().getArea();
			sd.put(k, val/area);
		}
		
		return sd;
	}
	
	
	
	public static boolean skip(String mnt, int num_pls, int num_days) {
		if(U_SEG == TIM && !mnt.equals("22201")) return true;
		if(U_SEG == ROAMING && mnt.equals("22201")) return true;
		if(num_days < MIN_DAYS || num_days > MAX_DAYS) return true;
		return false;
	}
	
	
}
