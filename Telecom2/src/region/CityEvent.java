package region;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.Config;
import dataset.DataFactory;
import dataset.EventFilesFinderI;
import dataset.PLSCoverageTimeI;


public class CityEvent {
	
	private static final SimpleDateFormat F = new SimpleDateFormat("dd/MM/yyyy HH:mm");
								
	public Placemark spot;
	public Calendar st;
	public Calendar et;
	public int head_count;
	
	public CityEvent(Placemark spot, String startTime, String endTime, int head_count) {
		this.spot = spot;
		st = new GregorianCalendar();
		et = new GregorianCalendar();
		try {
			st.setTime(F.parse(startTime));
			et.setTime(F.parse(endTime));
		} catch(Exception e) {
			e.printStackTrace();
		}
		this.head_count = head_count;
	}
	
	
	public int durationH() {
		return (int)((et.getTimeInMillis() - st.getTimeInMillis()) / 3600000);
	}
	
	public static Map<String,CityEvent> getAllEvents() {
		Map<String,CityEvent> all = new HashMap<String,CityEvent>();		
		try {
			BufferedReader br = new BufferedReader(new FileReader(Config.getInstance().events_file));
			String line;
			while((line = br.readLine())!=null) {
				if(line.startsWith("//") || line.trim().length() < 3) continue;
				String[] el = line.split(",");
				String placemark = el[0].trim();
				String start = el[1].trim();
				String end = el[2].trim();
				int hc = Integer.parseInt(el[3].trim());
				CityEvent ce = new CityEvent(Placemark.getPlacemark(placemark),start,end,hc);
				all.put(placemark+","+start.substring(0, start.indexOf(" ")),ce);
			}
			br.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return all;
	}
	
	public static CityEvent getEvent(String event) {
		return getAllEvents().get(event);
	}
	
	
	/*
	 * This method retrieves only those events that happen in a time covered by PLS data
	 */
	
	public static List<CityEvent> getEventsInData() {
		
		/*
		 * create a map with the days covered by the dataset
		 * key = 2013/Jul/18
		 * values = 0-0-1-1-2-2-3-3-4-4-5-5-6-6-7-7-8-.... 23
		 */
		
		
		
		PLSCoverageTimeI apc = DataFactory.getPLSCoverageTime();
		String k = Config.getInstance().pls_folder;
		k = k.substring(k.indexOf("file_pls/")+9);
		k = k.substring(0,k.indexOf("/"));
		List<String> ad = apc.computeAll().get(k);
		
	
		
		//for(String s : ad.keySet())
		//	System.out.println(s+" ==> "+ad.get(s));
		

		List<CityEvent> result = new ArrayList<CityEvent>();
		
		for(CityEvent ce: getAllEvents().values()) {
			String[] s = get(ce.st);
			String[] e = get(ce.et);
			
			
			//String dir = eff.find("2014-03-10","4","2014-03-10","7",11.2477,43.7629,11.2491,43.7620);
			// get region
			EventFilesFinderI eff = DataFactory.getEventFilesFinder();
			
			
			
			String dir = eff.find(ce.st,ce.et,ce.spot.getLatLon()[1],ce.spot.getLatLon()[0],ce.spot.getLatLon()[1],ce.spot.getLatLon()[0]);
				
			String key_s = s[0];
			String key_e = e[0];
			
			//System.out.println(key_s);
			//System.out.println(key_e);
			
			if(ad.contains(key_s)  && ad.contains(key_e))
				result.add(ce);
		}
		
		
		Collections.sort(result,new Comparator<CityEvent>(){
			public int compare(CityEvent e1, CityEvent e2) {
				return e1.st.compareTo(e2.st);
			}
			
		});
		
		return result;
	}
	
	static final String[] MONTHS = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	public static String[] get(Calendar cal) {
		int day =  cal.get(Calendar.DAY_OF_MONTH);
		String sday = day < 10 ? "0"+day : ""+day;
		String date = cal.get(Calendar.YEAR)+"/"+MONTHS[cal.get(Calendar.MONTH)]+"/"+sday;
		String h = cal.get(Calendar.HOUR_OF_DAY)+"-";
		return new String[]{date,h};
	}
	
	
	/*
	private static final SimpleDateFormat F2 = new SimpleDateFormat("yyyy/MMM/dd",Locale.US);
	public static boolean isInCoverage(CityEvent ce, Map<String,String> coverage) {
		boolean startCovered = false;
		boolean endCovered = false;
		for(String key: coverage.keySet()) {
			String[] elements = key.split("-");
			String region = elements[0];
			String day = elements[1];
			String hours = coverage.get(key);
			if(ce.spot.region.equals(region)) {
				String start_day = F2.format(ce.st.getTime());
				String start_h = ce.st.get(Calendar.HOUR_OF_DAY)+"-";
				String end_day = F2.format(ce.et.getTime());
				String end_h = ce.et.get(Calendar.HOUR_OF_DAY)+"-";
				if(start_day.equals(day) && hours.contains(start_h)) startCovered = true;
				if(end_day.equals(day) && hours.contains(end_h)) endCovered = true;
				if(startCovered && endCovered) break;
			} 
		}
		return startCovered && endCovered;
	}
	*/
	
	public CityEvent(Placemark spot, Calendar st, Calendar et,int head_count) {
		this.spot = spot;
		this.st = st;
		this.et = et;
		this.head_count = head_count;
	}
	
	
	
	public CityEvent changeDay(int day,int month,int year) {
		Calendar start = (Calendar)st.clone();
		start.set(Calendar.DAY_OF_MONTH, day);
		start.set(Calendar.MONTH, month);
		start.set(Calendar.YEAR, year);
		
		Calendar end = (Calendar)et.clone();
		end.set(Calendar.DAY_OF_MONTH, day);
		end.set(Calendar.MONTH, month);
		end.set(Calendar.YEAR, year);
		
		return new CityEvent(spot,start,end,head_count);
	}
	 
	public String toFileName() {
		return toString()+".txt";
	}
	
	public String toString() {
		String startTime = F.format(st.getTime());
		String endTime = F.format(et.getTime());
		return spot.getName().replaceAll("[/\\:\\s]", "_")+"-"+startTime.replaceAll("[/\\:\\s]", "_")+"-"+endTime.replaceAll("[/\\:\\s]", "_");
	}
	
	
	public double[] dailyPattern(int size) {
		
		if(size != 24 && size !=48) {
			System.err.println("Only size of 24 and 48 are supported. " +
					"They correspond to hour-based or half-an-hour-based sampling");
			return null;
		}
		
		int startIndex = 0;
		int endIndex = 0;
		
		if(size == 48) {		
			startIndex = 2 * st.get(Calendar.HOUR_OF_DAY) + (st.get(Calendar.MINUTE) < 30 ? 0 : 1);
			endIndex = 2 * et.get(Calendar.HOUR_OF_DAY) + (et.get(Calendar.MINUTE) < 30 ? 0 : 1);
		}
		if(size == 24) {		
			startIndex = st.get(Calendar.HOUR_OF_DAY);
			endIndex = et.get(Calendar.HOUR_OF_DAY);
		}
		double[] dp = new double[size];
		for(int i=startIndex;i<=endIndex;i++)
			dp[i] = 1;
		return dp;
	}
	
	
	public static CityEvent expand(CityEvent ce, int time_shift, double space_shift) {
		Placemark p = new Placemark(ce.spot.getName(),ce.spot.getLatLon(),ce.spot.getRadius()+space_shift);
		Calendar st = (Calendar)ce.st.clone();
		st.add(Calendar.HOUR_OF_DAY, -time_shift);
		Calendar et = (Calendar)ce.et.clone();
		et.add(Calendar.HOUR_OF_DAY, time_shift);
		CityEvent cevent = new CityEvent(p,st,et,ce.head_count);
		return cevent;
	}
	
	
	public static void main(String[] args) throws Exception {
		Collection<CityEvent> coll = getEventsInData();
		for(CityEvent ce : coll)
			System.out.println(ce);
	}
}
