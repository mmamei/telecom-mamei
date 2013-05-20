package test;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import visual.GraphPlotter;
import analysis.presence_at_event.PresenceProbability;



public class DynamicTimeWarping {
	private static final DecimalFormat DF = new DecimalFormat("#.##",new DecimalFormatSymbols(Locale.US));
	public static void main(String[] args) {
		
		double[] eventDay = {0,0,0,0,1,0,1,0,0,0};
		double[] eventPat = {0,0,0,0,1,1,0,0,0,0};
		double[] avgPatte = {1,1,0,0,0,1,0,0,0,0};
		
		String[] domain = new String[eventDay.length];
		for(int i=0; i<domain.length;i++)
			domain[i] = String.valueOf(i);
		
		double d1 = dtw(eventDay,eventPat);
		
		GraphPlotter g = GraphPlotter.drawGraph("", "DTW = "+DF.format(d1), "eventDay", "h", "pls", domain, eventDay);
		g.addData("eventPat", eventPat);
		g.addData("avgPatte", avgPatte);
		
	}
	
	public static double dtw(double[] s, double[] t) {
		int n = s.length -1;
		int m = t.length -1;
		double[][] dtw = new double[n+1][m+1];
	    for(int i=0; i<=n;i++) dtw[i][0] = Double.POSITIVE_INFINITY;
	    for(int i=0; i<=m;i++) dtw[0][i] = Double.POSITIVE_INFINITY;
	    dtw[0][0] = 0;
	    for(int i=1; i<=n;i++)
	    for(int j=1; j<=m;j++) 
	    	dtw[i][j] = Math.abs(s[i] - t[j]) + Math.min(dtw[i-1][j], Math.min(dtw[i][j-1],dtw[i-1][j-1]));   
	    return dtw[n][m];
	}
}
