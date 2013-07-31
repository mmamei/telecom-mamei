package analysis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import pls_parser.UsersCSVCreator;

import utils.Config;
import utils.Logger;
import visual.kml.KMLHeatMap;
import visual.kml.KML;
import area.CityEvent;
import area.Placemark;

public class AreaHeatMap {
	
	static final String[] MONTHS = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	static final String[] DAYS = new String[]{"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
	
	
	public static void main(String[] args) throws Exception {
		CityEvent ce = CityEvent.getEvent("Juventus Stadium (TO),20/03/2012");
		execute(ce);
	}
	
	public static void execute(CityEvent ce) throws Exception {
		
		String dir = Config.getInstance().base_dir+"/UsersCSVCreator/"+ce.toString();
		File fd = new File(dir);
		if(!fd.exists()) {
			Logger.logln(dir+" does not exist");
			UsersCSVCreator.create(ce);
		}
		else Logger.logln(dir+" already exists");
		
		
		CityEvent cevent = CityEvent.expand(ce,10,10000);
		
		List<PlsEvent> events = PlsEvent.readEvents(new File(dir),cevent.st,cevent.et);
		Map<String,Map<Long,Double>> hms = new TreeMap<String,Map<Long,Double>>();
		
		for(PlsEvent e: events) {
			
			if(!cevent.spot.contains(e.getCellac())) continue;
			
			String key = getKey(e.getCalendar());
			Map<Long,Double> hm = hms.get(key);
			if(hm == null) hm = new HashMap<Long,Double>();			
			Double count = hm.get(e.getCellac());
			hm.put(e.getCellac(), count == null ? 1 : count+1);
			hms.put(key, hm);
		}
		
		// compute overall max value
		double max = 0;
		for(Map<Long,Double> hm: hms.values()) 
		for(double v: hm.values())
			if(v > max) max = v;
		
		
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File("output/heat/HeatMap_"+ce.toFileName()+".kml"))));
		KML kml = new KML();
		kml.printHeaderDocument(out, ce.toString());
		// create the heat map
		Calendar startTime = events.get(0).getCalendar();
		Calendar endTime = events.get(events.size()-1).getCalendar();
		while(!startTime.after(endTime)) {
			String key = getKey(startTime);
			kml.printFolder(out, getLabel(startTime));
			
			out.println("<TimeStamp>");
			int y = startTime.get(Calendar.YEAR);
			int m = startTime.get(Calendar.MONTH)+1;
			String sm = m < 10 ? "0"+m : ""+m;
			int d = startTime.get(Calendar.DAY_OF_MONTH);
			String sd = d < 10 ? "0"+d : ""+d;
			int h = startTime.get(Calendar.HOUR_OF_DAY);
			String sh = h < 10 ? "0"+h : ""+h;
			
			out.println("<when>"+y+"-"+sm+"-"+sd+"T"+sh+":00</when>");
			out.println("</TimeStamp>");
			
			
			Map<Long,Double> map = hms.get(key);
			out.println(KMLHeatMap.drawHeatMap(getLabel(startTime), map, max));
			kml.closeFolder(out);
			startTime.add(Calendar.HOUR, 1);
		}
		kml.printFooterDocument(out);
		out.close();
		Logger.logln("Done!");
	}
	
	public static String getKey(Calendar cal) {
		return cal.get(Calendar.DAY_OF_MONTH)+"-"+
			 	MONTHS[cal.get(Calendar.MONTH)]+"-"+
			 	cal.get(Calendar.YEAR)+":"+
			 	cal.get(Calendar.HOUR_OF_DAY);
	}
	
	public static String getLabel(Calendar cal) {
		//return "-"+cal.get(Calendar.DAY_OF_MONTH)+":"+DAYS[cal.get(Calendar.DAY_OF_WEEK)-1]+"-";
		return "["+cal.get(Calendar.DAY_OF_MONTH)+"-"+DAYS[cal.get(Calendar.DAY_OF_WEEK)-1]+":"+cal.get(Calendar.HOUR_OF_DAY)+"]";
	}
}
