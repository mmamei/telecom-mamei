package analysis.tourist;

import java.util.Map;

import utils.CopyAndSerializationUtils;
import utils.FileUtils;
import utils.Logger;
import visual.java.GraphPlotter;
import analysis.densityANDflows.density.PopulationDensity;
import area.region.RegionMap;

public class TouristActivity {
	
	public static void main(String[] args) throws Exception {
		
		//String u_seg = "ROAMING";
		String u_seg = "TIM";
		
		
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(FileUtils.getFile("RegionMap/Venezia.ser"));
		Map<String,Double> space_density = (Map<String,Double>)CopyAndSerializationUtils.restore(FileUtils.getFile("TouristAnalyzer/Venezia_7_"+u_seg+"_space.ser"));
		Map<String,Double> time_density = (Map<String,Double>)CopyAndSerializationUtils.restore(FileUtils.getFile("TouristAnalyzer/Venezia_7_"+u_seg+"_time.ser"));
		
		for(String k : space_density.keySet()) {
			double val = space_density.get(k);
			double area = rm.getRegion(k).getGeom().getArea();
			space_density.put(k, val/area);
		}
		
		PopulationDensity.plot("Venezia_7_"+u_seg, space_density, rm);
		
		
		
		String[] domain = new String[24];
		for(int i=0; i<domain.length;i++)
			domain[i] = ""+i;
		double[] val = new double[24];
		
		for(String k: time_density.keySet()) {
			int h = Integer.parseInt(k.split(":")[1]);
			double x = time_density.get(k);
			System.out.println(h+","+x);
			val[h] += x;
		}
		GraphPlotter.drawGraph("Venezia_7_"+u_seg, "Venezia_7_"+u_seg, "", "hour", "num pls", domain, val);
		Logger.logln("Done!");
		
	}
	
}
