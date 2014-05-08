package analysis.tourist.extractGT;

import java.util.List;

import region.Placemark;

public class Transit extends Profile {
	
	public Transit(Placemark placemark) {
		super(placemark);
	}

	boolean check(String user_id, String mnt, int num_pls, int num_days, int days_interval, List<CalCell> list, int tot_days) {
		return isTransit(list);
	}	
	
	
	private boolean isTransit(List<CalCell> list) {
		CalCell spotted = list.get(0);
		if(placemark.contains(spotted.nc.getName())) return false; 
		if(placemark.contains(list.get(list.size()-1).nc.getName())) return false;
		
		boolean outside_placemark = true;		
		for(int i=1; i<list.size();i++) {
			CalCell cc = list.get(i);
			if(outside_placemark && !placemark.contains(cc.nc.getName())) 
				spotted = cc;
			if(!outside_placemark && !placemark.contains(cc.nc.getName())) {
				int dh = (int)((cc.cal.getTimeInMillis() - spotted.cal.getTimeInMillis()) / (1000 * 3600));
				if(dh > 4) return false;
				spotted = cc;
				outside_placemark = true;
			}
			if(placemark.contains(cc.nc.getName()))
				outside_placemark = false;
		}
		return true;
	}
	
}
