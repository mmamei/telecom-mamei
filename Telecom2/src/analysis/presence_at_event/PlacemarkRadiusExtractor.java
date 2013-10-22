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

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
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
	public static final int STEP = 200;
	
	public static final String ODIR = Config.getInstance().base_dir+"/PlacemarkRadiusExtractor/"+Config.getInstance().get_pls_subdir();
	
	
	public static final boolean INDIVIDUAL = true;
	
	public static void main(String[] args) throws Exception { 
		
		String file = INDIVIDUAL ? "result_individual.csv" : "result.csv";
		
		new File(ODIR).mkdirs();
		File f = new File(ODIR+"/"+file);
		if(f.exists() && f.length() > 0) {
			System.err.println(ODIR+"/"+file+" already exists!!!!!");
			System.err.println("Manually remove the file before proceeding!");
			System.exit(0);
		}
		
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
		
		PrintWriter out = new PrintWriter(new FileWriter(f));
		for(String p : eventsByPlacemark.keySet()) {
			List<CityEvent> le = eventsByPlacemark.get(p);
			List<double[][]> valXradius = createOrLoadValueRadiusDistrib(le);
			
			if(INDIVIDUAL) {
				for(int i=0; i<le.size(); i++) {
					double[][] vxr = valXradius.get(i);
					double bestr = getWeightedAverageWithThreshold(vxr,0);
					//double bestr = getMaxZDrop(vxr);
					out.println(le.get(i).toString()+","+bestr);
					Logger.logln(le.get(i).toString()+","+bestr);
				}
			}
			else {
				double bestr = getWeightedAverageWithThreshold(group(valXradius),0);	
				out.println(p+","+bestr);
				Logger.logln(p+","+bestr);
			}
		} 
		
		out.close();
		Logger.logln("Done");
	}
	
	
	public static List<double[][]> createOrLoadValueRadiusDistrib(List<CityEvent> le) throws Exception {
		List<double[][]> list_valueRadiusDistrib  = null;
		// restore
		File f = new File(ODIR+"/"+le.get(0).spot.name+"_zXradius.ser");
		if(f.exists()) list_valueRadiusDistrib = (List<double[][]>)CopyAndSerializationUtils.restore(f);
		else {
			list_valueRadiusDistrib = computeZXRadius(le);
			CopyAndSerializationUtils.save(f, list_valueRadiusDistrib);
		}
		
		for(int i=0; i<le.size();i++) 
			plot(le.get(i).toString(),list_valueRadiusDistrib.get(i),ODIR+"/"+le.get(i).toFileName()+"_val_r_distrib.png");
		
		return list_valueRadiusDistrib;
	}

	
	
	public static double getWeightedAverageWithThreshold(double[][] valXradius,double th) {
		double avg_r = 0;
		double cont = 0;
			
		for(int i=0; i<valXradius.length;i++) {
			if(valXradius[i][1] > th) {
				avg_r = avg_r + valXradius[i][0] * valXradius[i][1];
				cont = cont + valXradius[i][1];
			}
		}
		if(cont == 0) return -200;
		else return round(avg_r / cont);
	}
	
	
	public static double getMaxZDrop(double[][] valXradius) {
		
		double[] derivative = new double[valXradius.length-1];
		for(int i=1; i<valXradius.length;i++) 
			derivative[i-1] = valXradius[i][1] - valXradius[i-1][1];
		
		// smooth
		double[] smooth = new double[derivative.length];
		for(int i=0; i<smooth.length-1;i++)
			smooth[i] = (2*derivative[i] + derivative[i+1]) / 3;
		
		// get max
		
		int imax = 0;
		for(int i=1; i<smooth.length;i++) {
			if(smooth[i] > smooth[imax])
				imax = i;
		}
		return valXradius[imax+1][0];
	}
	
	
	
	
	
	
	public static final double z_threshold = 2;
	public static double[][] group(List<double[][]> valXradius) {
		return null;
	}
	/*
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
	*/
	
	public static List<double[][]> computeZXRadius(List<CityEvent> relevantEvents) throws Exception {
		
		Placemark p = relevantEvents.get(0).spot;
		p.changeRadius(MAX_R);
		Logger.logln("Processing events associated to "+p.name);
		
		String subdir = Config.getInstance().get_pls_subdir();
		
		String file = Config.getInstance().base_dir+"/PLSEventsAroundAPlacemark/"+subdir+"/"+p.name+"_"+p.radius+".txt";
		File f = new File(file);
		if(!f.exists()) {
			Logger.logln(file+" does not exist");
			Logger.logln("Executing PLSEventsAroundAPlacemark.process()");
			PLSEventsAroundAPlacemark.process(p);
		}
		
		List<double[][]> zXradius = new ArrayList<double[][]>();
		for(int i=0; i<relevantEvents.size();i++)
			zXradius.add(new double[1+(MAX_R - MIN_R)/STEP][2]);
		
		
		int index = 0;
		
		for(int max_r = MAX_R; max_r >= MIN_R; max_r = max_r - STEP) {
			PLSMap plsmap = getPLSMap(file,p,max_r);
			
			if(plsmap.startTime == null) {
				for(int i=0; i<relevantEvents.size();i++) {
					zXradius.get(i)[index][0] = max_r;
					zXradius.get(i)[index][1] = 0;
				}
				index++;
				continue;
			}
			
			DescriptiveStatistics[] stats = PLSBehaviorInAnArea.getStats(plsmap);
			double[] z_usr_data =  PLSBehaviorInAnArea.getZ2(stats[1],plsmap.startTime);
			
			PLSBehaviorInAnArea.drawGraph(p.name+"_"+max_r,plsmap.getDomain(),z_usr_data,plsmap,relevantEvents);
			Calendar cal = (Calendar)plsmap.startTime.clone();
			
			int h = 0;
			
			next_event:
			for(int i=0; i<relevantEvents.size();i++) {
				
				CityEvent e = relevantEvents.get(i);
				zXradius.get(i)[index][0] = max_r;
				zXradius.get(i)[index][1] = 0;
				for(;h<plsmap.getHours();h++) {
					
					boolean after_event = cal.after(e.et);
					
					if(e.st.before(cal) && e.et.after(cal)) 
						zXradius.get(i)[index][1] = Math.max(zXradius.get(i)[index][1], z_usr_data[h]);				
					cal.add(Calendar.HOUR_OF_DAY, 1);
					
					if(after_event) {
						h++;
						continue next_event;
					}
				}
			}
			index++;
		}
		
		/*
		// spatial normalization
		if(zXradius[0][1]==0) zXradius[0][1]=0.0001; // laplace smoothing
		double zarea =  zXradius[0][1];
		for(int i=0; i<zXradius.length;i++) {
			zXradius[i][1] = zXradius[i][1] - zarea;
			if(zXradius[i][1] < 0) zXradius[i][1] = 0;
		}
		*/
		return zXradius;		
	}
	
	
	
	public static Map<String,Double> readBestR(boolean individual) throws Exception {
		if(individual)
			return readBestR(ODIR+"/result_individual.csv");
		else
			return readBestR(ODIR+"/result.csv");
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
		Logger.logln(title+" *******************************");
		for(int i=0; i<n_outliersXradius.length;i++)
			Logger.logln("radius = "+n_outliersXradius[i][0]+" --> val = "+n_outliersXradius[i][1]);
		
		
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
				try{
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
				}catch(Exception e) {
					System.err.println("bad read: "+line);
					continue;
				}
			}
		}
		in.close();
		return plsmap;
	}
	/*
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
	*/
}
