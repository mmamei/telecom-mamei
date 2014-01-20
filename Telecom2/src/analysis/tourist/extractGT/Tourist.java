package analysis.tourist.extractGT;



import java.util.List;

import area.Placemark;

public class Tourist extends Profile {
	public Tourist(Placemark placemark) {
		super(placemark);
	}

	boolean check(String user_id, String mnt, int num_pls, int num_days, int days_interval, List<CalCell> list, int tot_days) {
		
		boolean is_italian = super.isItalian(mnt);
		
		if(!is_italian && num_days == days_interval && num_days >= 2 && num_days<= 3) return true;
		return false;
		
	}
	
}
