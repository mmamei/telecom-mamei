package analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
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
import visual.GraphPlotter;
import area.Placemark;

public class PLSBehaviorInAnAreaByCell {
	
	static final String[] MONTHS = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	static final String[] DAYS = new String[]{"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
	
	public static void main(String[] args) throws Exception { 
		//Placemark p = Placemark.getPlacemark("Juventus Stadium (TO)");
		//Placemark p = Placemark.getPlacemark("Stadio Olimpico (TO)");
		Placemark p = Placemark.getPlacemark("Stadio Silvio Piola (NO)");
		p.changeRadius(-300);
		process(p);
		
	}
	
	public static void process(Placemark p) throws Exception {
		String file = Config.getInstance().base_dir+"/PLSEventsAroundAPlacemark/"+p.name+"_"+p.radius+".txt";
		File f = new File(file);
		if(!f.exists()) {
			Logger.logln(file+" does not exist");
			Logger.logln("Executing PLSEventsAroundAPlacemark.process()");
			PLSEventsAroundAPlacemark.process(p);
		}
		
		Map<String,PLSMap> cell_plsmap = getPLSMap(file);
		
		for(String cell: cell_plsmap.keySet()) {
			
			PLSMap plsmap = cell_plsmap.get(cell);
			
			int size = plsmap.getHours();
			String[] domain = new String[size];
			double[] pls_data = new double[size];
			double[] usr_data = new double[size];
			DescriptiveStatistics pls_stats = new DescriptiveStatistics();
			DescriptiveStatistics usr_stats = new DescriptiveStatistics();
			
			Calendar cal = (Calendar)plsmap.startTime.clone();
			int i = 0;
			while(!cal.after(plsmap.endTime)) {
				String key = getKey(cal);
				Integer pls_count = plsmap.pls_counter.get(key);
				pls_data[i] = pls_count == null ? 0 : (double)pls_count;
				usr_data[i] = plsmap.usr_counter.get(key) == null ? 0 : plsmap.usr_counter.get(key).size();
				domain[i] = getLabel(cal);
				pls_stats.addValue(pls_data[i]);
				usr_stats.addValue(usr_data[i]);
				
				cal.add(Calendar.HOUR, 1);
				i++;
			}
				
			// compute z-score
			double[] z_pls_data = getZ(pls_stats);
			double[] z_usr_data =  getZ(usr_stats);
			
			drawGraph(p.name+"_"+p.radius+" Cell = "+cell,domain,null,null,z_pls_data,z_usr_data);
		
		}
		Logger.logln("Done!");
	}
	
	public static void drawGraph(String title, String[] domain, double[] pls_data,double[] usr_data,double[] z_pls_data,double[] z_usr_data) {
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
	
		GraphPlotter.drawMultiGraph(nrows, ncols, "events around "+title, titles, "hour", "n.", labels, domains, data);
	}
	
	public static double[] getZ(DescriptiveStatistics stat) {
		double mean = stat.getMean();
		double sigma = stat.getStandardDeviation();
		double[] z = stat.getValues();
		for(int i=0; i<z.length;i++)
			z[i] = (z[i] - mean)/sigma;
		return z;
	}

	
	
	public static Map<String,PLSMap> getPLSMap(String file) throws Exception {
		
		
		Map<String,PLSMap> cell_plsmap = new TreeMap<String,PLSMap>();
		
		
		Map<String,Set<String>> usr_counter = new TreeMap<String,Set<String>>();
		Map<String,Integer> pls_counter = new TreeMap<String,Integer>();
		
		String[] splitted;
		String line;
		//PlsEvent e;
		Calendar startTime = null;
		Calendar endTime = null;
		Calendar cal = new GregorianCalendar();
		BufferedReader in = new BufferedReader(new FileReader(file));
		while((line = in.readLine()) != null){
			line = line.trim();
			if(line.length() < 1) continue; // extra line at the end of file
			splitted = line.split(",");
			if(splitted.length == 5 && !splitted[3].equals("null")) {
				cal.setTimeInMillis(Long.parseLong(splitted[1]));
				
				if(startTime == null || startTime.after(cal)) startTime = (Calendar)cal.clone();
				if(endTime == null || endTime.before(cal)) endTime = (Calendar)cal.clone();
				
				String key = getKey(cal);
				String username = splitted[0];
				Set<String> users = usr_counter.get(key);
				if(users == null) users = new TreeSet<String>();
				users.add(username);
				usr_counter.put(key, users);
				
				Integer count = pls_counter.get(key);
				pls_counter.put(key, count == null ? 0 : count+1);	
			}
		}
		in.close();
		PLSMap plsmap = new PLSMap(usr_counter,pls_counter,startTime,endTime);
		cell_plsmap.put("all", plsmap);
		
		return cell_plsmap;
	}
	
	public static String getKey(Calendar cal) {
		return cal.get(Calendar.DAY_OF_MONTH)+"-"+
			 	MONTHS[cal.get(Calendar.MONTH)]+"-"+
			 	cal.get(Calendar.YEAR)+":"+
			 	cal.get(Calendar.HOUR_OF_DAY);
	}
	
	public static String getLabel(Calendar cal) {
		//return "-"+cal.get(Calendar.DAY_OF_MONTH)+":"+DAYS[cal.get(Calendar.DAY_OF_WEEK)-1]+"-";
		return "["+cal.get(Calendar.DAY_OF_MONTH)+"-"+DAYS[cal.get(Calendar.DAY_OF_WEEK)-1]+":"+cal.get(Calendar.HOUR_OF_DAY)+"]";
	}
}