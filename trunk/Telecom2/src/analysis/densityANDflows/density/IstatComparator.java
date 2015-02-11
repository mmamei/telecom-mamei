package analysis.densityANDflows.density;

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

import org.apache.commons.math3.stat.regression.SimpleRegression;

import region.ParserDatiISTAT;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;
import visual.java.GraphScatterPlotter;
import analysis.Constraints;

public class IstatComparator {
	
	
	public static boolean LOG = false;
	public static boolean INTERCEPT = true;
	
	public static void main(String[] args) throws Exception {
		
		/*
		String regionMap = "FIX_Piemonte.ser";
		String kind_of_place = "HOME";
		String exclude_kind_of_place = "";
		String places_file =Config.getInstance().base_folder+"/PlaceRecognizer/file_pls_piem_users_200_10000/results_piem.csv";
		String census_file = Config.getInstance().base_folder+"/CENSUS/Piemonte.csv";
		String title = "Piemonte";
		*/
		
		/*
		String regionMap = "torino_circoscrizioni_geo.ser";
		String kind_of_place = "HOME";
		String exclude_kind_of_place = "";
		String places_file =Config.getInstance().base_folder+"/PlaceRecognizer/file_pls_piem_users_200_10000/results_piem.csv";
		String census_file = Config.getInstance().base_folder+"/CENSUS/torino_residenti.csv";
		String title = "Torino";
		*/
		
		/*
		String regionMap = "torino_circoscrizioni_geo.ser";
		String kind_of_place = "HOME";
		String exclude_kind_of_place = "";
		String places_file =Config.getInstance().base_folder+"/PlaceRecognizer/fast_home_torino.csv";
		String census_file = Config.getInstance().base_folder+"/CENSUS/torino_residenti.csv";
		String title = "Torino";
		*/
		
		String regionMap = "milano_circoscrizioni_geo.ser";
		String kind_of_place = "HOME";
		String exclude_kind_of_place = "";
		String places_file =Config.getInstance().base_folder+"/PlaceRecognizer/fast_home_milano.csv";
		String census_file = Config.getInstance().base_folder+"/CENSUS/milano_residenti.csv";
		String title = "Milano";
		
		
		/*
		String regionMap = "milano_circoscrizioni_geo.ser";
		String kind_of_place = "HOME";
		String exclude_kind_of_place = "";
		String places_file =Config.getInstance().base_folder+"/PlaceRecognizer/file_pls_lomb_users_200_10000/results_lomb.csv";
		String census_file = Config.getInstance().base_folder+"/CENSUS/milano_residenti.csv";
		String title = "Milano";
		*/
		
		RegionMap rm = (RegionMap)(RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/"+regionMap));
		Map<String,UserPlaces> up = UserPlaces.readUserPlaces(places_file);
		
		PopulationDensityPlaces pdp = new PopulationDensityPlaces();
		Map<String,Double> density = pdp.computeSpaceDensity(rm,up,kind_of_place,exclude_kind_of_place,new Constraints(""));
		
		
		// read census data
		
		Map<String,Integer> istat = new HashMap<String,Integer>();
		BufferedReader br = new BufferedReader(new FileReader(new File(census_file)));
		String line;
		while((line=br.readLine())!=null) {
			String[] e = line.split(",");
			istat.put(e[0].toLowerCase(), (int)Double.parseDouble(e[1]));
		}
		br.close();
		
		
		
		compareWithISTAT(title,density,istat);
	}
	
	
	public static int THRESHOLD = 1;
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
		
		String x = "Estimated"+(LOG?" (log10)":"");
		String y = "GroundTruth"+(LOG?" (log10)":"");
		new GraphScatterPlotter("Result: "+title,x,y,ldata,labels);
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
