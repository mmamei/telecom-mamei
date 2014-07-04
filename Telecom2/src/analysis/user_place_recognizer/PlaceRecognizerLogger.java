package analysis.user_place_recognizer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.gps.utils.LatLonPoint;

import region.RegionI;
import region.RegionMap;
import utils.Config;
import visual.kml.KML;
import analysis.PLSEvent;
import dataset.file.DataFactory;



public class PlaceRecognizerLogger {
	
	public static void log(String username, String kind_of_place, Map<Integer, Cluster> clusters) {
		try {
		File dir = new File(Config.getInstance().base_folder+"/PlaceRecognizerLogger/"+username+"/"+kind_of_place);
		dir.mkdirs();
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(dir+"/eventsXcluster.txt")));	
		for(int k : clusters.keySet()) {
			out.println("Cluster "+k+":");
			List<PLSEvent> events = clusters.get(k).getEvents();
			for(PLSEvent e: events) {
				out.println("\t"+e);
			}
		}
		out.close();
		
		DecimalFormat df = new DecimalFormat("###.####",new DecimalFormatSymbols(Locale.US));
		out = new PrintWriter(new BufferedWriter(new FileWriter(dir+"/weight_evolution.txt")));	
		for(int k : clusters.keySet()) {
			Cluster c = clusters.get(k);
			double w_time = c.getWeight("WeightOnTime");
			double w_day = c.getWeight("WeightOnDay");
			double w_diversity = c.getWeight("WeightOnDiversity");		
			
			out.println("Cluster "+k+":\tsize = "+c.size()+
					                  "\tw_time = "+df.format(w_time) + 
									  "\tw_day = "+df.format(w_time+w_day) +
								      "\tw_diversity = "+df.format(w_time+w_day+w_diversity));
	
		}
		out.close();
		
		
		
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private static final String[] COLORS = new String[]{
		"0000ff", "ff0000", "00ff00", "ff00ff", "ffff00", "00ffff",
		"000077", "770000", "007700", "770077", "777700", "007777",
		"bb0000", "330000", "bb0077", "77bb33", "33bb77", "bb3377",
		"ddaaee", "ee9900", "55f63e", "c3c433", "ffff00", "00ffff"
	};
	
	
	
	private static PrintWriter totalCSV;
	public static void openTotalCSVFile(String file) {
		try{
			totalCSV = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void closeTotalCSVFile() {
		if(totalCSV!=null) totalCSV.close();
	}
	public static void logcsv(String username, String kind_of_place, List<LatLonPoint> placemarks) {
		if(totalCSV!=null){
			StringBuffer sb = new StringBuffer();
			for(LatLonPoint p: placemarks)
				sb.append(","+p.getLongitude()+" "+p.getLatitude());
			totalCSV.println(username+","+kind_of_place+""+sb.toString());
		}
	}
	
	
	
	private static PrintWriter outKml;
	private static KML kml;
	public static void openKMLFile(String file) {
		try {
			outKml = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			kml = new KML();
			kml.printHeaderFolder(outKml, "Results");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	
	public static void closeKMLFile() {
		try {
			kml.printFooterFolder(outKml);
			outKml.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void openUserFolderKML(String username) {
		kml.printFolder(outKml, username.length() > 5 ? username.substring(0,5) : username);
	}
	
	public static void closeUserFolderKML() {
		kml.closeFolder(outKml);
	}
	
	//static NetworkMap NM = NetworkMapFactory.getNetworkMap(Config.getInstance().pls_start_time);
	public static void logkml(String kind_of_place, Map<Integer, Cluster> clusters, List<LatLonPoint> placemarks) {
		try {
			
			kml.printFolder(outKml, kind_of_place);
			int colorIndex=0;
			
			
			
			kml.printFolder(outKml, "results");
			
			outKml.println("<Style id=\""+getColor(kind_of_place)+"\">" +
						   "<IconStyle>" +
						   "<color>ff"+getColor(kind_of_place)+"</color>" +
						   "<scale>1.2</scale>" +
						   "<Icon>" +
						   "<href>http://maps.google.com/mapfiles/kml/shapes/shaded_dot.png</href>" +
						   "</Icon>" +
						   "</IconStyle>" +
							"</Style>");
				
			
			for(LatLonPoint p: placemarks)
				outKml.println("<Placemark>" +
						    "<name>"+kind_of_place+"</name>" +
						    "<styleUrl>#"+getColor(kind_of_place)+"</styleUrl>" +
						    "<Point>" +
						    "<coordinates>"+p.getLongitude()+","+p.getLatitude()+",0</coordinates>" +
						    "</Point>" +
						    "</Placemark>");
			
			
			kml.closeFolder(outKml);
			
			
			
			for(int k: clusters.keySet()){
				
				if(k == -1) continue;
				
				List<PLSEvent> clusterEvents = clusters.get(k).getEvents();
				
				kml.printFolder(outKml, "cluster "+k);
				
				Map<String, List<PLSEvent>> clusterByCells = new HashMap<String, List<PLSEvent>>();
				for(PLSEvent e: clusterEvents){
					List<PLSEvent> l = clusterByCells.get(e.getCellac());
					if(l == null) {
						l = new ArrayList<PLSEvent>();
						clusterByCells.put(e.getCellac(), l);
					}
					l.add(e);
				} 
				
				
				
				
				for(String celllac: clusterByCells.keySet()){
					String desc = getDescription(clusterEvents, clusterByCells.get(celllac));
					int cellsize = clusterByCells.get(celllac).size();
					
					RegionMap nm = DataFactory.getNetworkMapFactory().getNetworkMap(clusterByCells.get(celllac).get(0).getTimeStamp());
					
					
					
					RegionI cell = nm.getRegion(celllac);
					
					
					if(cell!=null) {
						String name = "Cluster N. "+k+", cell_lac: "+celllac+", size: "+cellsize+"/"+clusterEvents.size();
						cell.setDescription(name+". "+desc);
						outKml.println(cell.toKml("aa"+COLORS[colorIndex % (COLORS.length)]));
					}
				}
				colorIndex++;
				kml.closeFolder(outKml);
			}
			kml.closeFolder(outKml);
			
					
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static String getColor(String kind_of_place) {
		if(kind_of_place.startsWith("HOME")) 
			return COLORS[0];
		else if(kind_of_place.startsWith("WORK")) 
			return COLORS[1];
		else if(kind_of_place.startsWith("FRIDAY_NIGHT")) 
			return COLORS[2];
		else if(kind_of_place.startsWith("SATURDAY_NIGHT")) 
			return COLORS[3];
		else if(kind_of_place.startsWith("SUNDAY")) 
			return COLORS[4];
		else if(kind_of_place.startsWith("NIGHT")) 
			return COLORS[5];
		else if(kind_of_place.startsWith("GENERIC")) 
			return COLORS[6];
		return COLORS[7];
	}
	
	
	
	public static final String[] DAYS = {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
	public static final int WIDTH = 200;
	public static final int HEIGHT = 100;
	
	public static String getDescription(List<PLSEvent> cluster, List<PLSEvent> cell) {
		
		StringBuffer sb = new StringBuffer();
		sb.append("<table>");
		sb.append("<tr>");
		
		sb.append("<td>");
		sb.append("<b>Hour Counter Cluster</b><br>");
		sb.append(getHourDist(cluster));
		sb.append("</td>");
		
		sb.append("<td>");
		sb.append("<b>Hour Counter Cell</b><br>");
		sb.append(getHourDist(cell));
		sb.append("</td>");
		
		sb.append("</tr><tr>");
		
		sb.append("<td>");
		sb.append("<b>Day Counter Cluster</b><br>");
		sb.append(getDayDist(cluster));
		sb.append("</td>");
		
		sb.append("<td>");
		sb.append("<b>Day Counter Cell</b><br>");
		sb.append(getDayDist(cell));
		sb.append("</td>");
		
		sb.append("</tr>");
		sb.append("</table>");
		return sb.toString();
		
	}
	
	public static String getHourDist(List<PLSEvent> x) {
		int[] counter = new int[24];
		Calendar c = new GregorianCalendar();
		for(PLSEvent e: x){
			c.setTimeInMillis(e.getTimeStamp());
			//int day_of_week = c.get(Calendar.DAY_OF_WEEK)-1;
			//int day = c.get(Calendar.DAY_OF_MONTH);
			//int month = c.get(Calendar.MONTH);
			int hour = c.get(Calendar.HOUR_OF_DAY);
			
			counter[hour]++;
		}
		int max = 0;
		StringBuffer sb2 = new StringBuffer();
		for(int i=0; i<counter.length;i++) {
			max = counter[i] > max ? counter[i] : max;
			sb2.append(","+counter[i]);
		}
		String data = sb2.toString().substring(1);
		
		return "<img src=\"http://chart.googleapis.com/chart?chxl=0:|0|6|12|18|23&chxp=0,0,6,12,18,23&chxr=0,0,23|1,0,"+max+"&chxt=x,y&chbh=a&chs="+WIDTH+"x"+HEIGHT+"&cht=bvs&chco=76A4FB&chds=0,"+max+"&chd=t:"+data+"\" width=\""+WIDTH+"\" height=\""+HEIGHT+"\" alt=\"\" />";
	}
	
	public static String getDayDist(List<PLSEvent> x) {
		int[] counter = new int[7];
		Calendar c = new GregorianCalendar();
		for(PLSEvent e: x){
			c.setTimeInMillis(e.getTimeStamp());
			int day_of_week = c.get(Calendar.DAY_OF_WEEK)-1;
			//int day = c.get(Calendar.DAY_OF_MONTH);
			//int month = c.get(Calendar.MONTH);
			//int hour = c.get(Calendar.HOUR_OF_DAY);
			
			counter[day_of_week]++;
		}
		int max = 0;
		StringBuffer sb2 = new StringBuffer();
		for(int i=0; i<counter.length;i++) {
			max = counter[i] > max ? counter[i] : max;
			sb2.append(","+counter[i]);
		}
		String data = sb2.toString().substring(1);
		
		return "<img src=\"http://chart.googleapis.com/chart?chxl=0:|Sun|Mon|Tue|Wed|Thu|Fri|Sat&chxp=0,0.5,1.5,2.5,3.5,4.5,5.5,6.5&chxr=0,0,7|1,0,"+max+"&chxt=x,y&chbh=a,3&chs="+WIDTH+"x"+HEIGHT+"&cht=bvs&chco=008000&chds=0,"+max+"&chd=t:"+data+"\" width=\""+WIDTH+"\" height=\""+HEIGHT+"\" alt=\"\" />";
	}	
	
	
}
