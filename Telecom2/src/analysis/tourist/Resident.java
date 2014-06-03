package analysis.tourist;

import java.util.Calendar;
import java.util.List;

import region.Placemark;



public class Resident extends Profile {
	
	public Resident(Placemark placemark) {
		super(placemark);
	}

	private static final double FR = 0.3;
	
	boolean check(String user_id, String mnt, int num_pls, int num_days, int days_interval, List<CalCell> list, int tot_days) {
		
		boolean is_italian = super.isItalian(mnt);
		boolean has_enough_days = (1.0 * num_days / tot_days) > FR;
	
		int num_weekends = countDays(list,Calendar.DAY_OF_WEEK,new int[]{Calendar.SATURDAY,Calendar.SUNDAY});
		boolean has_enough_weekends =  (1.0 * num_weekends / tot_days) > (FR*2/7);
		
		int num_nights = countDays(list,Calendar.HOUR_OF_DAY,new int[]{21,22,23,0,1,2,3,4,5,6});
		boolean has_enough_nights =  (1.0 * num_nights / tot_days) > (0.1 * FR);
		
		return is_italian && has_enough_days && has_enough_weekends && has_enough_nights;
	}	
}
