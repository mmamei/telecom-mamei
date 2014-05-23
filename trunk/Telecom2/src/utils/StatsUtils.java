package utils;

import java.util.Calendar;
import java.util.Random;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.TestUtils;

import visual.java.GraphPlotter;

public class StatsUtils {
	
	
	public static void main(String[] args) {
		double[] z = new double[100000];
		Random r = new Random();
		for(int i=0; i<z.length;i++)
			z[i] = r.nextGaussian();
		checkNormalDistrib(z,true);
		
		DescriptiveStatistics ds = new DescriptiveStatistics();
		for(int i=0; i<z.length;i++)
			ds.addValue(r.nextDouble());
		
		double m = ds.getMean();
		double s = ds.getStandardDeviation();
		z = ds.getValues();
		for(int i=0; i<z.length;i++)
			z[i] = (z[i] - m)/s;
		checkNormalDistrib(z,true);
	}

	
	
	/*
	 * This function used a Chi2-test to see whether z comes from N(0,1) o not
	 */
	
	public static boolean checkNormalDistrib(double[] z, boolean verbose) {
		return checkNormalDistrib(z,verbose,"Distribution");
	}
	public static boolean checkNormalDistrib(double[] z, boolean verbose, String title) {
		
		double[] sets = new double[]{Double.NEGATIVE_INFINITY,-3,-2,-1,0,1,2,3,Double.POSITIVE_INFINITY};
		
		NormalDistribution nd = new NormalDistribution();
		
		long[] count = new long[sets.length-1];
		for(int j=0; j<z.length;j++) {
			for(int i=0;i<sets.length-1;i++) 
				if(sets[i] < z[j] && z[j] <= sets[i+1]) {
					count[i]++;
					break;
				}
		}
		double[] dcount = toDouble(count);
		double[] expected_count = new double[count.length];
		try {
		for(int i=0;i<sets.length-1;i++) 
			expected_count[i] = nd.cumulativeProbability(sets[i], sets[i+1]) * z.length; 
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		if(verbose) {
			Logger.logln("["+toString(dcount,false)+"] should be ["+toString(expected_count,false)+"]");
			Logger.logln("["+toString(dcount,true)+"] should be ["+toString(expected_count,true)+"]");
		}
		
		boolean test = false;
		
		try {
			double p_value = TestUtils.chiSquareTest(expected_count, count);
			test = p_value >= 0.05;
			if(verbose) {
				if(!test) System.out.println("Reject H0: Data does not come from N(0,1)");
				if(test) System.out.println("Accept H0: Data comes from N(0,1)");
				
				String[] domain = new String[count.length];
				for(int i=0; i<domain.length;i++)
					domain[i] = sets[i]+"_"+sets[i+1];
				
				GraphPlotter g = GraphPlotter.drawGraph(title, title, "count", "x", "data", domain, dcount);
				g.addData("n(0,1)", expected_count);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return test;
	}
	
	private static String toString(double[] x, boolean perc) {
		
		double sum=0;
		for(int i=0; i<x.length;i++)
			sum += x[i];
		
		String s = "";
		for(int i=0; i<x.length;i++) {
			s = s + "," + (perc? (int)(100*x[i]/sum)+"%" : (int)x[i]);
		}
		return s.substring(1);
	}
	
	private static double[] toDouble(long[] x) {
		double[] y = new double[x.length];
		for(int i=0; i<x.length;i++)
			y[i] = x[i];
		return y;
	}
	
	

	public static double[] getZH(DescriptiveStatistics stat, Calendar startTime) {
		
		DescriptiveStatistics[] hstats = new DescriptiveStatistics[24];
		for(int i=0; i<hstats.length;i++)
			hstats[i] = new DescriptiveStatistics();
		
		
		Calendar cal = (Calendar)startTime.clone();
		double[] vals = stat.getValues();
		for(int i=0; i<vals.length;i++) {
				hstats[cal.get(Calendar.HOUR_OF_DAY)].addValue(vals[i]);
			cal.add(Calendar.HOUR_OF_DAY, 1);
		}
		
		double[] hmeans = new double[24];
		double[] hsigmas = new double[24];
		
		for(int i=0; i<hstats.length;i++) {
			hmeans[i] = hstats[i].getMean();
			hsigmas[i] = hstats[i].getStandardDeviation();
		}
		
		
		double[] z = stat.getValues();
		
		
		cal = (Calendar)startTime.clone();
		for(int i=0; i<vals.length;i++) {
			
			if( hsigmas[cal.get(Calendar.HOUR_OF_DAY)] == 0)
				z[i] = 0;
			else
				z[i] = (z[i] - hmeans[cal.get(Calendar.HOUR_OF_DAY)]) / hsigmas[cal.get(Calendar.HOUR_OF_DAY)];
			cal.add(Calendar.HOUR_OF_DAY, 1);
		}
		
		
		for(int i=0; i<z.length;i++) {
			if(z[i] < 0) z[i] = 0;
		}
		return z;
	}
	
	

	
	public static double[] getZ(DescriptiveStatistics stat, Calendar startTime) {
		
		DescriptiveStatistics stat2 = new DescriptiveStatistics();
		Calendar cal = (Calendar)startTime.clone();
		double[] vals = stat.getValues();
		for(int i=0; i<vals.length;i++) {
			if(cal.get(Calendar.HOUR_OF_DAY) > 10 && vals[i] > 0)
				stat2.addValue(vals[i]);
			cal.add(Calendar.HOUR_OF_DAY, 1);
		}
		
		double mean = stat2.getMean();
		double sigma = stat2.getStandardDeviation();
		double[] z = stat.getValues();
		for(int i=0; i<z.length;i++) {
			z[i] = (z[i] - mean) / sigma;
			if(z[i] < 0) z[i] = 0;
		}
		return z;
	}
	
}
