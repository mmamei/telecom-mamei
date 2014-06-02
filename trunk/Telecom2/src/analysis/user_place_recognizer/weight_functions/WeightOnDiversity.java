package analysis.user_place_recognizer.weight_functions;

import java.util.Calendar;

import utils.FilterAndCounterUtils;
import analysis.PLSEvent;
import analysis.user_place_recognizer.Cluster;

public class WeightOnDiversity implements WeightFunction {
	
	private double coeff;
	private double[][] weights;
	
	public WeightOnDiversity(double coeff, double[][] weights) {
		this.coeff = coeff;
		this.weights = weights;
	}
	
	public void weight(Cluster c) {
		if(c.getWeight("WeightOnTime") < 0) {
			c.addWeight("WeightOnDiversity",0);
			return;
		}
		
		
		double[] prob = new double[7];
		double den = 0;
		for(PLSEvent e: c.getEvents()){
			Calendar cal = e.getCalendar();
			double w = weights[cal.get(Calendar.DAY_OF_WEEK)-1][cal.get(Calendar.HOUR_OF_DAY)];
			if(w >= 0) {
				double inc = 1; // w;
				prob[(cal.get(Calendar.DAY_OF_WEEK)-1)] = prob[(cal.get(Calendar.DAY_OF_WEEK)-1)] + inc;
				den = den + inc;
			}
		}
		for(int i=0; i<7; i++){
			if(den > 0)
				prob[i] = prob[i]/den;
		}
		
		double H = 0;
		for(int i=0; i<7; i++){
			if(prob[i] != 0){
				H = H + (prob[i] * log2(prob[i]));
			}
		}
		H = -H;
		H = H * FilterAndCounterUtils.getNumDays(c.getEvents());
		
		c.addWeight("WeightOnDiversity", coeff * H);
	}
	
	private double log2(double x) {
		return Math.log(x) / Math.log(2);
	}
	

}
