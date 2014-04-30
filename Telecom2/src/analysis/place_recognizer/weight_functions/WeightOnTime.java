package analysis.place_recognizer.weight_functions;

import java.util.Calendar;
import java.util.List;

import utils.FilterAndCounterUtils;
import analysis.PlsEvent;
import analysis.place_recognizer.Cluster;


public class WeightOnTime implements WeightFunction {
	
	private double coeff;
	private double[][] weights;
	
	public WeightOnTime(double coeff, double[][] weights) {
		this.coeff = coeff;
		this.weights = weights;
	}
	
	public void weight(Cluster c) {
				
		double w = 0;
		List<PlsEvent> filtered = FilterAndCounterUtils.filterMultipleEventsInTheSameHour(c.getEvents());
		
		for(PlsEvent e: filtered) {
			Calendar cal = e.getCalendar();
			int day_of_week = cal.get(Calendar.DAY_OF_WEEK) - 1;
			int hour_of_day = cal.get(Calendar.HOUR_OF_DAY);
			w += weights[day_of_week][hour_of_day];
		}
		
		c.addWeight("WeightOnTime",w*coeff);
	}
}
