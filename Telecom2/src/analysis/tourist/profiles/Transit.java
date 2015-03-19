package analysis.tourist.profiles;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

import region.Placemark;
import visual.kml.KML;
import analysis.PLSEvent;

public class Transit extends Profile {
	
	public Transit(Placemark placemark) {
		super(placemark);
	}

	public boolean check(String user_id, String mnt, int num_pls, int num_days, int days_interval, List<PLSEvent> list, int tot_days) {
		/*
		// check
		String user = "5b1728db7737cbaa4a14feaf1e8ba2bb6a8f68972f8ae4938603c6571f4364f";
		if(user_id.equals(user)) {
			 {
				try {
					placemark.printKML("test.kml");
					PrintWriter out = new PrintWriter(new FileWriter("test2.kml"));
					KML kml = new KML();
					kml.printHeaderDocument(out, user);
					for(PLSEvent cc: list)
						out.println(cc.toKml());
					kml.printFooterDocument(out);
					out.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//System.out.println(cc.cal.getTime()+" --> "+placemark.contains(cc.nc.getName())+", ("+cc.nc.getLatLon()[0]+","+cc.nc.getLatLon()[1]+")");
			}
			System.out.println(isTransit(list));
			System.exit(0);
		}
		*/
		
		int maxdh = maxTimeInPlacemark(list);
		//System.out.println(maxdh);
		return maxdh != -1 && maxdh < 4;
	}	
	
	/*
	private boolean isTransit(List<PLSEvent> list) {
		PLSEvent spotted = list.get(0);
		if(placemark.contains(spotted.getCellac())) return false; 
		if(placemark.contains(list.get(list.size()-1).getCellac())) return false;
		
		int num_entry = 0;
		boolean outside_placemark = true;		
		for(int i=1; i<list.size();i++) {
			PLSEvent cc = list.get(i);
			if(outside_placemark && !placemark.contains(cc.getCellac())) // we are still out
				spotted = cc;
			if(!outside_placemark && !placemark.contains(cc.getCellac())) { // we exit
				int dh = (int)((cc.getCalendar().getTimeInMillis() - spotted.getCalendar().getTimeInMillis()) / (1000 * 3600));
				if(dh > 4) return false;
				spotted = cc;
				outside_placemark = true;
			}
			if(outside_placemark && placemark.contains(cc.getCellac())) {
				outside_placemark = false;
				num_entry ++;
			}
		}
		return num_entry > 0;
	}
	*/
	
	
	public int maxTimeInPlacemark(List<PLSEvent> list) {
		PLSEvent spotted = list.get(0);
		if(placemark.contains(spotted.getCellac())) return -1; 
		if(placemark.contains(list.get(list.size()-1).getCellac())) return -1;
		
		int num_entry = 0;
		boolean outside_placemark = true;		
		int maxdh = -1;
		for(int i=1; i<list.size();i++) {
			PLSEvent cc = list.get(i);
			if(outside_placemark && !placemark.contains(cc.getCellac())) // we are still out
				spotted = cc;
			if(!outside_placemark && !placemark.contains(cc.getCellac())) { // we exit
				int dh = (int)((cc.getCalendar().getTimeInMillis() - spotted.getCalendar().getTimeInMillis()) / (1000 * 3600));
				if(dh > maxdh) maxdh = dh;
				spotted = cc;
				outside_placemark = true;
			}
			if(outside_placemark && placemark.contains(cc.getCellac())) {
				outside_placemark = false;
				num_entry ++;
			}
		}
		if(num_entry > 0) return maxdh;
		else return -1;
	}
}
