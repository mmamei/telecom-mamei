package analysis.presence_at_event;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import network.NetworkCell;
import network.NetworkMap;
import network.NetworkMapFactory;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.gps.utils.LatLonUtils;

import pls_parser.PLSEventsAroundAPlacemark;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;
import visual.java.GraphPlotter;
import analysis.PLSBehaviorInAnArea;
import analysis.PLSMap;
import area.CityEvent;
import area.Placemark;

public class PlacemarkRadiusExtractor {
	
	
	public static final int MAX_R = 1500;
	public static final int MIN_R = -500;
	public static final int STEP = 100;
	
	
	public static final double z_threshold = 2;
	
	
	public static final String ODIR = Config.getInstance().base_dir+"/PlacemarkRadiusExtractor/"+Config.getInstance().get_pls_subdir();
	
	public static void main(String[] args) throws Exception { 
		
		new File(ODIR).mkdirs();
		
		if(new File(ODIR+"/result.csv").exists()) {
			System.err.println(ODIR+"/result.csv already exists!!!!!");
			System.err.println("Manually remove the file before proceeding!");
			System.exit(0);
		}
		
		PrintWriter out = new PrintWriter(new FileWriter(new File(ODIR+"/result.csv")));
		
		
		List<CityEvent> all = CityEvent.getEventsInData();
		// divide all the events by placemark
		Map<String,List<CityEvent>> eventsByPlacemark = new HashMap<String,List<CityEvent>>();
		for(CityEvent ce: all) {
			List<CityEvent> l = eventsByPlacemark.get(ce.spot.name);
			if(l==null) {
				l = new ArrayList<CityEvent>();
				eventsByPlacemark.put(ce.spot.name, l);
			}
			l.add(ce);
		}
		
		for(String p : eventsByPlacemark.keySet()) {
		    double bestr = getBestRadius(eventsByPlacemark.get(p));
		    out.println(p+","+bestr);
		    Logger.logln(p+","+bestr);
		} 
		
		out.close();
		Logger.logln("Done");
	}
	
	
	public static Map<String,Double> readBestR() throws Exception {
		return readBestR(ODIR+"/result.csv");
	}
	
	
	
	public static double getBestRadius(List<CityEvent> re) throws Exception {
		double[][] n_outliersXradius =  createOrLoadNOutliersRadiusDistrib(re);
		return getWeightedAverage(n_outliersXradius);
	}
	
	
	
	public static double getWeightedAverage(double[][] n_outliersXradius) throws Exception {	
		double avg_r = 0;
		double cont = 0;
		for(int i=0; i<n_outliersXradius.length;i++) {
			avg_r = avg_r + n_outliersXradius[i][0] * n_outliersXradius[i][1];
			cont = cont + n_outliersXradius[i][1];
		}
		avg_r = round(avg_r / cont);
		
		Logger.logln("best radius = "+avg_r);
		
		return avg_r;
	}
	
	
	
	public static double[][] createOrLoadNOutliersRadiusDistrib(List<CityEvent> relevantEvents) throws Exception {
		Placemark p = relevantEvents.get(0).spot;
		double[][] n_outliersXradius  = null;
		// restore
		File f = new File(ODIR+"/"+p.name+"/zXradius.ser");
		if(f.exists()) n_outliersXradius = (double[][])CopyAndSerializationUtils.restore(f);
		else {
			//create
			File d = new File(ODIR+"/"+p.name);
			if(!d.exists()) d.mkdirs();
			n_outliersXradius = computeNOutliersRadiusDistrib(relevantEvents);
			CopyAndSerializationUtils.save(f, n_outliersXradius);
		}
		
		
		plot(p.name,n_outliersXradius,null);
		
		return n_outliersXradius;
	}
	
	
	public static double[][] computeNOutliersRadiusDistrib(List<CityEvent> relevantEvents) throws Exception {
		
		Placemark p = relevantEvents.get(0).spot;
		Logger.logln("Processing "+p.name);
		p.changeRadius(MAX_R);
		String subdir = Config.getInstance().get_pls_subdir();
		String file = Config.getInstance().base_dir+"/PLSEventsAroundAPlacemark/"+subdir+"/"+p.name+"_"+p.radius+".txt";
		File f = new File(file);
		if(!f.exists()) {
			Logger.logln(file+" does not exist");
			Logger.logln("Executing PLSEventsAroundAPlacemark.process()");
			PLSEventsAroundAPlacemark.process(p);
		}
		
		double[][] n_outliersXradius = new double[1+(MAX_R - MIN_R)/STEP][2];
		int index = 0;
		
		for(int max_r = MAX_R; max_r >= MIN_R; max_r = max_r - STEP) {
			PLSMap plsmap = getPLSMap(file,p,max_r);
			
			int outliers_count = 0;
			
			
			if(plsmap.startTime != null) {
				DescriptiveStatistics[] stats = PLSBehaviorInAnArea.getStats(plsmap);
				double[] z_pls_data = PLSBehaviorInAnArea.getZ(stats[0],plsmap.startTime);
				double[] z_usr_data =  PLSBehaviorInAnArea.getZ(stats[1],plsmap.startTime);
					
			    Calendar cal = (Calendar)plsmap.startTime.clone();
				int i = 0;
				
				
				next_event:	
				for(int j=0; j<relevantEvents.size();j++) {
					CityEvent e = relevantEvents.get(j);
						
					for(;i<plsmap.getHours();i++) {
						
						boolean after_event = cal.after(e.et);
						boolean found_outlier = e.st.before(cal) && e.et.after(cal) && (z_pls_data[i] > z_threshold || z_usr_data[i] > z_threshold);
							
						if(found_outlier) 
							outliers_count ++;
							
						cal.add(Calendar.HOUR_OF_DAY, 1);
						if(after_event || found_outlier) {
							i++;
							continue next_event;
						}
					}
				}
			}
			
			n_outliersXradius[index][0] = max_r;
			n_outliersXradius[index][1] = outliers_count;
			
			index++;
			
			
		}
		return n_outliersXradius;	
	}
	
	
	/**********************************************************************************************************/
	
	static Map<String,Double> readBestR(String file) throws Exception {
		Map<String,Double> best = new HashMap<String,Double>();
		BufferedReader br = new BufferedReader(new FileReader(new File(file)));
		String line;
		while((line = br.readLine())!=null) {
			String[] e = line.split(",");
			best.put(e[0], Double.parseDouble(e[1]));
		}
		br.close();
		return best;
	}
	
	static double round(double x) {
		int best = MAX_R;
		for(int r = MAX_R; r >= MIN_R; r = r - STEP) 
			if(Math.abs(x-r) < Math.abs(x-best))
				best = r;
		return best;
	}
	
	
	static void plot(String title, double[][] n_outliersXradius, String save_file) {
		
		for(int i=0; i<n_outliersXradius.length;i++)
			Logger.logln("radius = "+n_outliersXradius[i][0]+" --> outliers = "+n_outliersXradius[i][1]);
		
		
		String[] domain = new String[n_outliersXradius.length];
		double[] data = new double[n_outliersXradius.length];
		for(int i=0; i<domain.length;i++) {
			domain[i] = ""+n_outliersXradius[i][0];
			data[i] = n_outliersXradius[i][1];
		}
		
		GraphPlotter g = GraphPlotter.drawGraph(title, title, "outliers", "radius", "n outliers", domain, data);
		if(save_file != null)
			g.save(save_file);
	}
	
	static NetworkMap NM = NetworkMapFactory.getNetworkMap();
	static PLSMap getPLSMap(String file, Placemark p, double maxr) throws Exception {
		PLSMap plsmap = new PLSMap();
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
				String key = PLSBehaviorInAnArea.getKey(cal);
				String celllac = splitted[3]; 
				NetworkCell nc = NM.get(Long.parseLong(celllac));
				double dist = LatLonUtils.getHaversineDistance(nc.getPoint(), p.center_point) - nc.getRadius();
				//System.out.println(dist);
				
				if(dist < maxr) {
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
		}
		in.close();
		return plsmap;
	}	
}
