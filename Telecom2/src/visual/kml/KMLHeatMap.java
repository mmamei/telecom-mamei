package visual.kml;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import region.RegionI;
import region.RegionMap;
import utils.Colors;
import utils.Config;
import dataset.file.DataFactory;




public class KMLHeatMap {
	
	public static String drawHeatMap(String name, Map<String,Double> map, double max) {
		RegionMap nm = DataFactory.getNetworkMapFactory().getNetworkMap(Config.getInstance().pls_start_time);
		StringBuffer result = new StringBuffer();
		for(String celllac: map.keySet()) {
			RegionI nc = nm.getRegion(celllac);
			int index = (int)(map.get(celllac) / max * (Colors.HEAT_COLORS.length-1));
			
			result.append(nc.toKml(Colors.rgb2kmlstring(Colors.HEAT_COLORS[index]), ""));
		}
		return result.toString();
	}
	
	
	public static void drawHeatMap(String file, Map<String,Double> den, RegionMap rm , String desc, boolean logscale) throws Exception {
		
		Map<String,Double> density = new HashMap<String,Double>();
		for(String r: den.keySet())
			density.put(r.toLowerCase(), den.get(r).doubleValue());
		
		// convert to the log scale,
		if(logscale) 
			for(String name: density.keySet()) 
				density.put(name, Math.max(0, Math.log10(density.get(name))));	
		
		
		
		//compute the maximum value in density
		double max = 0;
		for(double v: density.values()) 
			max = Math.max(max, v);
		
		
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		KML kml = new KML();
		kml.printHeaderFolder(out, rm.getName());
		
		for(RegionI r: rm.getRegions()) {
			double val = density.get(r.getName().toLowerCase())==null? 0 : density.get(r.getName().toLowerCase());
			//System.out.println(val);
			
			if(val > 0) {
				r.setDescription(desc+" DENSITY = "+(logscale ? Math.pow(10, val) : val));
				out.println(r.toKml(Colors.val01_to_color(val/max),"44aaaaaa"));
			}
		}
	
		
		kml.printFooterFolder(out);
		out.close();
	}
	
	
}
