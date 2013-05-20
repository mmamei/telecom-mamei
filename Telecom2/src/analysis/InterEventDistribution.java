package analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import pls_parser.UsersCSVCreator;

import area.CityEvent;

import utils.Config;
import utils.Logger;
import visual.GraphPlotter;

public class InterEventDistribution {
	public static void main(String[] args) throws Exception {
		CityEvent ce = CityEvent.getEvent("Stadio Silvio Piola (NO),11/03/2012");
		process(ce);
	}
	
	public static void process(CityEvent e) throws Exception {
		
		String dir = Config.getInstance().base_dir+"/UsersCSVCreator/"+e.toString();
		File fd = new File(dir);
		if(!fd.exists()) {
			Logger.logln(dir+" does not exist");
			UsersCSVCreator.create(e);
		}
		else Logger.logln(dir+" already exists");
		
		Calendar startTime = e.st;
		Calendar endTime = e.et;
		int startH = startTime.get(Calendar.HOUR_OF_DAY);
		int endH = endTime.get(Calendar.HOUR_OF_DAY);
		
		DescriptiveStatistics stats = new DescriptiveStatistics();
		DescriptiveStatistics per_user_first_q = new DescriptiveStatistics();
		DescriptiveStatistics per_user_median = new DescriptiveStatistics();
		DescriptiveStatistics per_user_third_q = new DescriptiveStatistics();
		
		File[] files = new File(dir).listFiles();
		for(int i=0; i<files.length; i++){
			File f = files[i];	
			if(!f.isFile()) continue;
			
			String filename = f.getName();
			List<PlsEvent> events = PlsEvent.readEvents(f);
			
			DescriptiveStatistics ustats = new DescriptiveStatistics();
			for(int j=1;j<events.size();j++) {
				
				int h1 = events.get(j).getCalendar().get(Calendar.HOUR_OF_DAY);
				int h2 = events.get(j-1).getCalendar().get(Calendar.HOUR_OF_DAY);
				
				if(h1 < startH || h1 > endH) continue;
				if(h2 < startH || h2 > endH) continue;
				
				int dt = (int)((events.get(j).getTimeStamp() - events.get(j-1).getTimeStamp())/60000);
				stats.addValue(dt);
				ustats.addValue(dt);
			}
			
			if(ustats.getN() == 0) continue;
			
			per_user_first_q.addValue(ustats.getPercentile(25));
			per_user_median.addValue(ustats.getPercentile(50));
			per_user_third_q.addValue(ustats.getPercentile(75));
		}
		
		Logger.logln("Gobal population resutls:");
		for(int i=10;i<100;i+=10)
			Logger.logln(i+"th Percentile = "+(int)stats.getPercentile(i)+" mins");
		
		
		Logger.logln("Per user median interevent time distribution:");
		for(int i=10;i<100;i+=10)
			Logger.logln(i+"th Percentile = "+(int)per_user_median.getPercentile(i)+" mins");
		
		GraphPlotter gp = GraphPlotter.drawGraph("Distribution of interevent time", "Distribution of interevent time", 
												 "1st quartile", "log_10(interevent time) mins", "fraction of users", 
											     new String[]{"0.1","1","10","100","1000","1000"}, hist(per_user_first_q.getSortedValues()));
		gp.addData("median",hist(per_user_median.getSortedValues()));
		gp.addData("3rd quartile",hist(per_user_third_q.getSortedValues()));
		
		Logger.logln("Done!");
	}
	
	
	public static void log(double[] h) {
		for(int i=0; i<h.length-1;i++) 
			Logger.log(h[i]+",");
		Logger.logln(String.valueOf(h[h.length-1]));
	}
	
	public static double[] hist(double[] sortedvalues) {
			
		double[] h = new double[6];
		for(int i=0;i<h.length;i++) 
		for(int j=0; j<sortedvalues.length && sortedvalues[j] <= Math.pow(10, i-1); j++) 
				h[i] ++;
		
		for(int i=h.length-1; i > 0; i--)
			h[i] = (h[i] - h[i-1])/sortedvalues.length;
		
		return h;
	}
}
