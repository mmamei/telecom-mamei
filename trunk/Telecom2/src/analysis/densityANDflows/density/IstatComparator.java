package analysis.densityANDflows.density;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import region.ParserDatiISTAT;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;
import visual.java.GraphScatterPlotter;

public class IstatComparator {
	
	
	public static boolean LOG = false;
	public static boolean INTERCEPT = true;
	
	public static void main(String[] args) throws Exception {
		
		String region = "Piemonte";
		String kind_of_place = "HOME";
		String exclude_kind_of_place = "";
		
		File input_obj_file = new File("G:/BASE/cache/"+region+".ser");
		if(!input_obj_file.exists()) {
			System.out.println(input_obj_file+" does not exist... run the region parser first!");
			System.exit(0);
		}
		
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(input_obj_file); 
		Map<String,UserPlaces> up = UserPlaces.readUserPlaces("G:/BASE/PlaceRecognizer/file_pls_piem_users_above_2000/results.csv");
		
		
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
		
		
		String dir = "BASE/PopulationDensity";
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
				if(estimated > 10){
					result[i][0] = LOG? Math.log10(estimated) : estimated;
					result[i][1] = LOG? Math.log10(groundtruth): groundtruth;
					i++;
				}
				
				
			}
		}
		
		out.close();
		
		SimpleRegression sr = new SimpleRegression(INTERCEPT);
		sr.addData(result);
		printInfo(sr);
		
		List<double[][]> ldata = new ArrayList<double[][]>();
		ldata.add(result);
		List<String> labels = new ArrayList<String>();
		labels.add("population density");
		
		new GraphScatterPlotter("Result: "+region,"Estimated (log10)","GroundTruth (log10)",ldata,labels);
	}
	
	public static void printInfo(SimpleRegression sr) {
		Logger.logln("r="+sr.getR()+", r^2="+sr.getRSquare()+", sse="+sr.getSumSquaredErrors());
		
		double s = sr.getSlope();
		double sconf = sr.getSlopeConfidenceInterval(); 
		
		double i = sr.getIntercept();
		double iconf = sr.getInterceptStdErr();
		
		Logger.logln("Y = "+s+" * X + "+i);
		Logger.logln("SLOPE CONF INTERVAL =  ["+(s-sconf)+","+(s+sconf)+"]");
		Logger.logln("INTERCEPT CONNF INTERVAL =  ["+(i-iconf)+","+(i+iconf)+"]");
		
	}
}
