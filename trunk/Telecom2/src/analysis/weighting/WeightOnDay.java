package analysis.weighting;

import utils.FilterAndCounterUtils;
import analysis.place_recognizer.Cluster;

public class WeightOnDay implements WeightFunction {
	
	private double coeff;
	
	public WeightOnDay(double coeff) {
		this.coeff = coeff;
	}
	
	public void weight(Cluster c) {
		if(c.getWeight("WeightOnTime") < 0) c.addWeight("WeightOnDay",0);
		else c.addWeight("WeightOnDay", coeff*FilterAndCounterUtils.getNumDays(c.getEvents()));
	}
}
