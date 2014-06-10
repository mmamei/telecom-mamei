package analysis.user_place_recognizer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import utils.FilterAndCounterUtils;
import analysis.PLSEvent;


public class Thresholding {
	
	public static final String REF_NETWORK_CELLAC = "4044247943";
	
	public static List<PLSEvent> buildReferenceTower(List<PLSEvent> events, double[][] weights) {
		ArrayList<PLSEvent> refEvents = new ArrayList<PLSEvent>();
		
		for(PLSEvent e: events) {
			Calendar cal = e.getCalendar();
			if(weights[cal.get(Calendar.DAY_OF_WEEK)-1][cal.get(Calendar.HOUR_OF_DAY)] > 0) {
				refEvents.add(new PLSEvent(e.getUsername(),e.getIMSI(),REF_NETWORK_CELLAC,""+e.getTimeStamp()));
			}
		}
		return refEvents;
	}
	
	public static double weight2Threshold(String kind_of_place, int totDays, Cluster ref_events, double wtf) {		
		
		int totDaysRefTower = FilterAndCounterUtils.getNumDays(ref_events.getEvents());
		double maxdays = getMaxDays(kind_of_place,totDays);
		
		int coarse_grained_percent = (int)(100.0*totDaysRefTower/maxdays);
		
		if(coarse_grained_percent < 20) return Double.MAX_VALUE; // too few events abort the operation
		
		double th = 0;
		if(kind_of_place.startsWith("HOME")) 
			th = ref_events.totWeight()  * 3.0/7.0 * totDays / totDaysRefTower;
		else if(kind_of_place.startsWith("WORK")) 
			th = ref_events.totWeight() * 2.0/5.0 * totDays / totDaysRefTower;
		else if(kind_of_place.startsWith("GENERIC")) 
			th = ref_events.totWeight() * 1.0/14.0 * totDays / totDaysRefTower;
		else if(kind_of_place.startsWith("NIGHT")) 
			th = ref_events.totWeight() * 1.0/14.0 * totDays / totDaysRefTower;
		else 
			th = ref_events.totWeight() * 1.0/14.0 * totDays / totDaysRefTower;
		
		return th * wtf;
	}
	
	
	public static double getMaxW(double[][] w, int totDays) {
		double tot = 0;
		for(int d=0; d<w.length;d++) 
		for(int h=0; h<w[d].length;h++)
			if(w[d][h] > 0)
			tot += w[d][h];
		return tot / 7 * totDays;
	}
	
	public static double getMaxDays(String kind_of_place, int totDays) {
		
		double days = 0;
			
		if(kind_of_place.startsWith("HOME")) 
			days = 7;
		else if(kind_of_place.startsWith("WORK")) 
			days = 5;
		else if(kind_of_place.startsWith("FRIDAY_NIGHT")) 
			days = 1;
		else if(kind_of_place.startsWith("SATURDAY_NIGHT")) 
			days = 1;
		else if(kind_of_place.startsWith("SUNDAY")) 
			days = 1;
		else if(kind_of_place.startsWith("NIGHT")) 
			days = 7;
		else if(kind_of_place.startsWith("GENERIC")) 
			days = 1;
		
		return days / 7 * totDays;
	}	
}
