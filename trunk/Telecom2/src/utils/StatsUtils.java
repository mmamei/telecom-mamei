package utils;

import java.util.Random;

import org.apache.commons.math.distribution.NormalDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;
import org.apache.commons.math.stat.inference.TestUtils;

public class StatsUtils {
	
	
	public static void main(String[] args) {
		double[] z = new double[10000];
		Random r = new Random();
		for(int i=0; i<z.length;i++)
			z[i] = r.nextGaussian();
		checkNormalDistrib(z,true);
	}

	
	
	
	public static boolean checkNormalDistrib(double[] z, boolean verbose) {
		
		double[] sets = new double[]{Double.NEGATIVE_INFINITY,-2,-1,0,1,2,Double.POSITIVE_INFINITY};
		
		NormalDistribution nd = new NormalDistributionImpl();
		
		long[] count = new long[sets.length-1];
		for(double x: z) {
			for(int i=0;i<sets.length-1;i++) 
				if(sets[i] < x && x <= sets[i+1])
					count[i]++;
		}
		
		double[] expected_count = new double[count.length];
		try {
		for(int i=0;i<sets.length-1;i++) 
			expected_count[i] = nd.cumulativeProbability(sets[i], sets[i+1]) * z.length; 
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		if(verbose) {
			Logger.logln("["+toString(count,false)+"] should be ["+toString(expected_count,false)+"]");
			Logger.logln("["+toString(count,true)+"] should be ["+toString(expected_count,true)+"]");
		}
		
		boolean test = false;
		
		try {
			double p_value = TestUtils.chiSquareTest(expected_count, count);
			test = p_value >= 0.05;
			if(verbose) {
				if(!test) System.out.println("Reject H0: Data does not come from N(0,1)");
				if(test) System.out.println("Accept H0: Data comes from N(0,1)");
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
	
	private static String toString(long[] x, boolean perc) {
		
		double sum=0;
		for(int i=0; i<x.length;i++)
			sum += x[i];
		
		String s = "";
		for(int i=0; i<x.length;i++) 
			s = s + "," + (perc? (int)(100*x[i]/sum)+"%" : (int)x[i]);
		return s.substring(1);
	}
	
}
