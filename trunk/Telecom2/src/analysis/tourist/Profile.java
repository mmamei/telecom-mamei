package analysis.tourist;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import region.Placemark;

public abstract class Profile {
	static final SimpleDateFormat F = new SimpleDateFormat("dd/MM/yyyy");
	static final String[] DAY_WEEK = new String[]{"0","Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
	
	abstract boolean check(String user_id, String mnt, int num_pls, int num_days, int days_interval, List<CalCell> list, int tot_days);
	
	Placemark placemark;
	
	public Profile(Placemark placemark) {
		this.placemark = placemark;
	}
	
	
	
	public boolean isItalian(String mnt) {
		return mnt.startsWith("222");
	}
	
	
	public int countDays(List<CalCell> list, int cal_field, int[] interval) {
		Set<String> days = new HashSet<String>();
		for(CalCell cc: list) {
			Calendar c = cc.cal;
			int crit = c.get(cal_field);
			String day = F.format(c.getTime());
			if(placemark.contains(cc.nc.getName()) && contains(interval,crit)) days.add(day);
		}
		return days.size();
	}
	
	private boolean contains(int[] x, int y) {
		for(int i: x)
			if(i == y)
				return true;
		return false;
	}
}
