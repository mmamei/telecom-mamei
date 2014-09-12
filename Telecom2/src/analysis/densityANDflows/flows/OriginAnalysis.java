package analysis.densityANDflows.flows;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import region.CityEvent;
import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;
import utils.Sort;
import visual.kml.KMLHeatMap;
import analysis.PLSEvent;
import dataset.file.DataFactory;

/*
 * This class analyzes data produced by the dataset.file.UsersAroundMultipleEvents class
 * Given an event (e.g., Melpignano concert) the UsersAroundMultipleEvents finds users that were at the concert and were also at given origin locations (e.g., train stations, airport, etc.) 
 * some days before.
 * This class analyzes that data and try to estimate histograms on where  the people come from
 */

public class OriginAnalysis {
	

	
	public static void main(String[] args) throws Exception {
		
		CityEvent target_event = CityEvent.getEvent("Melpignano,22/08/2014");	
		//CityEvent target_event = CityEvent.getEvent("Lecce,14/08/2014");	
		//CityEvent target_event = CityEvent.getEvent("Lecce,24/08/2014");	
		
		
		DescriptiveStatistics first_pls = new DescriptiveStatistics();
		DescriptiveStatistics last_pls = new DescriptiveStatistics();
		File dir = new File(Config.getInstance().base_folder+"/UsersCSVCreator/"+target_event.toFileName()+"_STR");
		
		
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/FIX_Puglia.ser"));

		Map<String,Double> densityFirst = new HashMap<String,Double>();
		Map<String,Double> densityLast = new HashMap<String,Double>();
		for(RegionI r: rm.getRegions()) {
			densityFirst.put(r.getName(), 0.0);
			densityLast.put(r.getName(), 0.0);
		}
		
		int tot = dir.listFiles().length;
		System.out.println("TOT USERS = "+tot);
		
		for(File file: dir.listFiles()) {
			//System.out.println("Processing "+file.getName());
			List<PLSEvent> list = PLSEvent.readEvents(file);
			PLSEvent f = list.get(0);
			PLSEvent l = list.get(list.size()-1);
			long ftime = f.getTimeStamp();
			long ltime = l.getTimeStamp();
			
			first_pls.addValue(ftime);
			last_pls.addValue(ltime);
			
			try {
			double[] flatlon = DataFactory.getNetworkMapFactory().getNetworkMap(ftime).getRegion(f.getCellac()).getLatLon();
			double[] llatlon = DataFactory.getNetworkMapFactory().getNetworkMap(ltime).getRegion(l.getCellac()).getLatLon();
			
			RegionI fregion = rm.get(flatlon[1], flatlon[0]);
			RegionI lregion = rm.get(llatlon[1], llatlon[0]);
			
			
			if(fregion!=null) densityFirst.put(fregion.getName(), densityFirst.get(fregion.getName())+1);
			if(lregion!=null) densityLast.put(lregion.getName(), densityLast.get(lregion.getName())+1);
			}catch(Exception e){System.out.println(".");}
		}
		
		// percent
		for(String r: densityFirst.keySet()) {
			densityFirst.put(r, 100*densityLast.get(r)/tot);
			densityLast.put(r, 100*densityLast.get(r)/tot);
		}
		
		
		KMLHeatMap.drawHeatMap(Config.getInstance().base_folder+"/Tourist/"+target_event.toString()+"_STR_first.kml", densityFirst, rm, "First PLS", true);
		KMLHeatMap.drawHeatMap(Config.getInstance().base_folder+"/Tourist/"+target_event.toString()+"_STR_last.kml", densityLast, rm, "Last PLS", true);
		
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis((long)first_pls.getMean());
		System.out.println("Median First PLS "+cal.getTime());
		cal.setTimeInMillis((long)last_pls.getMean());
		System.out.println("Median Last PLS "+cal.getTime());
		
		System.out.println("********** PERCENTILE ANALYSIS FIRST PLS");
		
		
		for(int i=10; i<=100;i=i+10) {
			cal.setTimeInMillis((long)first_pls.getPercentile(i));
			System.out.println(i+"th percentile,"+cal.get(Calendar.DAY_OF_MONTH));
		}
			
		
		Logger.logln("Done!");
	}
	
	public static void main2(String[] args) throws Exception {
		
		
		//CityEvent target_event = CityEvent.getEvent("Melpignano,22/08/2014");	
		//CityEvent target_event = CityEvent.getEvent("Lecce,14/08/2014");	
		CityEvent target_event = CityEvent.getEvent("Lecce,24/08/2014");	
		Map<String, Integer> hist = getHist(Config.getInstance().base_folder+"/UsersAroundAnEvent/"+target_event.toFileName(),null);
		PrintWriter out = new PrintWriter(System.out);
		out.println("ITA,STR");
		printHist(out,hist,false);
		
		String dir = Config.getInstance().base_folder+"/UsersAroundAnEvent/"+target_event;
		File d = new File(dir);
		
		Set<String> alreayconsidered = new HashSet<String>(); // to avoid double count of people from multiple origins
		out.println("Place,Day,ITA,STR");
		for(File f: d.listFiles()) {
			String s = f.getName();
			String place = s.substring(0,s.indexOf("-"));
			String day = s.substring(s.indexOf("-")+1,s.indexOf("_", s.indexOf("-")));
			out.print(place+","+day+",");
			hist = getHist(f.getAbsolutePath(),alreayconsidered);
			printHist(out,hist,false);
		}
	}
	
	
	
	private static Map<String, Integer> getHist(String file,Set<String> alreayconsidered) throws Exception {
		Map<String, Integer> hist = new HashMap<String,Integer>();
		
		BufferedReader br = new BufferedReader(new FileReader(new File(file)));
		String line;
		while((line = br.readLine()) != null) {
			String[] elements = line.split(",");
			
			String usr = elements[0];
			String mnt = elements[1];
			
			if(alreayconsidered!=null && alreayconsidered.contains(usr)) continue;
			if(alreayconsidered!=null) alreayconsidered.add(usr);
			
			
			Integer c = hist.get(mnt);
			if(c == null) c = 0;
			hist.put(mnt, c+1);
			
			String aggregated = mnt.startsWith("222") ? "ITA" : "STR";
			c = hist.get(aggregated);
			if(c == null) c = 0;
			hist.put(aggregated, c+1);
		} 
		br.close();
		return Sort.sortHashMapByValuesD(hist, Collections.reverseOrder());
	} 
	
	private static void printHist(PrintWriter out, Map<String, Integer> hist, boolean verbose) {
		if(verbose) {
			for(String k: hist.keySet()) 
				out.println(k+","+hist.get(k));
		}
		else {
			Integer ita = hist.get("ITA");
			if(ita == null) ita = 0;
			Integer str = hist.get("STR");
			if(str == null) str = 0;
			out.println(ita+","+str);
		}
		out.flush();
	}	
}
