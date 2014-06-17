package analysis;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import dataset.file.UsersCSVCreator;
import region.CityEvent;
import utils.Config;
import utils.Logger;
import visual.java.GraphPlotter;

public class InterEventDistribution {
	public static void main(String[] args) throws Exception {
		process(Config.getInstance().base_folder+"/UsersCSVCreator/file_pls_piem_users_200_10000",null,null);
	}
	
	public static void process(String dir, Calendar startTime, Calendar endTime) throws Exception {
		
		File fd = new File(dir);
		if(!fd.exists()) {
			Logger.logln(dir+" does not exist");
			System.exit(0);
		}		
		
		int startH = 0;
		int endH = 24;
		
		
		if(startTime != null && endTime !=null) {
			startH = startTime.get(Calendar.HOUR_OF_DAY);
			endH = endTime.get(Calendar.HOUR_OF_DAY);
		}
		
		DescriptiveStatistics stats = new DescriptiveStatistics();
		DescriptiveStatistics per_user_first_q = new DescriptiveStatistics();
		DescriptiveStatistics per_user_median = new DescriptiveStatistics();
		DescriptiveStatistics per_user_third_q = new DescriptiveStatistics();
		
		File[] files = new File(dir).listFiles();
		for(int i=0; i<files.length; i++){
			File f = files[i];	
			if(!f.isFile()) continue;
			
			List<PLSEvent> events = PLSEvent.readEvents(f);
			
			DescriptiveStatistics ustats = new DescriptiveStatistics();
			for(int j=1;j<events.size();j++) {
				
				int d1 = events.get(j).getCalendar().get(Calendar.DAY_OF_YEAR);
				int d2 = events.get(j-1).getCalendar().get(Calendar.DAY_OF_YEAR);
				
				int h1 = events.get(j).getCalendar().get(Calendar.HOUR_OF_DAY);
				int h2 = events.get(j-1).getCalendar().get(Calendar.HOUR_OF_DAY);
				
				if(h1 < startH || h1 > endH) continue;
				if(h2 < startH || h2 > endH) continue;
				
				if(d1 != d2) continue;
				
				if(startTime != null && endTime !=null) {
					if(events.get(j).getCalendar().before(startTime) || events.get(j).getCalendar().after(endTime)) continue;
					if(events.get(j-1).getCalendar().before(startTime) || events.get(j-1).getCalendar().after(endTime)) continue;
				}
				double dt = (1.0 * (events.get(j).getTimeStamp() - events.get(j-1).getTimeStamp())/60000);
				if(dt <= 0) {
					System.err.println("Warning:");
					System.err.println("-- "+events.get(j).getTimeStamp());
					System.err.println("-- "+events.get(j-1).getTimeStamp());
				}
				stats.addValue(dt);
				ustats.addValue(dt);
			}
			if(ustats.getN() == 0) continue;
			
			per_user_first_q.addValue(ustats.getPercentile(25));
			per_user_median.addValue(ustats.getPercentile(50));
			per_user_third_q.addValue(ustats.getPercentile(75));
		}
		
		System.err.println("MEAN OF MEDIAN = "+per_user_median.getMean());
		System.err.println("GEOM. MEAN OF MEDIAN = "+per_user_median.getGeometricMean());
		
		Logger.logln("Gobal population resutls:");
		for(int i=10;i<100;i+=10)
			Logger.logln(i+"th Percentile = "+(int)stats.getPercentile(i)+" mins");
		
		
		Logger.logln("Per user median interevent time distribution:");
		for(int i=10;i<100;i+=10)
			Logger.logln(i+"th Percentile = "+(int)per_user_median.getPercentile(i)+" mins");
		
		
		double[] xaxis = new double[100];
		for(int i=0; i<xaxis.length;i++)
			xaxis[i] = i;
		
		String[] labels = new String[xaxis.length];
		for(int i = 0; i<xaxis.length; i++)
			labels[i] = String.valueOf((int)xaxis[i]);
		
		
		new File(Config.getInstance().base_folder+"/InterEventDistribution").mkdirs();
		PrintWriter out = new PrintWriter(new FileWriter(new File(Config.getInstance().base_folder+"/InterEventDistribution/InterEventDistrib_"+fd.getName()+".csv")));
		
		out.print("xlabels");
		for(String l: labels)
			out.print(","+l);
		out.println();
		
		
		double[] h = hist(per_user_first_q.getSortedValues(),xaxis);
		out.print("1st q");
		for(double l: h)
			out.print(","+l);
		out.println();
		
		
		GraphPlotter gp = GraphPlotter.drawGraph("Distribution of interevent time", "Distribution of interevent time", 
												 "1st quartile", "interevent time (mins)", "fraction of users", 
											     labels, h);
		
		h = hist(per_user_median.getSortedValues(),xaxis);
		out.print("median");
		for(double l: h)
			out.print(","+l);
		out.println();
		gp.addData("median",h);
		
		h = hist(per_user_third_q.getSortedValues(),xaxis);
		out.print("3rd q");
		for(double l: h)
			out.print(","+l);
		out.println();
		gp.addData("3rd quartile",h);
		
		
		xaxis = new double[10];
		for(int i=0; i<xaxis.length;i++)
			xaxis[i] = 10*i;
		labels = new String[xaxis.length];
		for(int i = 0; i<xaxis.length; i++)
			labels[i] = String.valueOf((int)xaxis[i]);
		
		GraphPlotter gp2 = GraphPlotter.drawGraph("Distribution of interevent time", "Distribution of interevent time", 
				 "all", "interevent time (mins)", "fraction of users", 
			     labels, hist(stats.getSortedValues(),xaxis));
		
		out.close();
		Logger.logln("Done!");
	}
	
	
	public static void log(double[] h) {
		for(int i=0; i<h.length-1;i++) 
			Logger.log(h[i]+",");
		Logger.logln(String.valueOf(h[h.length-1]));
	}
	
	
	public static double[] hist(double[] sortedvalues, double[] x) {
		
		double[] h = new double[x.length];
		for(int i=0;i<x.length;i++) 
		for(int j=0; j<sortedvalues.length && sortedvalues[j] <= x[i]; j++) 
				h[i] ++;
		
		for(int i=h.length-1; i > 0; i--)
			h[i] = (h[i] - h[i-1])/sortedvalues.length;
		h[0] = h[0] / sortedvalues.length;
		
		return h;
	}
}
