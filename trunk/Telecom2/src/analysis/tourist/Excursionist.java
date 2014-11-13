package analysis.tourist;



import java.util.Calendar;
import java.util.List;

import region.Placemark;
import analysis.PLSEvent;

public class Excursionist extends Profile {
	
	private Transit transit;
	
	public Excursionist(Placemark placemark) {
		super(placemark);
		transit = new Transit(placemark);
	}

	boolean check(String user_id, String mnt, int num_pls, int num_days, int days_interval, List<PLSEvent> list, int tot_days) {
		
		boolean is_italian = super.isItalian(mnt);
		
		int days_in_area = countDays(list,Calendar.DAY_OF_MONTH,null);
		boolean in_transit = transit.check(user_id, mnt, num_pls, num_days, days_interval, list, tot_days);
		if(is_italian && days_in_area == 1 && !in_transit) return true;
		return false;
		
	}
	
}
