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
	
	public static final String ODIR = Config.getInstance().base_dir+"/PlacemarkRadiusExtractor/"+Config.getInstance().get_pls_subdir();
	
	public static void main(String[] args) throws Exception { 
		
		boolean individual = true;
		
		String file = individual ? "result_individual.csv" : "result.csv";
		
		new File(ODIR).mkdirs();
		
		if(new File(ODIR+"/"+file).exists()) {
			System.err.println(ODIR+"/"+file+" already exists!!!!!");
			System.err.println("Manually remove the file before proceeding!");
			System.exit(0);
		}
		
		
		List<CityEvent> all = CityEvent.getEventsInData();
		
		// divide all the events by placemark
		Map<String,List<CityEvent>> eventsByPlacemark = new HashMap<String,List<CityEvent>>();
		
		PrintWriter out = null;
		if(!individual) {
			out = new PrintWriter(new FileWriter(new File(ODIR+"/result.csv")));
			for(CityEvent ce: all) {
				List<CityEvent> l = eventsByPlacemark.get(ce.spot.name);
				if(l==null) {
					l = new ArrayList<CityEvent>();
					eventsByPlacemark.put(ce.spot.name, l);
				}
				l.add(ce);
			}
		}
		if(individual) {
			out = new PrintWriter(new FileWriter(new File(ODIR+"/result_individual.csv")));
			for(CityEvent ce: all) {
				List<CityEvent> l = new ArrayList<CityEvent>();
				l.add(ce);
				eventsByPlacemark.put(ce.toString(), l);
			}
		}
		
		
		for(String p : eventsByPlacemark.keySet()) {
		    double bestr = getBestRadius(eventsByPlacemark.get(p));
		    out.println(p+","+bestr);
		    Logger.logln(p+","+bestr);
		} 
		
		out.close();
		Logger.logln("Done");
	}
	
	
	public static Map<String,Double> readBestR(boolean individual) throws Exception {
		if(individual)
			return readBestR(ODIR+"/result_individual.csv");
		else
			return readBestR(ODIR+"/result.csv");
	}
	
	
	public static double getBestRadius(List<CityEvent> le) throws Exception {
		
		String name = le.size() == 1 ? le.get(0).toFileName() : le.get(0).spot.name;
		double[][] valXradius = createOrLoadValueRadiusDistrib(name, le);
	
		if(le.size()==1) 
			return getWeightedAverageWithThreshold(valXradius,0.5);	
		else 
			return getWeightedAverage(valXradius);
	}
	
	
	public static double[][] createOrLoadValueRadiusDistrib(String name, List<CityEvent> le) throws Exception {
		double[][] valueRadiusDistrib  = null;
		// restore
		File f = new File(ODIR+"/"+name+"/zXradius.ser");
		if(f.exists()) valueRadiusDistrib = (double[][])CopyAndSerializationUtils.restore(f);
		else {
			//create
			File d = new File(ODIR+"/"+name);
			if(!d.exists()) d.mkdirs();
			
			if(le.size()==1)
				valueRadiusDistrib = computeZXRadius(le.get(0));
			else
				valueRadiusDistrib = computeNOutliersRadiusDistrib(le);
			CopyAndSerializationUtils.save(f, valueRadiusDistrib);
		}
		plot(name,valueRadiusDistrib,ODIR+"/"+le.get(0).toFileName()+"/val_r_distrib.png");
		
		return valueRadiusDistrib;
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
	
	public static double getWeightedAverageWithThreshold(double[][] zXradius,double th) {
		double avg_r = 0;
		double cont = 0;
			
		for(int i=0; i<zXradius.length;i++) {
			if(zXradius[i][1] > th) {
				avg_r = avg_r + zXradius[i][0] * zXradius[i][1];
				cont = cont + zXradius[i][1];
			}
		}
		
		if(cont == 0) return -200;
		else return PlacemarkRadiusExtractor.round(avg_r / cont);
	}
	
	
	
	
	
	public static final double z_threshold = 2;
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
	
	
public static double[][] computeZXRadius(CityEvent e) throws Exception {
		
		Placemark p = e.spot;
		p.changeRadius(MAX_R);
		
		String subdir = Config.getInstance().get_pls_subdir();
		
		String file = Config.getInstance().base_dir+"/PLSEventsAroundAPlacemark/"+subdir+"/"+p.name+"_"+p.radius+".txt";
		File f = new File(file);
		if(!f.exists()) {
			Logger.logln(file+" does not exist");
			Logger.logln("Executing PLSEventsAroundAPlacemark.process()");
			PLSEventsAroundAPlacemark.process(p);
		}
		
		double[][] zXradius = new double[1+(MAX_R - MIN_R)/STEP][2];
		int index = 0;
		
		
		DescriptiveStatistics[] maxr_stats = null;
		
		for(int max_r = MAX_R; max_r >= MIN_R; max_r = max_r - STEP) {
			
			PLSMap plsmap = PlacemarkRadiusExtractor.getPLSMap(file,p,max_r);
					
			
			double max_z = 0;
			double sum_z = 0;
		
			if(plsmap.startTime != null) {
				
			
				DescriptiveStatistics[] stats = PLSBehaviorInAnArea.getStats(plsmap);
				
				if(max_r == MAX_R) 
					maxr_stats = clone(stats);
				
				/* space norm */
				/*
				DescriptiveStatistics[] norm_stats = new DescriptiveStatistics[stats.length];
				
				for(int i=0; i<stats.length;i++) {
					norm_stats[i] = new DescriptiveStatistics();
					double[] vals = stats[i].getValues();
					double[] maxr_vals = maxr_stats[i].getValues();
					for(int j=0; j<vals.length;j++)
						if(maxr_vals[j] == 0) {
							if(vals[j] != 0) 
								System.err.println("error!");
							norm_stats[i].addValue(0);
						}  
						else norm_stats[i].addValue(vals[j]/maxr_vals[j]);
				}
				*/
				DescriptiveStatistics[] norm_stats = stats;
				
				//double[] z_pls_data = PLSBehaviorInAnArea.getZ(norm_stats[0],plsmap.startTime);
				double[] z_usr_data =  PLSBehaviorInAnArea.getZ2(norm_stats[1],plsmap.startTime);
				List<CityEvent> relevant = new ArrayList<CityEvent>();
				relevant.add(e);
							
				GraphPlotter gs = PLSBehaviorInAnArea.drawGraph(p.name+"_"+max_r,plsmap.getDomain(),z_usr_data,plsmap,relevant);
				gs.save(ODIR+"/"+e.toFileName()+"/maxr="+max_r+".png");
				
				
				Calendar cal = (Calendar)plsmap.startTime.clone();
				
				for(int i=0;i<plsmap.getHours();i++) {
					
					if(cal.after(e.et)) break; // we are already after the event
					
					if(e.st.before(cal) && e.et.after(cal)) {
						max_z = Math.max(max_z, z_usr_data[i]);
						sum_z = sum_z + z_usr_data[i];
					}
						
					cal.add(Calendar.HOUR_OF_DAY, 1);
				}
			
			}
			zXradius[index][0] = max_r;
			//zXradius[index][1] = sum_z/h;
			zXradius[index][1] = max_z;
			
			System.out.println(max_r+"  --> "+max_z);
			
			index++;
		}
		
		// spatial normalization
		if(zXradius[0][1]==0) zXradius[0][1]=0.0001; // laplace smoothing
		double zarea =  zXradius[0][1];
		for(int i=0; i<zXradius.length;i++) {
			zXradius[i][1] = zXradius[i][1] - zarea;
			if(zXradius[i][1] < 0) zXradius[i][1] = 0;
		}
		
		
		return zXradius;		
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
	
	static  DescriptiveStatistics[] clone(DescriptiveStatistics[] x) {
		DescriptiveStatistics[] y = new DescriptiveStatistics[x.length];
		
		for(int i=0; i<y.length;i++) {
			y[i] = new DescriptiveStatistics();
			double[] vals = x[i].getValues();
			for(int j=0; j<vals.length;j++)
				y[i].addValue(vals[j]);
		}
		return y;
	}
}
