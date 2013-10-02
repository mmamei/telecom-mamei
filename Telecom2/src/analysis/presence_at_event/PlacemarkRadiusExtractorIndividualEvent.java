package analysis.presence_at_event;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import pls_parser.PLSEventsAroundAPlacemark;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;
import visual.java.GraphPlotter;
import analysis.PLSBehaviorInAnArea;
import analysis.PLSMap;
import area.CityEvent;
import area.Placemark;

public class PlacemarkRadiusExtractorIndividualEvent {
	
	
	public static final int MAX_R = 1500;
	public static final int MIN_R = -500;
	public static final int STEP = 200;
	
	public static final String ODIR = Config.getInstance().base_dir+"/PlacemarkRadiusExtractorIndividualEvent";
	
	public static void main(String[] args) throws Exception { 
		
		new File(ODIR).mkdirs();
		PrintWriter out = new PrintWriter(new FileWriter(new File(ODIR+"/result.csv")));
		
		for(CityEvent e : CityEvent.getEventsInData()) {
					
			
			//if(e.spot.name.equals("Juventus Stadium (TO)")) {
			    double bestr = getBestRadius(e);
			    out.println(e+","+bestr);
			    System.out.println(e+","+bestr);
		   // }
		} 
		
		out.close();
		Logger.logln("Done");
	}
	
	
	
	public static double[][] createOrLoadZRadiusDistrib(CityEvent e) throws Exception {
		double[][] zXradius  = null;
		// restore
		File f = new File(ODIR+"/"+e.toFileName()+"/zXradius.ser");
		if(f.exists()) zXradius = (double[][])CopyAndSerializationUtils.restore(f);
		else {
			//create
			File d = new File(ODIR+"/"+e.toFileName());
			if(!d.exists()) d.mkdirs();
			zXradius = getZXRadius(e);
			CopyAndSerializationUtils.save(f, zXradius);
		}
		
		String[] domain = new String[zXradius.length];
		double[] data = new double[zXradius.length];
		for(int i=0; i<domain.length;i++) {
			domain[i] = ""+zXradius[i][0];
			data[i] = zXradius[i][1];
		}
		
		GraphPlotter g = GraphPlotter.drawGraph(e.toString(), e.toFileName(), "z", "radius", "z", domain, data);
		g.save(ODIR+"/"+e.toFileName()+"/z_dist.png");
		
		return zXradius;
	}
	
	
	public static double getBestRadius(CityEvent e) throws Exception {
		
		File f = new File(ODIR+"/"+e.toFileName());
		if(!f.exists()) f.mkdirs();
		
		double[][] zXradius = createOrLoadZRadiusDistrib(e);
		
		return getWeightedAverageWithThreshold(zXradius);
		
	}
	
	
	public static double getWeightedAverage(double[][] zXradius) {
		double avg_r = 0;
		double cont = 1; // kind of laplace smoothing
		for(int i=0; i<zXradius.length;i++) {
				avg_r = avg_r + zXradius[i][0] * zXradius[i][1];
				cont = cont + zXradius[i][1];
		}
		avg_r = PlacemarkRadiusExtractor.round(avg_r / cont);
			
		return avg_r;
	}
	
	public static double getWeightedAverageWithThreshold(double[][] zXradius) {
		double th = 1;
		double avg_r = 0;
		double cont = 1; // kind of laplace smoothing
		for(int i=0; i<zXradius.length;i++) {
			if(zXradius[i][1] > th) {
				avg_r = avg_r + zXradius[i][0] * zXradius[i][1];
				cont = cont + zXradius[i][1];
			}
		}
		avg_r = PlacemarkRadiusExtractor.round(avg_r / cont);
			
		return avg_r;
	}
	
	public static double getMax(double[][] zXradius) {
		double maxR = zXradius[0][0];
		double maxZ = zXradius[0][1];
		for(int i=1; i<zXradius.length;i++) {
			if(zXradius[i][1] > maxZ) {
				maxR = zXradius[i][0];
				maxZ = zXradius[i][1];
			}
		}
		return maxR;
	}
	
	
	
	public static double[][] getZXRadius(CityEvent e) throws Exception {
		
		Placemark p = e.spot;
		p.changeRadius(MAX_R);
		
		String file = Config.getInstance().base_dir+"/PLSEventsAroundAPlacemark/"+p.name+"_"+p.radius+".txt";
		File f = new File(file);
		if(!f.exists()) {
			Logger.logln(file+" does not exist");
			Logger.logln("Executing PLSEventsAroundAPlacemark.process()");
			PLSEventsAroundAPlacemark.process(p);
		}
		
		double[][] zXradius = new double[1+(MAX_R - MIN_R)/STEP][2];
		int index = 0;
		for(int max_r = MAX_R; max_r >= MIN_R; max_r = max_r - STEP) {
			
			PLSMap plsmap = PlacemarkRadiusExtractor.getPLSMap(file,p,max_r);
			
			double max_z = 0;
			double sum_z = 0;
			double h = 0;
			
			if(plsmap.startTime != null) {
				
			
				DescriptiveStatistics[] stats = PLSBehaviorInAnArea.getStats(plsmap);
				//double[] z_pls_data = PLSBehaviorInAnArea.getZ(stats[0],plsmap.startTime);
				double[] z_usr_data =  PLSBehaviorInAnArea.getZ(stats[1],plsmap.startTime);
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
						h ++;
					}
						
					cal.add(Calendar.HOUR_OF_DAY, 1);
				}
			
			}
			zXradius[index][0] = max_r;
			//zXradius[index][1] = sum_z/h;
			zXradius[index][1] = max_z;
			index++;
		}
		
		
		return zXradius;		
	}
	
	
	public static Map<String,Double> readBestR() throws Exception {
		Map<String,Double> best = new HashMap<String,Double>();
		BufferedReader br = new BufferedReader(new FileReader(new File(Config.getInstance().base_dir+"/PlacemarkRadiusExtractorIndividualEvent/result.csv")));
		String line;
		while((line = br.readLine())!=null) {
			String[] e = line.split(",");
			best.put(e[0], Double.parseDouble(e[1]));
		}
		br.close();
		return best;
	}
	
}
