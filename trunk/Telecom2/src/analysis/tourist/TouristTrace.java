package analysis.tourist;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import pls_parser.UserEventCounterCellacXHour;
import utils.FileUtils;
import utils.Logger;
import visual.kml.KMLPath;
import analysis.PlsEvent;

public class TouristTrace {
	
	
	static String city = "Venezia";
	static String[] usr = new String[]{"dc49e6374d9879387cbe4d93117992aaf2e31bfe2b26aaffb17b0dbcacbb380"};
	
	public static void main(String[] args) throws Exception {
		
		//RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(FileUtils.getFile("RegionMap/"+city+".ser"));
		BufferedReader br = FileUtils.getBR("UserEventCounter/"+city+"_cellacXhour.csv");
		if(br == null) {
			UserEventCounterCellacXHour.process(city);
			br = FileUtils.getBR("UserEventCounter/"+city+"_cellacXhour.csv");
		}
		
		String f = FileUtils.getFileS("TouristData");
		KMLPath.openFile(f+"/TouristTrace.kml");
		
		
		String line;
		while((line=br.readLine())!=null) {
			for(String u: usr)
				if(line.startsWith(u))
					process(line);
		}
		
		KMLPath.closeFile();
	}
	//public PlsEvent(String username, String imsi, long cellac, String timestamp){
	public static void process(String line) {
		String[] el = line.split(",");
		String username = el[0];
		String imsi = el[1];
		
		Logger.logln(username+","+imsi);
		
		List<PlsEvent> l = new ArrayList<PlsEvent>();
		for(int i=5;i<el.length;i++) {
			String[] pls = el[i].split(":"); // 2013-3-27:Sat:19:1972908327
			String[] ymd = pls[0].split("-");
			int y = Integer.parseInt(ymd[0]);
			int m = Integer.parseInt(ymd[1]);
			int d = Integer.parseInt(ymd[2]);
			int h = Integer.parseInt(pls[2]);
			Calendar cal = new GregorianCalendar(y,m,d,h,0,0);
			String timestamp = ""+cal.getTimeInMillis();
			long celllac = Long.parseLong(pls[3]);
			PlsEvent pe = new PlsEvent(username,imsi,celllac,timestamp);
			l.add(pe);
			Logger.logln(pe.toString());
		}
		KMLPath.print(username, l);
	}
	
}
