package dataset.file;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import region.Placemark;
import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.Logger;
import utils.Mail;

public class FastHome extends BufferAnalyzerConstrained {
	
	private String title;
	private Map<String,Map<String,Set<String>>> users_lonlat_days;
	private RegionMap nm;
	private int min_days;
	
	FastHome(Placemark p, int min_days) {
		super(p,null);
		this.min_days = min_days;
		this.title = p.getName();
		users_lonlat_days = new HashMap<String,Map<String,Set<String>>>();
		nm = NetworkMapFactory.getInstance().getNetworkMap(new GregorianCalendar(2012,Calendar.APRIL,1));
	}
	
	private static final DecimalFormat DF = new DecimalFormat("##.###",new DecimalFormatSymbols(Locale.US));
	private static final SimpleDateFormat F = new SimpleDateFormat("dd/MM/yyyy");
	void analyze(String username, String imsi, String celllac, long timestamp, Calendar cal, String header) {
		
		if(!imsi.startsWith("222"))
			return;
		
		if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
			return;
		
		RegionI r = nm.getRegion(celllac);
		if(r==null) return;
		
		
		
		String lonlat = DF.format(r.getLatLon()[1]) +" "+ DF.format(r.getLatLon()[0]);
		Map<String,Set<String>> lonlat_days = users_lonlat_days.get(username);
		if(lonlat_days == null) {
			lonlat_days = new HashMap<String,Set<String>>();
			users_lonlat_days.put(username, lonlat_days);
		}
		Set<String> days = lonlat_days.get(lonlat);
		if(days == null) {
			days = new HashSet<String>();
			lonlat_days.put(lonlat, days);
		}
		days.add(F.format(cal.getTime()));
	}
	
	protected void finish() {
		try{
			System.out.println("N Users = "+users_lonlat_days.size());
			File d = new File(Config.getInstance().base_folder+"/PlaceRecognizer");
			d.mkdirs();
			PrintWriter out = new PrintWriter(new FileWriter(d+"/fast_home_"+title+".csv"));
			for(String user: users_lonlat_days.keySet()) {
				Map<String,Set<String>> lonlat_days = users_lonlat_days.get(user);
				String max_lonlat = null;
				for(String latlon: lonlat_days.keySet()) 
					if(lonlat_days.get(latlon).size() > min_days)
					if(max_lonlat == null || lonlat_days.get(max_lonlat).size() < lonlat_days.get(latlon).size())
						max_lonlat = latlon;
				if(max_lonlat!=null)
					out.println(user+",HOME,"+max_lonlat);
			}
			out.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	public static void main(String[] args) throws Exception {
		String region = "file_pls_lomb";
		String city = "Milano";
		Config.getInstance().pls_folder = Config.getInstance().pls_root_folder+"/"+region;
		Config.getInstance().pls_start_time = new GregorianCalendar(2014,Calendar.MARCH,1,0,0,0);
		Config.getInstance().pls_end_time = new GregorianCalendar(2014,Calendar.MARCH,30,23,59,59);
		PLSParser.MIN_HOUR = 21;
		PLSParser.MAX_HOUR = 25;
		Placemark p = Placemark.getPlacemark(city);
		new FastHome(p,20).run();
		Logger.logln("Done!");
		Mail.send("Fast Home completed!");
	}
}
