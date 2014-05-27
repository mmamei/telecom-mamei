package visual.kml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import region.RegionMap;
import utils.Colors;
import utils.FileUtils;
import utils.Logger;
import analysis.PLSEvent;
import dataset.file.DataFactory;

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
	
	
	
	
	public static void addKml(String kml) {
		out.println(kml);
	}
	
	static RegionMap nm = null;
	public static void print(String username, List<PLSEvent> plsEvents) {
		nm =  DataFactory.getNetworkMapFactory().getNetworkMap(plsEvents.get(0).getCalendar());
		kml.printFolder(out, username.substring(0,10)+"...");
		List<PLSEvent> s = plsEvents;//FilterAndCounterUtils.smooth(plsEvents);
		Map<String,List<PLSEvent>> evPerDay = splitByDay(s);
		
		
		
		
		kml.printFolder(out, "cells");
		for(String day: evPerDay.keySet()) {
			kml.printFolder(out, day);
			for(PLSEvent pe: evPerDay.get(day))
				out.println(nm.getRegion(String.valueOf(pe.getCellac())).toKml("#7f770077"));
			kml.closeFolder(out);
		}
		kml.closeFolder(out);
		
		kml.printFolder(out, "paths");
		int color_index = 0;
		
		
		for(String day: evPerDay.keySet()) {
			kml.printFolder(out, day);
			for(int i=0; i<evPerDay.get(day).size()-1;i++) {	
				PLSEvent pe = evPerDay.get(day).get(i);
				PLSEvent pe1 = evPerDay.get(day).get(i+1);
				int dmin =  (int)((pe1.getTimeStamp() - pe.getTimeStamp()) / 60000);
				if(dmin < 180) {
					double lon1 = nm.getRegion(String.valueOf(pe.getCellac())).getLatLon()[1] + jitter(pe);
					double lat1 = nm.getRegion(String.valueOf(pe.getCellac())).getLatLon()[0] + jitter(pe);
					double lon2 = nm.getRegion(String.valueOf(pe1.getCellac())).getLatLon()[1] + jitter(pe1);
					double lat2 = nm.getRegion(String.valueOf(pe1.getCellac())).getLatLon()[0] + jitter(pe1);
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
	        	"<href>https://maps.google.com/mapfiles/kml/shapes/shaded_dot.png</href>" +
	        	"</Icon>" +
	        	"</IconStyle>" +
				"</Style>");
		
		for(String day: evPerDay.keySet()) {
			kml.printFolder(out, day);
			for(PLSEvent pe: evPerDay.get(day)) {
				double lon1 = nm.getRegion(pe.getCellac()).getLatLon()[1] + jitter(pe);
				double lat1 = nm.getRegion(pe.getCellac()).getLatLon()[0] + jitter(pe);
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

	static final String[] DAY_WEEK = new String[]{"0","Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
	static final String[] MONTHS = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	public static Map<String,List<PLSEvent>> splitByDay(List<PLSEvent> plsEvents ) {
		Map<String,List<PLSEvent>> eventsPerDay = new TreeMap<String,List<PLSEvent>>();
		
		for(PLSEvent pe: plsEvents) {	
			
			int d = pe.getCalendar().get(Calendar.DAY_OF_MONTH);
			int m = pe.getCalendar().get(Calendar.MONTH);
			int y = pe.getCalendar().get(Calendar.YEAR);
			int dow = pe.getCalendar().get(Calendar.DAY_OF_WEEK);
			String k = d < 10 ? "0" : ""; 
		    k = k + d+"-"+MONTHS[m]+"-"+y+"-"+DAY_WEEK[dow];
			List<PLSEvent> de = eventsPerDay.get(k);
			if(de == null) 
				de = new ArrayList<PLSEvent>();
			de.add(pe);
			eventsPerDay.put(k, de);
		}
		
		return eventsPerDay;
	}
	
	
	
	public static void closeFile() {
		kml.printFooterDocument(out);
		out.close();
	}
	
	public static double jitter(PLSEvent pe) {
		if(!JITTER) return 0;
		
		Random r = new Random();
		r.setSeed(pe.getTimeStamp());
		return 0.01 * r.nextDouble() - 0.005;
	}
	
	
	private static List<PLSEvent> getDataFormUserEventCounterCellacXHour(String file, String username) throws Exception {
		// read the UserEventCounterCellacXHour file ti find the line corresponding to the user being looked for.
		// parse that line to create the List<PlsEvent> object
		
		List<PLSEvent> l = null;
		
		File f = FileUtils.getFile("BASE/UserEventCounter/"+file);
		if(f == null) {
			Logger.logln("Launch UserEventCounterCellacXHour first!");
			System.exit(0);
		}
		BufferedReader br = new BufferedReader(new FileReader(f));
		
		String line;
		while((line=br.readLine())!=null) 
			if(line.startsWith(username)) {
				l = getDataFormUserEventCounterCellacXHourLine(line);
				break;
			}
		br.close();
		return l;
	}
	
	public static List<PLSEvent> getDataFormUserEventCounterCellacXHourLine(String line) {
		List<PLSEvent> l = null;
		String[] el = line.split(",");
		String username = el[0];
		String imsi = el[1];
		l = new ArrayList<PLSEvent>();
		for(int i=5;i<el.length;i++) {
			String[] pls = el[i].split(":"); // 2013-3-27:Sat:19:1972908327
			String[] ymd = pls[0].split("-");
			int y = Integer.parseInt(ymd[0]);
			int m = Integer.parseInt(ymd[1]);
			int d = Integer.parseInt(ymd[2]);
			int h = Integer.parseInt(pls[2]);
			Calendar cal = new GregorianCalendar(y,m,d,h,0,0);
			String timestamp = ""+cal.getTimeInMillis();
			String celllac = pls[3];
			PLSEvent pe = new PLSEvent(username,imsi,celllac,timestamp);
			l.add(pe);
			//Logger.logln(pe.toString());
		}
		return l;
	}
	
	
	public static void main(String[] args) throws Exception {
		openFile(FileUtils.createDir("BASE/TouristData").getAbsolutePath()+"/test.kml");
		
		String user = "feaf164623aa5fcac0512b3b4a62496c34458ac017141a808dfe306b62759f";
		File dir = FileUtils.createDir("BASE/UsersCSVCreator/test");
		List<PLSEvent> data = PLSEvent.readEvents(new File(dir+"/"+user+".csv"));
		
		/*
		String user = "6f73f1939cbec78c2aa4d8da3ed44da8ed0357b46ccee4439836ec6fb7b90fe";
		List<PlsEvent> data = getDataFormUserEventCounterCellacXHour("file_pls_fi_Firenze_cellXHour.csv",user);
		*/
		print(user,data);
		
		closeFile();
		Logger.logln("Done!");
	}
	
}
