package analysis.presence_at_event;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
	
	public static final boolean PLOT = false;
	public static final boolean CALL_PRESENCE_COUNTER_AFTERWARDS = !PLOT;
	public static final boolean NORMALIZE_SPATIALLY = true;
	public static final boolean DIFF = false;
	
	public static final int MAX_R = 1500;
	public static final int MIN_R = -500;
	public static final int STEP = 100;
	
	
	public static final int W_AVG = 0;
	public static final int UP_TO_PERCENT = 1;
	public static final int MAX_DROP = 2;
	public static final int MODE = W_AVG;
	
	// if MAX = true we compute the zXradius as the max of the z within the event time interval
	// if MAX = false we compute the zXradius as the sum of the z within the event time interval
	public static final boolean MAX = false;
	
	
	public static final String ODIR = Config.getInstance().base_dir+"/PlacemarkRadiusExtractor/"+Config.getInstance().get_pls_subdir();
	
	
	public static void main(String[] args) throws Exception { 
		
		String file = "result_individual.csv";
		
		new File(ODIR).mkdirs();
		File f = new File(ODIR+"/"+file);
		if(f.exists() && f.length() > 0) {
			System.err.println(ODIR+"/"+file+" already exists!!!!!");
			System.err.println("Manually remove the file before proceeding!");
			//System.exit(0);
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
			List<double[][]> valXradius = createOrLoadValueRadiusDistrib(le,false);
			List<double[][]> valXring = createOrLoadValueRadiusDistrib(le,true);
			
			if(NORMALIZE_SPATIALLY) {
				Placemark x = le.get(0).spot.clone();
				normalizeByArea(valXradius,x);
				x.ring = true;
				normalizeByArea(valXring,x);
			}
			
			List<double[][]> diff = diff(valXradius,valXring);
			
			
			
			if(PLOT)
				for(int i=0; i<le.size();i++)  {
					
					double[][] data1 = valXradius.get(i);
					double[][] data2 = valXring.get(i);
					double[][] data3 = diff.get(i);
					
					
					double durationH = le.get(i).durationH();
					for(int r=0; r<data1.length;r++) {
						data1[r][1] = data1[r][1] / durationH;
						if(data2 != null) data2[r][1] = data2[r][1] / durationH;
						if(data3 != null) data3[r][1] = data3[r][1] / durationH;
					}
						
					
					
					
					plot(le.get(i).toString(),data1,data2,data3,ODIR+"/"+le.get(i)+"_val_r_distrib.png");
				}
			
			for(int i=0; i<valXradius.size(); i++) {

				double[][] vxr = DIFF ? diff.get(i) : valXradius.get(i);
				double bestr = 0;
				if(MODE == W_AVG) bestr = getWeightedAverage(vxr);
				else if(MODE == UP_TO_PERCENT) bestr = getUpToPercentage(vxr,0.1);
				else if(MODE == MAX_DROP) bestr = getMaxZDrop(vxr,le.get(i).toString());
				out.println(le.get(i).toString()+","+bestr);
				Logger.logln(le.get(i).toString()+","+bestr);
			}
		} 
		
		out.close();
		Logger.logln("Done");
		
		if(CALL_PRESENCE_COUNTER_AFTERWARDS) PresenceCounter.main(null);
		
	}
	
	public static void normalizeByArea(List<double[][]> valXradius, Placemark p) {
		for(double[][] vxr: valXradius) {
			for(int i=0;i<vxr.length;i++) {
				if(p.ring) p.changeRadiusRing(vxr[i][0]);
				else p.changeRadius(vxr[i][0]);
				double a = p.getSumRadii();
				vxr[i][1] = a == 0 ? 0 : vxr[i][1] / a;
			}
		}
	}
	
	
	public static List<double[][]> diff(List<double[][]> a, List<double[][]> b) {
		List<double[][]> res = new ArrayList<double[][]>();
		for(int i=0; i<a.size();i++){
			double[][] x = a.get(i);
			double[][] y = b.get(i);
			double[][] z = new double[x.length][x[0].length];
			for(int j=0;j<z.length;j++) {
				z[j][0] = x[j][0];
				z[j][1] = x[j][1] - y[j][1];
				if(z[j][1] < 0 || z[j][0] == 1500) z[j][1] = 0;
			}
			res.add(z);
		}
		return res;
	}
	
	
	public static List<double[][]> createOrLoadValueRadiusDistrib(List<CityEvent> le, boolean ring) throws Exception {
		List<double[][]> list_valueRadiusDistrib  = null;
		// restore
		
		String n = le.get(0).spot.name;
		if(ring) n = "ring_"+n;
		
		
		
		
		File f = MAX ? new File(ODIR+"/zXr_ser_computed_with_max/"+n+"_zXradius.ser") : new File(ODIR+"/"+n+"_zXradius.ser");
		if(f.exists()) list_valueRadiusDistrib = (List<double[][]>)CopyAndSerializationUtils.restore(f);
		else {
			list_valueRadiusDistrib = computeZXRadius(le,ring); 
			CopyAndSerializationUtils.save(f, list_valueRadiusDistrib);
		}
		return list_valueRadiusDistrib;
	}

	
	
	public static double getWeightedAverage(double[][] valXradius) {
		double avg_r = 0;
		double cont = 0;
		for(int i=0; i<valXradius.length;i++) {
			avg_r = avg_r + valXradius[i][0] * valXradius[i][1];
			cont = cont + valXradius[i][1];
		}
		if(cont == 0) return -200;
		else return round(avg_r / cont);
	}
	
	
	public static double getUpToPercentage(double[][] valXradius, double percent) {
		double sum = 0;
		for(int i=0; i<valXradius.length;i++) 
			sum = sum + valXradius[i][1];
		
		double cum = 0;
		for(int i=0; i<valXradius.length;i++) {
			cum = cum + valXradius[i][1]/sum;
			if(cum > (1-percent))
				return  valXradius[i-1][0];
		}
		
		System.err.println("problem");
		return 0;
	}
	
	
	
	public static double getMaxZDrop(double[][] valXradius, String title) {
		
		// get values
		double[] f = new double[valXradius.length+1];
		f[0] = valXradius[0][1];
		for(int i=1; i<f.length;i++)
			f[i] = valXradius[i-1][1] == 0 ? f[i-1] : valXradius[i-1][1];
		
	
		double[] derivative = new double[valXradius.length];
		for(int i=0; i<valXradius.length;i++) 
			derivative[i] = f[i+1] - f[i];
		
		// smooth
		
		double[] smooth = new double[derivative.length-2];
		
		for(int i=0; i<smooth.length;i++)
			smooth[i] = (derivative[i]+ 2*derivative[i+1] + derivative[i+2]) / 4;
		
		// get max
		
		int imax = 0;
		int imin = 0;
		for(int i=1; i<smooth.length;i++) {
			if(smooth[i] > smooth[imax])
				imax = i;
			if(smooth[i] > 0 && smooth[imin] > smooth[i])
				imin = i;
		}
		
		if(PLOT) {
			// plot derivative
			String[] domain = new String[smooth.length];
			for(int i=0; i<domain.length;i++)
				domain[i] = ""+valXradius[i+1][0];
			
			GraphPlotter.drawGraph("d/dX "+title, "d/dX "+title, "", "radius", "smooth", domain, smooth);
		}
		return valXradius[imax][0];
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
	
	public static List<double[][]> computeZXRadius(List<CityEvent> relevantEvents, boolean ring) throws Exception {
		
		Placemark p = relevantEvents.get(0).spot.clone();
		p.changeRadius(MAX_R);
		Logger.logln("Processing events associated to "+p.name);
		
		String subdir = Config.getInstance().get_pls_subdir();
		
		String file = "G:/BASE/PLSEventsAroundAPlacemark/"+subdir+"/"+p.name+"_"+p.getR()+".txt";
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
			
			if(ring) p.changeRadiusRing(max_r);
			else p.changeRadius(max_r);
			PLSMap plsmap = getPLSMap(file,p);
			
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
			
			if (PLOT) PLSBehaviorInAnArea.drawGraph(p.name+"_"+max_r,plsmap.getDomain(),z_usr_data,plsmap,relevantEvents);
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
						zXradius.get(i)[index][1] = zXradius.get(i)[index][1] + z_usr_data[h]; // Math.max(zXradius.get(i)[index][1], z_usr_data[h]);				
					cal.add(Calendar.HOUR_OF_DAY, 1);
					
					if(after_event) {
						h++;
						continue next_event;
					}
				}
			}
			index++;
		}
		
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
	
	
	static void plot(String title, double[][] valXradius, double[][] valXring, double[][] diff, String save_file) {
		Logger.logln(title+" *******************************");
		String[] domain = new String[valXradius.length];
		
		double[] data1 = new double[valXradius.length];
		double[] data2 = new double[valXradius.length];
		double[] data3 = new double[valXradius.length];
	
		for(int i=0; i<domain.length;i++) {
			domain[i] = ""+valXradius[i][0];
			data1[i] = valXradius[i][1];
			if(valXring!=null) data2[i] = valXring[i][1];
			if(diff!=null) data3[i] = diff[i][1];
		}
		/*
		data1 = norm(data1);
		data2 = norm(data2);
		data3 = norm(data3);
		*/
		GraphPlotter g = GraphPlotter.drawGraph(title, title, "area", "radius", "z", domain, data1);
		if(valXring!=null) 	g.addData("ring", data2);
		if(diff!=null)	g.addData("diff", data3);
		if(save_file != null)
			g.save(save_file);
	}
	
	static double[] norm(double[] x) {
		double[] y = new double[x.length];
		double sum = 0;
		for(int i=0; i<x.length; i++)
			sum = sum + x[i];
		for(int i=0; i<x.length; i++)
			y[i] = x[i] / sum;
		return y;
	}
	
	
	
	
	static PLSMap getPLSMap(String file, Placemark p) {
			
		String dir = Config.getInstance().base_dir+"/PlacemarkRadiusExtractor/"+Config.getInstance().get_pls_subdir()+"/saved_plsmaps";
		new File(dir).mkdirs();
		File f = new File(dir+"/PLSMap_"+p.toString()+".ser");
		if(f.exists()) {
			Logger.logln("Restoring: "+f.getAbsolutePath());
			return (PLSMap)CopyAndSerializationUtils.restore(f);
		}
		
		PLSMap plsmap = new PLSMap();
		String[] splitted;
		String line;
		Calendar cal = new GregorianCalendar();
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(file));			
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
						if(p.contains(celllac)) {
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
						//System.err.println("bad read: "+line);
						continue;
					}
				}
			}
			in.close();
		}catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		CopyAndSerializationUtils.save(f, plsmap);
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
