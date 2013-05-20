package area;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import pls_parser.AnalyzePLSCoverage;
import utils.Config;


public class CityEvent {
	
	private static Map<String,CityEvent> CITY_EVENTS;
	
	private static final SimpleDateFormat F = new SimpleDateFormat("dd/MM/yyyy HH:mm");
								
	public Placemark spot;
	public String startTime;
	public String endTime;
	public Calendar st;
	public Calendar et;
	public int head_count;
	
	public CityEvent(Placemark spot, String startTime, String endTime, int head_count) {
		this.spot = spot;
		this.startTime = startTime;
		this.endTime = endTime;
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
	
	
	public static Collection<CityEvent> getAllEvents() {
		if(CITY_EVENTS == null) init();
		return CITY_EVENTS.values();
	}
	
	public static CityEvent getEvent(String event) {
		if(CITY_EVENTS == null) init();
		return CITY_EVENTS.get(event);
	}
	
	public static Collection<CityEvent> getEventsInData() {
		
		Map<String,String> ad = AnalyzePLSCoverage.compute();

		if(CITY_EVENTS == null) init();
		Collection<CityEvent> result = new ArrayList<CityEvent>();
		
		for(CityEvent ce: CITY_EVENTS.values()) {
			String[] s = get(ce.st);
			String[] e = get(ce.et);
			
			if(ad.get(s[0])!=null && ad.get(s[0]).contains(s[1]) &&
			   ad.get(e[0])!=null && ad.get(e[0]).contains(e[1]))
				result.add(ce);
		}
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
	 
	
	
	
	
	public static void init() {
		CITY_EVENTS = new HashMap<String,CityEvent>();
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
				CITY_EVENTS.put(placemark+","+start.substring(0, start.indexOf(" ")),new CityEvent(Placemark.getPlacemark(placemark),start,end,hc));
			}
			br.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public CityEvent(Placemark spot, Calendar st, Calendar et,int head_count) {
		this.spot = spot;
		this.st = st;
		this.et = et;
		try {
			this.startTime = F.format(st.getTime());
			this.endTime = F.format(et.getTime());
		} catch(Exception e) {
			e.printStackTrace();
		}
		this.head_count = head_count;
	}
	
	
	
	public CityEvent changeDay(int day,int month,int year) {
		String newStartTime = day+"/"+(month+1)+"/"+year+startTime.substring(startTime.indexOf(" "));
		String newEndTime = day+"/"+(month+1)+"/"+year+endTime.substring(endTime.indexOf(" "));
		return new CityEvent(spot,newStartTime,newEndTime,head_count);
	}
	 
	public String toFileName() {
		return toString()+".txt";
	}
	
	public String toString() {
		return spot.name.replaceAll("[/\\:\\s]", "_")+"-"+startTime.replaceAll("[/\\:\\s]", "_")+"-"+endTime.replaceAll("[/\\:\\s]", "_");
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
		Placemark p = new Placemark(ce.spot.name,ce.spot.center,ce.spot.radius+space_shift);
		Calendar st = (Calendar)ce.st.clone();
		st.add(Calendar.HOUR_OF_DAY, -time_shift);
		Calendar et = (Calendar)ce.et.clone();
		et.add(Calendar.HOUR_OF_DAY, time_shift);
		CityEvent cevent = new CityEvent(p,st,et,ce.head_count);
		return cevent;
	}
	
	
	public static void main(String[] args) {
		Collection<CityEvent> coll = getEventsInData();
		for(CityEvent ce : coll)
			System.out.println(ce);
	}
	
	
}
