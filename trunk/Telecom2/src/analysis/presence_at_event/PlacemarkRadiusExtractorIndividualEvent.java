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
	
	public static final String ODIR = Config.getInstance().base_dir+"/PlacemarkRadiusExtractorIndividualEvent/"+Config.getInstance().get_pls_subdir();
	
	public static void main(String[] args) throws Exception { 
		
		new File(ODIR).mkdirs();
		
		
		if(new File(ODIR+"/result.csv").exists()) {
			System.err.println(ODIR+"/result.csv already exists!!!!!");
			System.err.println("Manually remove the file before proceeding!");
			System.exit(0);
		}
		
		PrintWriter out = new PrintWriter(new FileWriter(new File(ODIR+"/result.csv")));
		
		for(CityEvent e : CityEvent.getEventsInData()) {
			    double bestr = getBestRadius(e);
			    out.println(e+","+bestr);
			    Logger.logln(e+","+bestr);
		} 
		
		out.close();
		Logger.logln("Done");
	}
	
	
	public static Map<String,Double> readBestR() throws Exception {
		return PlacemarkRadiusExtractor.readBestR(ODIR+"/result.csv");
	}
	
	public static double getBestRadius(CityEvent e) throws Exception {
		
		File f = new File(ODIR+"/"+e.toFileName());
		if(!f.exists()) f.mkdirs();
		
		double[][] zXradius = createOrLoadZRadiusDistrib(e);
		
		return getWeightedAverageWithThreshold(zXradius,0.5);
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
			zXradius = computeZXRadius(e);
			CopyAndSerializationUtils.save(f, zXradius);
		}
		
		// spatial normalization
		if(zXradius[0][1]==0) zXradius[0][1]=0.0001; // laplace smoothing
		double zarea =  zXradius[0][1];
		for(int i=0; i<zXradius.length;i++) {
			zXradius[i][1] = zXradius[i][1] - zarea;
			if(zXradius[i][1] < 0) zXradius[i][1] = 0;
		}
		
		PlacemarkRadiusExtractor.plot(e.toString(), zXradius, ODIR+"/"+e.toFileName()+"/z_dist.png");
		
		return zXradius;
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
		return zXradius;		
	}


	public static  DescriptiveStatistics[] clone(DescriptiveStatistics[] x) {
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
