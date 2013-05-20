package analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
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

public class PLSBehaviorInAnAreaRefined {
	
	static final String[] MONTHS = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	static final String[] DAYS = new String[]{"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
	
	public static void main(String[] args) throws Exception { 
		//Placemark p = Placemark.getPlacemark("Juventus Stadium (TO)");
		Placemark p = Placemark.getPlacemark("Stadio Olimpico (TO)");
		//Placemark p = Placemark.getPlacemark("Stadio Silvio Piola (NO)");
		process(p);
	}
	
	public static void process(Placemark p) throws Exception {
		
		String file = Config.getInstance().base_dir+"/PLSEventsAroundAPlacemark/"+p.name+".txt";
		File f = new File(file);
		if(!f.exists()) {
			Logger.logln(file+" does not exist");
			Logger.logln("Executing PLSEventsAroundAPlacemark.process()");
			PLSEventsAroundAPlacemark.process(p);
		}
		
		
		List<PlsEvent> events = PlsEvent.readEvents(new File(file));
		
		Map<String,Set<String>> usr_counter = new TreeMap<String,Set<String>>();
		Map<String,Integer> pls_counter = new TreeMap<String,Integer>();
		
		
		
		for(PlsEvent e: events) {
			String key = getKey(e.getCalendar());
			
			Set<String> users = usr_counter.get(key);
			if(users == null) users = new TreeSet<String>();
			users.add(e.getUsername());
			usr_counter.put(key, users);
			
			Integer count = pls_counter.get(key);
			pls_counter.put(key, count == null ? 0 : count+1);
		}
		
		List<Double> usr_vals = new ArrayList<Double>();
		List<Double> pls_vals = new ArrayList<Double>();
		
		Calendar startTime = events.get(0).getCalendar();
		Calendar endTime = events.get(events.size()-1).getCalendar();
		while(!startTime.after(endTime)) {
			String key = getKey(startTime);
			
			Integer pls_count = pls_counter.get(key);
			pls_vals.add(pls_count == null ? 0 : (double)pls_count);
			
			double usr_count = usr_counter.get(key) == null ? 0 : usr_counter.get(key).size();
			usr_vals.add(usr_count);
			
			startTime.add(Calendar.HOUR, 1);
		}
		
		String[] domain = new String[pls_vals.size()];
		double[] pls_data = new double[pls_vals.size()];
		double[] usr_data = new double[pls_vals.size()];
		
		DescriptiveStatistics[] pls_stats = new DescriptiveStatistics[24];
		DescriptiveStatistics[] usr_stats = new DescriptiveStatistics[24];
		for(int i=0; i<24;i++) {
			pls_stats[i] = new DescriptiveStatistics();
			usr_stats[i] = new DescriptiveStatistics();
		}
		
		startTime = events.get(0).getCalendar();
		for(int i=0; i<pls_vals.size();i++) {
			pls_data[i] = pls_vals.get(i);
			usr_data[i] = usr_vals.get(i);
			pls_stats[startTime.get(Calendar.HOUR_OF_DAY)].addValue(pls_data[i]);
			usr_stats[startTime.get(Calendar.HOUR_OF_DAY)].addValue(usr_data[i]);
			domain[i] = getLabel(startTime);
			startTime.add(Calendar.HOUR_OF_DAY, 1);
		}
		
		
		double[] pls_mean = new double[24];
		double[] usr_mean = new double[24];
		for(int i=0; i<24;i++) {
			pls_mean[i] = pls_stats[i].getMean();
			usr_mean[i] = usr_stats[i].getMean();
		}
		
		
		// compute normalized values (actual values - mean of that hour) 
		double[] n_pls_data = new double[pls_vals.size()];
		double[] n_usr_data = new double[pls_vals.size()];
		
		startTime = events.get(0).getCalendar();
		for(int i=0; i<pls_vals.size();i++) {
			n_pls_data[i] = (pls_data[i] - pls_mean[startTime.get(Calendar.HOUR_OF_DAY)]);
			n_usr_data[i] = (usr_data[i] - usr_mean[startTime.get(Calendar.HOUR_OF_DAY)]);
			startTime.add(Calendar.HOUR_OF_DAY, 1);
		}
		
		
		List<String> labels = new ArrayList<String>();
		labels.add("pls");
		labels.add("n-pls");
		labels.add("users");
		labels.add("n-users");
		
		List<String> titles = new ArrayList<String>();
		titles.add("N. PLS Events around "+p.name);
		titles.add("Normalized PLS Events around "+p.name);
		titles.add("N. Users around "+p.name);
		titles.add("Normalized Users around "+p.name);
		
		List<String[]> domains = new ArrayList<String[]>();
		domains.add(domain);
		domains.add(domain);
		domains.add(domain);
		domains.add(domain);
		List<double[]> data = new ArrayList<double[]>();
		data.add(pls_data);
		data.add(n_pls_data);
		data.add(usr_data);
		data.add(n_usr_data);
		
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
