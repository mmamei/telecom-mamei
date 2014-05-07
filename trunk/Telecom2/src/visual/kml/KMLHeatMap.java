package visual.kml;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import region.Region;
import region.RegionMap;
import region.network.NetworkCell;
import region.network.NetworkMap;
import region.network.NetworkMapFactory;
import utils.Colors;
import utils.Config;




public class KMLHeatMap {
	
	public static String drawHeatMap(String name, Map<Long,Double> map, double max) {
		NetworkMap nm = NetworkMapFactory.getNetworkMap(Config.getInstance().pls_start_time);
		StringBuffer result = new StringBuffer();
		for(long celllac: map.keySet()) {
			NetworkCell nc = nm.get(celllac);
			int index = (int)(map.get(celllac) / max * (Colors.HEAT_COLORS.length-1));
			
			result.append(nc.toKml(Colors.rgb2kmlstring(Colors.HEAT_COLORS[index]), "", ""));
		}
		return result.toString();
	}
	
	
	public static void drawHeatMap(String file, Map<String,Double> den, RegionMap rm , String desc, boolean logscale) throws Exception {
		
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
		
		
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		KML kml = new KML();
		kml.printHeaderFolder(out, rm.getName());
		
		for(Region r: rm.getRegions()) {
			double val = density.get(r.getName())==null? 0 : density.get(r.getName());
			String description = desc+" DENSITY = "+(logscale ? Math.pow(10, val) : val);
			out.println(r.toKml(Colors.val01_to_color(val/max),"44aaaaaa",description));
		}
	
		
		kml.printFooterFolder(out);
		out.close();
	}
	
	
}
