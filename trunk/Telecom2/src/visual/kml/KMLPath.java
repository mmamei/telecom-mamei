package visual.kml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import network.NetworkMap;
import network.NetworkMapFactory;
import utils.Colors;
import utils.Config;
import utils.FileUtils;
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
	
	
	private static List<PlsEvent> getDataFormUserEventCounterCellacXHour(String file, String username) throws Exception {
		List<PlsEvent> l = null;
		// read the UserEventCounterCellacXHour file ti find the line corresponding to the user being looked for.
		// parse that line to create the List<PlsEvent> object
		
		BufferedReader br = FileUtils.getBR("UserEventCounter/"+file);
		if(br == null) {
			Logger.logln("Launch UserEventCounterCellacXHour first!");
			System.exit(0);
		}
		String line;
		while((line=br.readLine())!=null) 
			if(line.startsWith(username)) {
					String[] el = line.split(",");
					String imsi = el[1];
					l = new ArrayList<PlsEvent>();
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
						//Logger.logln(pe.toString());
					}
					break;
			}
		br.close();
		return l;
	}
	
	
	public static void main(String[] args) throws Exception {
		openFile(FileUtils.create("TouristData").getAbsolutePath()+"/test.kml");
		
		/*
		String user = "164e6218294db749859bfffc798c5a51ba31262f6cfd7ab1e4e27d134789ba";
		List<PlsEvent> data = PlsEvent.readEvents(new File(Config.getInstance().base_dir+"/UsersCSVCreator/test/"+user+".csv"));
		*/
		
		String user = "6f73f1939cbec78c2aa4d8da3ed44da8ed0357b46ccee4439836ec6fb7b90fe";
		List<PlsEvent> data = getDataFormUserEventCounterCellacXHour("file_pls_fi_Firenze_cellXHour.csv",user);
		
		print(user,data);
		
		closeFile();
		Logger.logln("Done!");
	}
	
}
