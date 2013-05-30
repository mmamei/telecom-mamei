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

public class PLSBehaviorInAnArea {
	
	static final String[] MONTHS = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	static final String[] DAYS = new String[]{"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
	
	public static void main(String[] args) throws Exception { 
		//Placemark p = Placemark.getPlacemark("Juventus Stadium (TO)");
		//Placemark p = Placemark.getPlacemark("Stadio Olimpico (TO)");
		Placemark p = Placemark.getPlacemark("Stadio Silvio Piola (NO)");
		//p.changeRadius(-300);
		process(p,true);
		
	}
	
	public static void process(Placemark p, boolean process_users) throws Exception {
		String file = Config.getInstance().base_dir+"/PLSEventsAroundAPlacemark/"+p.name+"_"+p.radius+".txt";
		File f = new File(file);
		if(!f.exists()) {
			Logger.logln(file+" does not exist");
			Logger.logln("Executing PLSEventsAroundAPlacemark.process()");
			PLSEventsAroundAPlacemark.process(p);
		}
		
		
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
				if(process_users) {
					String username = splitted[0];
					Set<String> users = usr_counter.get(key);
					if(users == null) users = new TreeSet<String>();
					users.add(username);
					usr_counter.put(key, users);
				}
				Integer count = pls_counter.get(key);
				pls_counter.put(key, count == null ? 0 : count+1);	
			}
		}
		in.close();
		
		Logger.logln("map date --> (events,user) created!");
		Logger.logln("start time = "+startTime.getTime());
		Logger.logln("end time = "+endTime.getTime());
		
		List<Double> usr_vals = new ArrayList<Double>();
		List<Double> pls_vals = new ArrayList<Double>();
		
		cal = (Calendar)startTime.clone();
		
		while(!cal.after(endTime)) {
			String key = getKey(cal);
			
			Integer pls_count = pls_counter.get(key);
			pls_vals.add(pls_count == null ? 0 : (double)pls_count);
			if(process_users) {
				double usr_count = usr_counter.get(key) == null ? 0 : usr_counter.get(key).size();
				usr_vals.add(usr_count);
			}
			cal.add(Calendar.HOUR, 1);
		}
		
		String[] domain = new String[pls_vals.size()];
		double[] pls_data = new double[pls_vals.size()];
		double[] usr_data = new double[pls_vals.size()];
		
		DescriptiveStatistics pls_stats = new DescriptiveStatistics();
		DescriptiveStatistics usr_stats = new DescriptiveStatistics();
		
		for(int i=0; i<pls_vals.size();i++) {
			pls_data[i] = pls_vals.get(i);
			pls_stats.addValue(pls_data[i]);
			if(process_users) {
				usr_data[i] = usr_vals.get(i);
				usr_stats.addValue(usr_data[i]);
			}
			domain[i] = getLabel(startTime);
			startTime.add(Calendar.HOUR, 1);
		}
		
		
		double pls_mean = pls_stats.getMean();
		double pls_sigma = pls_stats.getStandardDeviation();
		
		double usr_mean = usr_stats.getMean();
		double usr_sigma = usr_stats.getStandardDeviation();
		
		// compute z-score
		double[] z_pls_data = new double[pls_vals.size()];
		double[] z_usr_data = new double[pls_vals.size()];
		
		for(int i=0; i<z_pls_data.length;i++) {
			z_pls_data[i] = Math.abs(pls_data[i] - pls_mean)/pls_sigma;
			z_usr_data[i] = Math.abs(usr_data[i] - usr_mean)/usr_sigma;
		}
		
		List<String> labels = new ArrayList<String>();
		labels.add("pls");
		labels.add("z-pls");
		labels.add("users");
		labels.add("z-users");
		
		List<String> titles = new ArrayList<String>();
		titles.add("N. PLS Events around "+p.name+"_"+p.radius);
		titles.add("Z-Score PLS Events around "+p.name+"_"+p.radius);
		titles.add("N. Users around "+p.name+"_"+p.radius);
		titles.add("Z-Score Users around "+p.name+"_"+p.radius);
		
		List<String[]> domains = new ArrayList<String[]>();
		domains.add(domain);
		domains.add(domain);
		domains.add(domain);
		domains.add(domain);
		List<double[]> data = new ArrayList<double[]>();
		data.add(pls_data);
		data.add(z_pls_data);
		data.add(usr_data);
		data.add(z_usr_data);
		
		GraphPlotter.drawMultiGraph(2, 2, "events around "+p.name, titles, "hour", "n.", labels, domains, data);
		//GraphPlotter.drawGraph("events around "+p.name, "events around "+p.name, "events per hour", "hours", "pls events", domain, pls_data);
		Logger.logln("Done!");
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
