package analysis.tourist;

import java.io.BufferedReader;
import java.io.File;
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

import pls_parser.UserEventCounterCellacXHour;
import utils.CopyAndSerializationUtils;
import utils.FileUtils;
import utils.Logger;
import visual.html.HeatMapGoogleMaps;
import visual.java.GraphPlotter;
import visual.kml.KMLHeatMap;
import area.region.Region;
import area.region.RegionMap;

public class TouristActivity {
	
	public static final int TIM = 0;
	public static final int ROAMING = 1;
	public static final int ALL = 2;
	public static final String[] U_SEGMENT = new String[]{"TIM","ROAMING","ALL"};
	
	public static final int MIN_DAYS = 1;
	public static final int MAX_DAYS = 3;
	
	public static final int U_SEG = ROAMING;
	
	public static void main(String[] args) throws Exception {
		process("Venezia");
		Logger.logln("Done!");
	}
		
	
	public static void process(String city) throws Exception {
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(FileUtils.getFile("RegionMap/"+city+".ser"));
		
		Map<String,Double> space_density = computeSpaceDensity(rm);
		plotSpaceDensity(city+"_"+MIN_DAYS+"_"+MAX_DAYS+"_"+U_SEGMENT[U_SEG], space_density, rm,0);
		
		Map<String,Double> time_density = computeTimeDensity(rm);
		plotTimeDensity(city,time_density);
	}
	
	public static void plotSpaceDensity(String city, Map<String,Double> space_density, RegionMap rm, double threshold) throws Exception {
		File d = FileUtils.getFile("TouristActivity");
		if(d == null) d = FileUtils.create("TouristActivity");
		KMLHeatMap.drawHeatMap(d.getAbsolutePath()+"/"+city+".kml",space_density,rm,city,false);
		HeatMapGoogleMaps.draw(d.getAbsolutePath()+"/"+city+".html", city, space_density, rm, threshold);
	}
	
	public static void plotTimeDensity(String city, Map<String,Double> time_density) {
		 
		DescriptiveStatistics[] hstats = new DescriptiveStatistics[24];
		for(int i=0; i<hstats.length;i++)
			hstats[i] = new DescriptiveStatistics();
		
		String[] domain = new String[24];
		for(int i=0; i<domain.length;i++)
			domain[i] = ""+i;
		double[] val = new double[24];
		
		for(String k: time_density.keySet()) {
			int h = Integer.parseInt(k.split(":")[2]);
			double x = time_density.get(k);
			hstats[h].addValue(x);
			val[h] += x;
		}
		
		GraphPlotter gp = GraphPlotter.drawGraph(""+city+"_"+MIN_DAYS+"_"+MAX_DAYS+"_"+U_SEGMENT[U_SEG], ""+city+"_"+MIN_DAYS+"_"+MAX_DAYS+"_"+U_SEGMENT[U_SEG], "", "hour", "num pls", domain, val);
		gp.save(FileUtils.getFileS("TouristActivity")+"/"+city+"_"+MIN_DAYS+"_"+MAX_DAYS+"_"+U_SEGMENT[U_SEG]+"_day.png");
	
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
			int hour = Integer.parseInt(x[2]);
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
		
		Logger.logln("EXTREME DAYS:");
		
		for(int i=0; i<temp.size();i++){
			Calendar cal = ((Calendar)temp.get(i)[0]);
			domain[i] = cal.getTime().toString();
			int h = cal.get(Calendar.HOUR_OF_DAY);
			val[i] = ((Double)temp.get(i)[1]);
			val[i] = (val[i] - hmeans[h]) / hsigmas[h];
			if(val[i] < 0) val[i] = 0;
			
			if(val[i] > 3) Logger.logln(domain[i]);
		}
		
		
		gp = GraphPlotter.drawGraph(""+city+"_"+MIN_DAYS+"_"+MAX_DAYS+"_"+U_SEGMENT[U_SEG], ""+city+"_"+MIN_DAYS+"_"+MAX_DAYS+"_"+U_SEGMENT[U_SEG], "", "time", "z_num pls", domain, val);			
		gp.save(FileUtils.getFileS("TouristActivity")+"/"+city+"_"+MIN_DAYS+"_"+MAX_DAYS+"_"+U_SEGMENT[U_SEG]+"_z_day.png");
	}
	
	
	public static Map<String,Double> computeSpaceDensity(RegionMap rm) throws Exception {
		String city = rm.getName();
		BufferedReader br = FileUtils.getBR("TouristData/"+city+".csv");
		if(br == null) {
			TouristData.process(city,null);
			br = FileUtils.getBR("TouristData/"+city+".csv");
		}
		
		Map<String,Double> sd = new HashMap<String,Double>();
		for(Region r: rm.getRegions())
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
	
	
	
	public static Map<String,Double> computeTimeDensity(RegionMap rm) throws Exception {
		String city = rm.getName();
		BufferedReader br = FileUtils.getBR("UserEventCounter/"+city+"_cellacXhour.csv");
		if(br == null) {
			Logger.logln("Launch UserEventCounterCellacXHour first!");
			System.exit(0);
		}
		
		
		Map<String,Double> td = new TreeMap<String,Double>();
		String line;
		while((line=br.readLine())!=null) {
			// 1b44888ff4f,22201,3,1,2013-5-23:Sun:13:4018542484,2013-5-23:Sun:17:4018542495,2013-5-23:Sun:13:4018542391,...
			String[] p = line.split(",");
			if(skip(p[1],Integer.parseInt(p[2]),Integer.parseInt(p[3]))) continue;
			for(int i=4;i<p.length;i++) {
				// 2013-5-23:Sun:13:4018542484
				String key = p[i].substring(0,p[i].lastIndexOf(":"));
				Double n = td.get(key);
				if(n == null) n = 0.0;
				td.put(key, n+1);
			}
		}
		br.close();
		return td;
	}
	
	public static boolean skip(String mnt, int num_pls, int num_days) {
		if(U_SEG == TIM && !mnt.equals("22201")) return true;
		if(U_SEG == ROAMING && mnt.equals("22201")) return true;
		if(num_days < MIN_DAYS || num_days > MAX_DAYS) return true;
		return false;
	}
	
}
