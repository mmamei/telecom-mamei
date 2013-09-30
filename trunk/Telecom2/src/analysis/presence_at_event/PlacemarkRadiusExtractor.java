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
	public static double[] RS = new double[]{-500,-400,-300,-200,-100,0,100,200,300,400,500,600,700,800,900,1000,1100,1200,1300,1400,1500};
	
	public static final String[] pnames = new String[]{
		//"Juventus Stadium (TO)"//,"Stadio Olimpico (TO)",
		//"Stadio Silvio Piola (NO)",
		"Stadio San Siro (MI)",
		//"Stadio Atleti Azzurri d'Italia (BG)","Stadio Mario Rigamonti (BS)","Stadio Franco Ossola (VA)"
		};
	
	
	
	public static final double z_threshold = 2;
	
	public static void main(String[] args) throws Exception { 
		
		//Map<String,Double> bestRadius = new HashMap<String,Double>();
		
		String odir = Config.getInstance().base_dir+"/PlacemarkRadiusExtractor";
		new File(odir).mkdirs();
		PrintWriter out = new PrintWriter(new FileWriter(new File(odir+"/result_s.csv")));
		
		for(String pn : pnames) {
			Placemark p = Placemark.getPlacemark(pn);
		    double bestr = getBestRadius(p);
		    out.println(pn+","+bestr);
		    //bestRadius.put(pn, bestr);
		} 
		
		out.close();
		//CopyAndSerializationUtils.save(new File(odir+"/result.ser"), bestRadius);
		
		Logger.logln("Done");
	}
	
	
	public static Map<String,Double> readBestR() throws Exception {
		Map<String,Double> best = new HashMap<String,Double>();
		BufferedReader br = new BufferedReader(new FileReader(new File(Config.getInstance().base_dir+"/PlacemarkRadiusExtractor/result.csv")));
		String line;
		while((line = br.readLine())!=null) {
			String[] e = line.split(",");
			best.put(e[0], Double.parseDouble(e[1]));
		}
		br.close();
		return best;
	}
	
	
	
	public static double getBestRadius(Placemark p) throws Exception {
		
		Logger.logln("Processing "+p.name);
		p.changeRadius(MAX_R);
		
		String file = Config.getInstance().base_dir+"/PLSEventsAroundAPlacemark/"+p.name+"_"+p.radius+".txt";
		File f = new File(file);
		if(!f.exists()) {
			Logger.logln(file+" does not exist");
			Logger.logln("Executing PLSEventsAroundAPlacemark.process()");
			PLSEventsAroundAPlacemark.process(p);
		}
		
		
		// get the city events to be considered to find relevant cells for this placemark
		List<CityEvent> releventEvents = new ArrayList<CityEvent>();
		for(CityEvent e : CityEvent.getEventsInData()) 
			if(e.spot.name.equals(p.name)) releventEvents.add(e);
		
		int[][] n_outliersXradius =  getNOutliersXRadius(file,p,releventEvents);
		
		
		for(int i=0; i<n_outliersXradius.length;i++)
			Logger.logln("radius = "+n_outliersXradius[i][0]+" --> outliers = "+n_outliersXradius[i][1]);
		
		String[] domain = new String[n_outliersXradius.length];
		double[] data = new double[n_outliersXradius.length];
		for(int i=0; i<domain.length;i++) {
			domain[i] = ""+n_outliersXradius[i][0];
			data[i] = n_outliersXradius[i][1];
		}
		
		GraphPlotter.drawGraph(p.name, p.name, "outliers", "radius", "n outliers", domain, data);
		
		
		/*
		// find max number of outliers
		int max = n_outliersXradius[0][1];
		for(int i=1; i<n_outliersXradius.length;i++)
			if(n_outliersXradius[i][1] > max)
				max = n_outliersXradius[i][1];
		
		
		// find the average radius associated with that number of outliser
		double avg_r = 0;
		double cont = 0;
		for(int i=0; i<n_outliersXradius.length;i++) 
			if(n_outliersXradius[i][1] == max) {
				avg_r += n_outliersXradius[i][0];
				cont ++;
			}
		
		avg_r = round(avg_r / cont);
		*/
		
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
	
	
	public static double round(double x) {
		int ri = 0;
		for(int i=1; i<RS.length;i++) {
			if(Math.abs(x-RS[i]) < Math.abs(x-RS[ri]))
				ri = i;
		}
		return RS[ri];
	}
	
	
	public static int[][] getNOutliersXRadius(String file, Placemark p, List<CityEvent> releventEvents) throws Exception {
		
		int[][] n_outliersXradius = new int[RS.length][2];
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
				for(int j=0; j<releventEvents.size();j++) {
					CityEvent e = releventEvents.get(j);
						
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
	
	
	
	static NetworkMap nm = NetworkMap.getInstance();
	public static PLSMap getPLSMap(String file, Placemark p, double maxr) throws Exception {
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
				NetworkCell nc = nm.get(Long.parseLong(celllac));
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
