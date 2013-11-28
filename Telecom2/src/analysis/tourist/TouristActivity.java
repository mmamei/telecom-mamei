package analysis.tourist;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import pls_parser.PLSParser;
import pls_parser.TouristAnalyzer;
import utils.CopyAndSerializationUtils;
import utils.FileUtils;
import utils.Logger;
import visual.java.GraphPlotter;
import analysis.densityANDflows.density.PopulationDensity;
import area.Placemark;
import area.region.RegionMap;

public class TouristActivity {
	
	public static void main(String[] args) throws Exception {
		
		int min_days = 1;
		int max_days = 5;
		int u_seg =TouristAnalyzer.ROAMING;
		
		File space = FileUtils.getFile("TouristAnalyzer/Venezia_"+min_days+"_"+max_days+"_"+TouristAnalyzer.U_SEGMENT[u_seg]+"_space.ser");
		File time = FileUtils.getFile("TouristAnalyzer/Venezia_"+min_days+"_"+max_days+"_"+TouristAnalyzer.U_SEGMENT[u_seg]+"_time.ser");
		
		if(time == null || space == null) {
			TouristAnalyzer ba = new TouristAnalyzer("UserEventCounterDetailed/Venezia_trim3.csv","RegionMap/Venezia.ser",Placemark.getPlacemark("Venezia"),min_days, max_days,u_seg);
		    PLSParser.parse(ba);
		    ba.finish();
		    space = FileUtils.getFile("TouristAnalyzer/Venezia_"+min_days+"_"+max_days+"_"+TouristAnalyzer.U_SEGMENT[u_seg]+"_space.ser");
			time = FileUtils.getFile("TouristAnalyzer/Venezia_"+min_days+"_"+max_days+"_"+TouristAnalyzer.U_SEGMENT[u_seg]+"_time.ser");
		}
		
		Map<String,Double> space_density = (Map<String,Double>)CopyAndSerializationUtils.restore(space);
		Map<String,Double> time_density = (Map<String,Double>)CopyAndSerializationUtils.restore(time);
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(FileUtils.getFile("RegionMap/Venezia.ser"));
		
		for(String k : space_density.keySet()) {
			double val = space_density.get(k);
			double area = rm.getRegion(k).getGeom().getArea();
			space_density.put(k, val/area);
		}
		
		PopulationDensity.plot("Venezia_"+min_days+"_"+max_days+"_"+TouristAnalyzer.U_SEGMENT[u_seg], space_density, rm,0);
		
		

		DescriptiveStatistics[] hstats = new DescriptiveStatistics[24];
		for(int i=0; i<hstats.length;i++)
			hstats[i] = new DescriptiveStatistics();
		
		String[] domain = new String[24];
		for(int i=0; i<domain.length;i++)
			domain[i] = ""+i;
		double[] val = new double[24];
		
		for(String k: time_density.keySet()) {
			int h = Integer.parseInt(k.split(":")[1]);
			double x = time_density.get(k);
			hstats[h].addValue(x);
			System.out.println(h+","+x);
			val[h] += x;
		}
		GraphPlotter.drawGraph("Venezia_"+min_days+"_"+max_days+"_"+TouristAnalyzer.U_SEGMENT[u_seg], "Venezia_"+min_days+"_"+max_days+"_"+TouristAnalyzer.U_SEGMENT[u_seg], "", "hour", "num pls", domain, val);
		
		double[] hmeans = new double[24];
		double[] hsigmas = new double[24];
		
		for(int i=0; i<hstats.length;i++) {
			hmeans[i] = hstats[i].getMean();
			hsigmas[i] = hstats[i].getStandardDeviation();
		}
		
		
		
		
		List<Object[]> temp = new ArrayList<Object[]>();
		for(String k: time_density.keySet()) {
			String[] x = k.split(":");
			String[] y = x[0].split("-");
			int year = Integer.parseInt(y[0]);
			int month = Integer.parseInt(y[1]);
			int day = Integer.parseInt(y[2]);
			int hour = Integer.parseInt(x[1]);
			Calendar cal = new GregorianCalendar();
			cal.set(year, month, day, hour, 0, 0);
			temp.add(new Object[]{cal,time_density.get(k)});
		}
		Collections.sort(temp,new Comparator<Object[]>(){
			public int compare(Object[] o1, Object[] o2) {
				Calendar cal1 = (Calendar)o1[0];
				Calendar cal2 = (Calendar)o2[0];
				if(cal1.before(cal2)) return -1;
				if(cal1.after(cal2)) return 1;
				return 0;
			}	
		});
		
		
		
		domain = new String[temp.size()];
		val = new double[temp.size()];
		for(int i=0; i<temp.size();i++){
			Calendar cal = ((Calendar)temp.get(i)[0]);
			domain[i] = cal.getTime().toString();
			int h = cal.get(Calendar.HOUR_OF_DAY);
			val[i] = ((Double)temp.get(i)[1]);
			val[i] = (val[i] - hmeans[h]) / hsigmas[h];
			if(val[i] < 0) val[i] = 0;
			
			if(val[i] > 3) System.out.println(domain[i]);
		}
		
		
		
		
		
		
		GraphPlotter.drawGraph("Venezia_"+min_days+"_"+max_days+"_"+TouristAnalyzer.U_SEGMENT[u_seg], "Venezia_"+min_days+"_"+max_days+"_"+TouristAnalyzer.U_SEGMENT[u_seg], "", "time", "z_num pls", domain, val);	
		
		Logger.logln("Done!");
		
	}
	
}
