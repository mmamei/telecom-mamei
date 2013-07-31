package utils;

import java.util.Random;

import org.apache.commons.math.distribution.NormalDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.inference.TestUtils;

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
		
		NormalDistribution nd = new NormalDistributionImpl();
		
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
	
}
