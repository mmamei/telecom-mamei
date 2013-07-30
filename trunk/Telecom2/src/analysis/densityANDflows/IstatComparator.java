package analysis.densityANDflows;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.stat.regression.SimpleRegression;

import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;
import visual.GraphScatterPlotter;
import area.region.ParserDatiISTAT;
import area.region.RegionMap;

public class IstatComparator {
	
	
	public static void main(String[] args) throws Exception {
		
		String region = "TorinoGrid20";
		String kind_of_place = "HOME";
		String exclude_kind_of_place = "";
		
		File input_obj_file = new File(Config.getInstance().base_dir+"/cache/"+region+".ser");
		if(!input_obj_file.exists()) {
			System.out.println(input_obj_file+" does not exist... run the region parser first!");
			System.exit(0);
		}
		
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(input_obj_file); 
		Map<String,UserPlaces> up = UserPlaces.readUserPlaces(Config.getInstance().base_dir+"/PlaceRecognizer/file_pls_piem_users_above_2000/results.csv");
		
		
		Map<String,Double> density = PopulationDensity.process(rm,up,kind_of_place,exclude_kind_of_place);
		
		compareWithISTAT(density,region);
	}
	
	
	
	public static void compareWithISTAT(Map<String,Double> density, String region) throws Exception {
		Map<String,Integer> istat = ParserDatiISTAT.load(region);
		int size = 0;
		for(String r: density.keySet()) {
			int estimated = density.get(r).intValue();
			Integer groundtruth = istat.get(r);
			if(groundtruth != null && estimated>10) {
				size++;
				//System.out.println(r+","+estimated+","+groundtruth);
			}
		}
		
		
		String dir = Config.getInstance().base_dir+"/PopulationDensity";
		File d = new File(dir);
		if(!d.exists()) d.mkdirs();
		
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(dir+"/"+region+"_hist.csv")));
		out.println("estimated;groundtruth");
		
		
		double[][] result = new double[size][2];
		int i = 0;
		for(String r: density.keySet()) {
			int estimated = density.get(r).intValue();
			Integer groundtruth = istat.get(r);
			if(groundtruth != null && estimated>0 && groundtruth>0) {
				out.println(estimated+";"+groundtruth);
				
				if(estimated>10){
					result[i][0] = Math.log10(estimated);
					result[i][1] = Math.log10(groundtruth);
					i++;
				}
				
				
			}
		}
		
		out.close();
		
		SimpleRegression sr = new SimpleRegression();
		sr.addData(result);
		Logger.logln("r="+sr.getR()+", r^2="+sr.getRSquare()+", sse="+sr.getSumSquaredErrors());
		
		List<double[][]> ldata = new ArrayList<double[][]>();
		ldata.add(result);
		List<String> labels = new ArrayList<String>();
		labels.add("population density");
		
		new GraphScatterPlotter("Result: "+region,"Estimated (log10)","GroundTruth (log10)",ldata,labels);
	}
}
