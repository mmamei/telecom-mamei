package analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import pls_parser.PLSEventsAroundAPlacemark;

import utils.Config;
import utils.Logger;
import utils.StatsUtils;
import visual.GraphPlotter;
import area.CityEvent;
import area.Placemark;

public class PLSBehaviorInAnArea {
	
	static final String[] MONTHS = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	
	
	static String[] pnames = new String[]{"Juventus Stadium (TO)","Stadio Olimpico (TO)","Stadio Silvio Piola (NO)"};
	
	public static void main(String[] args) throws Exception { 
		for(String pn: pnames) {
			Placemark p = Placemark.getPlacemark(pn);
			p.changeRadius(500);
			process(p);
		}
		Logger.logln("Done!");
	}
	
	public static void process(Placemark p) throws Exception {
		
		Logger.logln("Processing... "+p.name);
		
		String file = Config.getInstance().base_dir+"/PLSEventsAroundAPlacemark/"+p.name+"_"+p.radius+".txt";
		File f = new File(file);
		if(!f.exists()) {
			Logger.logln(file+" does not exist");
			Logger.logln("Executing PLSEventsAroundAPlacemark.process()");
			PLSEventsAroundAPlacemark.process(p);
		}
		
		
		
		Map<String,PLSMap> cell_plsmap = getPLSMap(file,false);
		
		for(String cell: cell_plsmap.keySet()) {
			
			PLSMap plsmap = cell_plsmap.get(cell);
			
			DescriptiveStatistics[] stats = getStats(plsmap);
				
			// compute data
			//double[] pls_data = stats[0].getValues();
			//double[] usr_data = stats[1].getValues();
			double[] z_pls_data = getZ(stats[0]);
			double[] z_usr_data =  getZ(stats[1]);
			
			StatsUtils.checkNormalDistrib(z_pls_data,true,p.name+" hourly z");
			StatsUtils.checkNormalDistrib(getZ3(stats[0]),true,p.name+" val z");
			
			
			//drawGraph(p.name+"_"+p.radius+" Cell = "+cell,plsmap.getDomain(),null,null,z_pls_data,z_usr_data);
		
		}
	}
	
	
	public static DescriptiveStatistics[] getStats(PLSMap plsmap) {
		DescriptiveStatistics pls_stats = new DescriptiveStatistics();
		DescriptiveStatistics usr_stats = new DescriptiveStatistics();
		
		Calendar cal = (Calendar)plsmap.startTime.clone();
		while(!cal.after(plsmap.endTime)) {
			String key = getKey(cal);
			Integer pls_count = plsmap.pls_counter.get(key);
			double pls = pls_count == null ? 0 : (double)pls_count;
			double usr = plsmap.usr_counter.get(key) == null ? 0 : plsmap.usr_counter.get(key).size();	
			pls_stats.addValue(pls);
			usr_stats.addValue(usr);
			cal.add(Calendar.HOUR, 1);
		}
		
		return new DescriptiveStatistics[]{pls_stats,usr_stats};
	}
	
	
	public static GraphPlotter[] drawGraph(String title, String[] domain, double[] pls_data,double[] usr_data,double[] z_pls_data,double[] z_usr_data) {
		List<String> labels = new ArrayList<String>();
		List<String> titles = new ArrayList<String>();
		List<String[]> domains = new ArrayList<String[]>();
		List<double[]> data = new ArrayList<double[]>();
		if(pls_data != null) {
			labels.add("pls");
			titles.add("N. PLS Events around "+title);
			domains.add(domain);
			data.add(pls_data);
		}
		if(z_pls_data != null) {
			labels.add("z-pls");
			titles.add("Z-Score PLS Events around "+title);
			domains.add(domain);
			data.add(z_pls_data);
		}
		if(usr_data != null) {
			labels.add("users");
			titles.add("N. Users around "+title);
			domains.add(domain);
			data.add(usr_data);
		}
		if(z_usr_data != null) {
			labels.add("z-users");
			titles.add("Z-Score Users around "+title);
			domains.add(domain);
			data.add(z_usr_data);
		}
		
		int nrows = 1;
		int ncols = 1;
		
		if(data.size()==2) {
			ncols = 2;
		}
		else if(data.size()==4) {
			nrows = 2;
			ncols = 2;
		}
	
		return GraphPlotter.drawMultiGraph(nrows, ncols, "events around "+title, titles, "hour", "n.", labels, domains, data);
	}
	
	
	
	public static double[] getZ(DescriptiveStatistics stat) {
		double[] z = stat.getValues();
		DescriptiveStatistics[] hourly_stats = new DescriptiveStatistics[24];
		for(int i=0; i<hourly_stats.length;i++) 
			hourly_stats[i] = new DescriptiveStatistics();
		
		for(int i=0; i<z.length;i++) 
			if(z[i]>0)
				hourly_stats[i%24].addValue(z[i]);
		
	
		double[] m = new double[24];
		double[] s = new double[24];
		for(int i=0; i<hourly_stats.length;i++) {
			m[i] = hourly_stats[i].getMean();
			s[i] = hourly_stats[i].getStandardDeviation();
		}
		
		for(int i=0; i<z.length;i++) {
			z[i] = (z[i] - m[i%24]) / s[i%24];
			if(z[i] < 0) z[i] = 0;
		}
		return z;
	}
	
	
	
	public static double[] getZ3(DescriptiveStatistics stat) {
		
		DescriptiveStatistics stat2 = new DescriptiveStatistics();
		double[] vals = stat.getValues();
		for(int i=0; i<vals.length;i++) {
			if(vals[i] > 0)
				stat2.addValue(vals[i]);
		}
		
		double mean = stat2.getMean();
		double sigma = stat2.getStandardDeviation();
		double[] z = stat.getValues();
		for(int i=0; i<z.length;i++) {
			z[i] = (z[i] - mean) / sigma;
			if(z[i] < 0) z[i] = 0;
		}
		return z;
	}

	
	public static Map<String,PLSMap> getPLSMap(String file, boolean group_by_cells) throws Exception {
		
		Map<String,PLSMap> cell_plsmap = new TreeMap<String,PLSMap>();
		String[] splitted;
		String line;
		
		Calendar cal = new GregorianCalendar();
		BufferedReader in = new BufferedReader(new FileReader(file));
		while((line = in.readLine()) != null){
			line = line.trim();
			if(line.length() < 1) continue; // extra line at the end of file
			splitted = line.split(",");
			if(splitted.length == 5 && !splitted[3].equals("null")) {
				
				
				String username = splitted[0];
				cal.setTimeInMillis(Long.parseLong(splitted[1]));
				String key = getKey(cal);
				String celllac = splitted[3]; 
				
				if(!group_by_cells) celllac = "all"; // if we do not want to extract pls by cells we just overwrite the key to use always the 'all' key
				
				PLSMap plsmap = cell_plsmap.get(celllac);
				if(plsmap==null) {
					plsmap = new PLSMap();
					cell_plsmap.put(celllac,plsmap);
				}
				
				if(plsmap.startTime == null || plsmap.startTime.after(cal)) plsmap.startTime = (Calendar)cal.clone();
				if(plsmap.endTime == null || plsmap.endTime.before(cal)) plsmap.endTime = (Calendar)cal.clone();
				Set<String> users = plsmap.usr_counter.get(key);
				if(users == null) users = new TreeSet<String>();
				users.add(username);
				plsmap.usr_counter.put(key, users);
				Integer count = plsmap.pls_counter.get(key);
				plsmap.pls_counter.put(key, count == null ? 0 : count+1);	
			}
		}
		in.close();
		return cell_plsmap;
	}
	
	public static String getKey(Calendar cal) {
		return cal.get(Calendar.DAY_OF_MONTH)+"-"+
			 	MONTHS[cal.get(Calendar.MONTH)]+"-"+
			 	cal.get(Calendar.YEAR)+":"+
			 	cal.get(Calendar.HOUR_OF_DAY);
	}
}