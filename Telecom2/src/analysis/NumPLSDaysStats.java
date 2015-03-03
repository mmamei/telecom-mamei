package analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import utils.Config;
import utils.Logger;
import visual.r.RPlotter;



/*
 * This class computes statistics about the number of PLS per user and the number of days (with PLS) per user.
 */
public class NumPLSDaysStats {
	public static void main(String[] args) throws Exception {
		//process(Config.getInstance().base_folder+"/UserEventCounter/file_pls_piem_LDAPOP_cellXHour.csv");
		process(Config.getInstance().base_folder+"/UserEventCounter/file_pls_ve_Venezia_cellXHour_July2013.csv");
	}
	
	public static void process(String cellXHourFile) throws Exception {
		
		DescriptiveStatistics pls_stat = new DescriptiveStatistics();
		DescriptiveStatistics days_stat = new DescriptiveStatistics();
		DescriptiveStatistics plsXday_stat = new DescriptiveStatistics();
		
		BufferedReader br = new BufferedReader(new FileReader(new File(cellXHourFile)));		
		String line;
		while((line=br.readLine())!=null) {
			if(line.startsWith("//")) {Logger.logln(line); continue;}
			
			String[] p = line.split(",");
			
			int num_pls = Integer.parseInt(p[2]);
			int num_days = Integer.parseInt(p[3]);
			
			pls_stat.addValue(num_pls);
			days_stat.addValue(num_days);
			plsXday_stat.addValue(1.0*num_pls/num_days);
		}
		br.close();
		
		
		double[] p = new double[100];
		double[] pls_cdf = new double[100];
		double[] days_cdf = new double[100];
		double[] plsxday_cdf = new double[100];
		
		for(int i=1;i<=100;i++) {
			p[i-1] = (double)i/100;
			pls_cdf[i-1] = pls_stat.getPercentile(i);
			days_cdf[i-1] = days_stat.getPercentile(i);
			plsxday_cdf[i-1] = plsXday_stat.getPercentile(i);
		}
		
		RPlotter.drawScatter(pls_cdf, p, "num_pls", "cdf", Config.getInstance().base_folder+"/Images/pls.pdf", "geom_line()");	
		RPlotter.drawScatter(days_cdf, p, "num_days", "cdf", Config.getInstance().base_folder+"/Images/days.pdf", "geom_line()");	   
		RPlotter.drawScatter(plsxday_cdf, p, "plsXday", "cdf", Config.getInstance().base_folder+"/Images/plsXday.pdf", "geom_line()");	   
		System.out.println("Done!");
		
	}
}
