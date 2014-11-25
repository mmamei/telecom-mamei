package visual.kml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import region.RegionI;
import region.RegionMap;
import utils.Colors;
import utils.Config;
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
		
		
		
		nm =  DataFactory.getNetworkMapFactory().getNetworkMap(plsEvents.iterator().next().getCalendar());
		kml.printFolder(out, username.substring(0,10)+"...");
		List<PLSEvent> s = plsEvents;//FilterAndCounterUtils.smooth(plsEvents);
		Map<String,List<PLSEvent>> evPerDay = splitByDay(s);
		
		
		
		
		kml.printFolder(out, "cells");
		for(String day: evPerDay.keySet()) {
			kml.printFolder(out, day);
			for(PLSEvent pe: evPerDay.get(day)) {
				RegionI r = nm.getRegion(pe.getCellac());
				if(r!=null)
					out.println(r.toKml("#7f770077"));
			}
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
					
					RegionI r = nm.getRegion(pe.getCellac());
					RegionI r1 = nm.getRegion(pe.getCellac());
					
					if(r!=null && r1!=null) {
					double lon1 = r.getLatLon()[1] + jitter(pe);
						double lat1 = r.getLatLon()[0] + jitter(pe);
						double lon2 = r1.getLatLon()[1] + jitter(pe1);
						double lat2 = r1.getLatLon()[0] + jitter(pe1);
						//out.println(KMLArrow.printArrow(lon1, lat1, lon2, lat2, 2, Colors.RANDOM_COLORS[color_index],true));
						out.println(KMLArrowCurved.printArrow(lon1, lat1, lon2, lat2, 2, Colors.RANDOM_COLORS[color_index],true));
					}
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
		
		
		Map<String,String> hour_time = new HashMap<String,String>();
		hour_time.put("00:00", "night"); hour_time.put("01:00", "night"); hour_time.put("02:00", "night"); hour_time.put("03:00", "night"); 
		hour_time.put("04:00", "night"); hour_time.put("05:00", "night"); hour_time.put("06:00", "night"); hour_time.put("07:00", "night"); 
		hour_time.put("08:00", "day"); hour_time.put("09:00", "day"); hour_time.put("10:00", "day"); hour_time.put("11:00", "day"); 
		hour_time.put("12:00", "day"); hour_time.put("13:00", "day"); hour_time.put("14:00", "day"); hour_time.put("15:00", "day"); 
		hour_time.put("16:00", "day"); hour_time.put("17:00", "day"); hour_time.put("18:00", "day"); hour_time.put("19:00", "day"); 
		hour_time.put("20:00", "night"); hour_time.put("21:00", "night"); hour_time.put("22:00", "night"); hour_time.put("23:00", "night"); 
		
							// weekday-night, // weekend-night, weekday-day, weekend-day
		String[] pcolors = new String[]{"ff000000","ffaa0000","ff0000ff","ff00ff00"};
		
		for(String color : pcolors)
		out.println("<Style id=\""+color+"\">" +
	        	"<IconStyle>" +
	        	"<color>"+color+"</color>" +
	        	"<scale>1.2</scale>" +
	        	"<Icon>" +
	        	"<href>https://maps.google.com/mapfiles/kml/shapes/shaded_dot.png</href>" +
	        	"</Icon>" +
	        	"</IconStyle>" +
				"</Style>");
		
		for(String day: evPerDay.keySet()) {
			kml.printFolder(out, day);
			
			boolean weekend = day.endsWith("Sat") || day.endsWith("Sun");
			boolean weekday = !weekend;
			
			for(PLSEvent pe: evPerDay.get(day)) {
				RegionI r = nm.getRegion(pe.getCellac());
				if(r!=null) {
					
					String hour = pe.getTime().split(" ")[1];
					String color = "";
					if(weekday && hour_time.get(hour).equals("day")) color = "ff0000ff";
					if(weekday && hour_time.get(hour).equals("night")) color = "ff000000";
					if(weekend && hour_time.get(hour).equals("day")) color = "ff00ff00";
					if(weekend && hour_time.get(hour).equals("night")) color = "ffaa0000";
					
					double lon1 = r.getLatLon()[1] + jitter(pe);
					double lat1 = r.getLatLon()[0] + jitter(pe);
					out.println("<Placemark>" +
							    "<name>"+hour+"</name>" +
							    "<description>"+pe.getTime()+"</description>" +
							    "<styleUrl>#"+color+"</styleUrl>" +
							    "<Point>" +	
							    "<coordinates>"+lon1+","+lat1+",0</coordinates>" +
							    "</Point>" +
							    "</Placemark>");
				}
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
		
		File f = new File(Config.getInstance().base_folder+"/UserEventCounter/"+file);
		if(f == null) {
			Logger.logln("Launch UserEventCounterCellacXHour first!");
			System.exit(0);
		}
		BufferedReader br = new BufferedReader(new FileReader(f));
		
		String line;
		while((line=br.readLine())!=null) 
			if(line.startsWith(username)) {
				l = PLSEvent.getDataFormUserEventCounterCellacXHourLine(line);
				break;
			}
		br.close();
		return l;
	}
	
	
	
	// single user
	public static void main2(String[] args) throws Exception {
		openFile(new File(Config.getInstance().base_folder+"/TouristData").getAbsolutePath()+"/test.kml");
		
		String user = "feaf164623aa5fcac0512b3b4a62496c34458ac017141a808dfe306b62759f";
		File dir = new File(Config.getInstance().base_folder+"/UsersCSVCreator/test");
		dir.mkdirs();
		List<PLSEvent> data = PLSEvent.readEvents(new File(dir+"/"+user+".csv"));
		
		/*
		String user = "6f73f1939cbec78c2aa4d8da3ed44da8ed0357b46ccee4439836ec6fb7b90fe";
		List<PlsEvent> data = getDataFormUserEventCounterCellacXHour("file_pls_fi_Firenze_cellXHour.csv",user);
		*/
		print(user,data);
		
		closeFile();
		Logger.logln("Done!");
	}
	
	
	// multiple users
	public static void main(String[] args) throws Exception {
		openFile(new File(Config.getInstance().base_folder+"/Tourist").getAbsolutePath()+"/melpignano.kml");
		
		
		File dir = new File(Config.getInstance().base_folder+"/UsersCSVCreator/Melpignano-22_08_2014_00_00-25_08_2014_00_00.txt_STR");
		for(File f: dir.listFiles()) {
			System.out.println("Processing "+f.getName());
			print(f.getName(),PLSEvent.readEvents(f));
		}
		
		closeFile();
		Logger.logln("Done!");
	}
	
	
}
