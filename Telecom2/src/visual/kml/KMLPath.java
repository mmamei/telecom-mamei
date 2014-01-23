package visual.kml;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import network.NetworkMap;
import network.NetworkMapFactory;
import utils.Colors;
import utils.Config;
import utils.FilterAndCounterUtils;
import utils.Logger;
import analysis.PlsEvent;

public class KMLPath {
	public static final boolean JITTER = false;
	private static PrintWriter out;
	private static KML kml;
	
	public static void openFile(String file) {
		try {
			out = new PrintWriter(new FileWriter(file));
			kml = new KML();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		kml.printHeaderDocument(out, "trace");
	}
	
	public static void print(String username, List<PlsEvent> plsEvents) {
		kml.printFolder(out, username);
		List<PlsEvent> s = plsEvents;//FilterAndCounterUtils.smooth(plsEvents);
		Map<String,List<PlsEvent>> evPerDay = splitByDay(s);
		
		
		NetworkMap nm =  NetworkMapFactory.getNetworkMap();
		
		kml.printFolder(out, "cells");
		for(String day: evPerDay.keySet()) {
			kml.printFolder(out, day);
			for(PlsEvent pe: evPerDay.get(day))
				out.println(nm.get(pe.getCellac()).toKml());
			kml.closeFolder(out);
		}
		kml.closeFolder(out);
		
		kml.printFolder(out, "paths");
		int color_index = 0;
		
		
		for(String day: evPerDay.keySet()) {
			kml.printFolder(out, day);
			for(int i=0; i<evPerDay.get(day).size()-1;i++) {	
				PlsEvent pe = evPerDay.get(day).get(i);
				PlsEvent pe1 = evPerDay.get(day).get(i+1);
				int dmin =  (int)((pe1.getTimeStamp() - pe.getTimeStamp()) / 60000);
				if(dmin < 180) {
					double lon1 = nm.get(pe.getCellac()).getBarycentreLongitude() + jitter(pe);
					double lat1 = nm.get(pe.getCellac()).getBarycentreLatitude() + jitter(pe);
					double lon2 = nm.get(pe1.getCellac()).getBarycentreLongitude() + jitter(pe1);
					double lat2 = nm.get(pe1.getCellac()).getBarycentreLatitude() + jitter(pe1);
					//out.println(KMLArrow.printArrow(lon1, lat1, lon2, lat2, 2, Colors.RANDOM_COLORS[color_index],true));
					out.println(KMLArrowCurved.printArrow(lon1, lat1, lon2, lat2, 2, Colors.RANDOM_COLORS[color_index],true));
				}
				else {
					color_index ++;
					if(color_index >= Colors.RANDOM_COLORS.length) color_index = 0;
				}
			}
			color_index ++;
			if(color_index >= Colors.RANDOM_COLORS.length) color_index = 0;
			kml.closeFolder(out);
		}
		
		kml.closeFolder(out); // close paths folder
		

		kml.printFolder(out, "points");
		
		out.println("<Style id=\"ff0000ff\">" +
	        	"<IconStyle>" +
	        	"<color>ff0000ff</color>" +
	        	"<scale>1.2</scale>" +
	        	"<Icon>" +
	        	"<href>http://maps.google.com/mapfiles/kml/shapes/shaded_dot.png</href>" +
	        	"</Icon>" +
	        	"</IconStyle>" +
				"</Style>");
		
		for(String day: evPerDay.keySet()) {
			kml.printFolder(out, day);
			for(PlsEvent pe: evPerDay.get(day)) {
				double lon1 = nm.get(pe.getCellac()).getBarycentreLongitude() + jitter(pe);
				double lat1 = nm.get(pe.getCellac()).getBarycentreLatitude() + jitter(pe);
				out.println("<Placemark>" +
						    "<name>"+pe.getTime().split(" ")[1]+"</name>" +
						    "<description>"+pe.getTime()+"</description>" +
						    "<styleUrl>#ff0000ff</styleUrl>" +
						    "<Point>" +	
						    "<coordinates>"+lon1+","+lat1+",0</coordinates>" +
						    "</Point>" +
						    "</Placemark>");
			}
			kml.closeFolder(out);
		}
		kml.closeFolder(out); // close points folder

		
		kml.closeFolder(out); // close user folder
	}

	
	static final String[] MONTHS = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	public static Map<String,List<PlsEvent>> splitByDay(List<PlsEvent> plsEvents ) {
		Map<String,List<PlsEvent>> eventsPerDay = new TreeMap<String,List<PlsEvent>>();
		
		for(PlsEvent pe: plsEvents) {	
			
			int d = pe.getCalendar().get(Calendar.DAY_OF_MONTH);
			int m = pe.getCalendar().get(Calendar.MONTH);
			int y = pe.getCalendar().get(Calendar.YEAR);
			String k = d < 10 ? "0" : ""; 
		    k = k + d+"-"+MONTHS[m]+"-"+y;
			List<PlsEvent> de = eventsPerDay.get(k);
			if(de == null) 
				de = new ArrayList<PlsEvent>();
			de.add(pe);
			eventsPerDay.put(k, de);
		}
		
		return eventsPerDay;
	}
	
	
	
	public static void closeFile() {
		kml.printFooterDocument(out);
		out.close();
	}
	
	public static double jitter(PlsEvent pe) {
		if(!JITTER) return 0;
		
		Random r = new Random();
		r.setSeed(pe.getTimeStamp());
		return 0.01 * r.nextDouble() - 0.005;
	}
	
	
	public static void main(String[] args) throws Exception {
		openFile(Config.getInstance().base_dir+"/test.kml");
		List<PlsEvent> data = PlsEvent.readEvents(new File(Config.getInstance().base_dir+"/UsersCSVCreator/test/164e6218294db749859bfffc798c5a51ba31262f6cfd7ab1e4e27d134789ba.csv"));
		print("164e6218294db749859bfffc798c5a51ba31262f6cfd7ab1e4e27d134789ba",data);
		closeFile();
		Logger.logln("Done!");
	}
	
}
