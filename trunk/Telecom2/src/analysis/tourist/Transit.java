package analysis.tourist;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

import region.Placemark;
import visual.kml.KML;

public class Transit extends Profile {
	
	public Transit(Placemark placemark) {
		super(placemark);
	}

	boolean check(String user_id, String mnt, int num_pls, int num_days, int days_interval, List<CalCell> list, int tot_days) {
		
		// check
		/*
		if(user_id.equals("ea6784f58c5f54d2912f33df8b2012be91103bcb4028c2e3761b3ea4d5a33a86")) {
			for(CalCell cc: list) {
				try {
					placemark.printKML("test.kml");
					PrintWriter out = new PrintWriter(new FileWriter("test2.kml"));
					KML kml = new KML();
					kml.printHeaderDocument(out, cc.nc.getName());
					out.println(cc.nc.toKml("#ff0000ff"));
					kml.printFooterDocument(out);
					out.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(cc.cal.getTime()+" --> "+placemark.contains(cc.nc.getName())+", ("+cc.nc.getLatLon()[0]+","+cc.nc.getLatLon()[1]+")");
			}
			System.out.println(isTransit(list));
			System.exit(0);
		}
		*/
		return isTransit(list);
	}	
	
	
	private boolean isTransit(List<CalCell> list) {
		CalCell spotted = list.get(0);
		if(placemark.contains(spotted.nc.getName())) return false; 
		if(placemark.contains(list.get(list.size()-1).nc.getName())) return false;
		
		boolean outside_placemark = true;		
		for(int i=1; i<list.size();i++) {
			CalCell cc = list.get(i);
			if(outside_placemark && !placemark.contains(cc.nc.getName())) // we are still out
				spotted = cc;
			if(!outside_placemark && !placemark.contains(cc.nc.getName())) { // we exit
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
