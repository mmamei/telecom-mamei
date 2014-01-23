package analysis.tourist.extractGT;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import area.Placemark;
import network.NetworkCell;
import network.NetworkMap;
import network.NetworkMapFactory;
import utils.FileUtils;
import utils.Logger;

public class Transit extends Profile {
	
	public Transit(Placemark placemark) {
		super(placemark);
	}

	boolean check(String user_id, String mnt, int num_pls, int num_days, int days_interval, List<CalCell> list, int tot_days) {
		if(user_id.equals("736c7a483ea0c5882241e7b4751e6f51e9a381c05823ad7234a8fb6b3ca997"))
			System.out.println("here");
		
		return isTransit(list);
	}	
	
	
	private boolean isTransit(List<CalCell> list) {
		CalCell spotted = list.get(0);
		if(placemark.contains(spotted.nc.getCellac())) return false; 
		if(placemark.contains(list.get(list.size()-1).nc.getCellac())) return false;
		
		boolean outside_placemark = true;		
		for(int i=1; i<list.size();i++) {
			CalCell cc = list.get(i);
			if(outside_placemark && !placemark.contains(cc.nc.getCellac())) 
				spotted = cc;
			if(!outside_placemark && !placemark.contains(cc.nc.getCellac())) {
				int dh = (int)((cc.cal.getTimeInMillis() - spotted.cal.getTimeInMillis()) / (1000 * 3600));
				if(dh > 4) return false;
				spotted = cc;
				outside_placemark = true;
			}
			if(placemark.contains(cc.nc.getCellac()))
				outside_placemark = false;
		}
		return true;
	}
	
}
