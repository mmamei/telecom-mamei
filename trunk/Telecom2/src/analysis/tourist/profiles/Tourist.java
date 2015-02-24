package analysis.tourist.profiles;



import java.util.Calendar;
import java.util.List;

import region.Placemark;
import analysis.PLSEvent;

public class Tourist extends Profile {
	public Tourist(Placemark placemark) {
		super(placemark);
	}

	public boolean check(String user_id, String mnt, int num_pls, int num_days, int days_interval, List<PLSEvent> list, int tot_days) {
		
		boolean is_italian = super.isItalian(mnt);
		
		int days_in_area = countDays(list,Calendar.DAY_OF_MONTH,null);
		
		if(!is_italian && days_in_area >= 2 && days_in_area<= 3 && 
		   Math.abs(days_interval-days_in_area) < 4 && 
		   Math.abs(num_days-days_in_area) < 4 &&
		   Math.abs(num_days-days_interval) < 4) return true;
		return false;
		
	}
	
}
