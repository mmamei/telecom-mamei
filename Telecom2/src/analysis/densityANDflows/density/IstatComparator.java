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
import analysis.Constraints;

public class IstatComparator {
	
	
	public static boolean LOG = true;
	public static boolean INTERCEPT = true;
	
	public static void main(String[] args) throws Exception {
		/*
		String regionMap = "FIX_Piemonte.ser";
		String kind_of_place = "HOME";
		String exclude_kind_of_place = "";
		String dir ="file_pls_piem_users_200_10000";
		*/
		
		String regionMap = "FIX_Lombardia.ser";
		String kind_of_place = "HOME";
		String exclude_kind_of_place = "";
		String dir ="file_pls_lomb_users_200_10000";
		
		
		RegionMap rm = (RegionMap)(RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/"+regionMap));
		Map<String,UserPlaces> up = UserPlaces.readUserPlaces(Config.getInstance().base_folder+"/PlaceRecognizer/"+dir+"/results.csv");
		
		PopulationDensityPlaces pdp = new PopulationDensityPlaces();
		Map<String,Double> density = pdp.computeSpaceDensity(rm,up,kind_of_place,exclude_kind_of_place,new Constraints(""));
		
		String region = regionMap.substring("FIX_".length(),regionMap.indexOf("."));
		Map<String,Integer> istat = ParserDatiISTAT.parse("G:/DATASET/ISTAT/DatiDemografici/"+region);
		compareWithISTAT(region,density,istat);
	}
	
	
	public static int THRESHOLD = 10;
	public static void compareWithISTAT(String title, Map<String,Double> density, Map<String,Integer> istat) throws Exception {
				
		int size = 0;
		for(String r: density.keySet()) {
			int estimated = density.get(r).intValue();
			Integer groundtruth = istat.get(r);
			if(groundtruth != null && estimated>0) {
				size++;
				System.out.println(r+","+estimated+","+groundtruth);
			}
		}
		
	
		File d = new File(Config.getInstance().base_folder+"/IstatComparator");
		if(!d.exists()) d.mkdirs();
		
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(d+"/"+title+"_hist.csv")));
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
		
		new GraphScatterPlotter("Result: "+title,"Estimated (log10)","GroundTruth (log10)",ldata,labels);
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
