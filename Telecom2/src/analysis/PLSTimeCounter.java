package analysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.gps.utils.LatLonPoint;
import org.gps.utils.LatLonUtils;

import region.Placemark;
import utils.Config;
import utils.FileUtils;
import utils.Logger;
import dataset.DataFactory;
import dataset.PLSEventsAroundAPlacemarkI;
import dataset.file.EventFilesFinder;



public class PLSTimeCounter implements Serializable {
	public Map<String,Set<String>> usr_counter;
	public Map<String,Integer> pls_counter;
	public Calendar startTime = null;
	public Calendar endTime = null;
	
	public PLSTimeCounter() {
		this.usr_counter = new TreeMap<String,Set<String>>();
		this.pls_counter  = new TreeMap<String,Integer>();
	}
	
	 
	
	public int getHours() {
		return (int)Math.ceil((1.0*(endTime.getTimeInMillis() - startTime.getTimeInMillis()) / 3600000));
	}
	
	public String[] getDomain() {
		Calendar cal = (Calendar)startTime.clone();
		String[] domain = new String[getHours()];
		for(int i=0; i<domain.length;i++) {
			domain[i] = getLabel(cal);
			cal.add(Calendar.HOUR_OF_DAY, 1);
		}
		return domain;
	}
	static final String[] DAYS = new String[]{"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
	private String getLabel(Calendar cal) {
		//return "-"+cal.get(Calendar.DAY_OF_MONTH)+":"+DAYS[cal.get(Calendar.DAY_OF_WEEK)-1]+"-";
		return "["+cal.get(Calendar.DAY_OF_MONTH)+"-"+DAYS[cal.get(Calendar.DAY_OF_WEEK)-1]+":"+cal.get(Calendar.HOUR_OF_DAY)+"]";
	}
	
	
	
	
	public static PLSTimeCounter getPLSTimeCounter(String file, Placemark within) {
		
		//System.out.println(file);
		//System.out.println(FileUtils.getFile(file));
		
		PLSTimeCounter plsmap = new PLSTimeCounter();
		String[] splitted;
		String line;
		
		Calendar cal = new GregorianCalendar();
		try {
			BufferedReader in = new BufferedReader(new FileReader(FileUtils.getFile(file)));
			while((line = in.readLine()) != null){
				line = line.trim();
				if(line.length() < 1) continue; // extra line at the end of file
				splitted = line.split(",");
				if(splitted.length == 5 && !splitted[3].equals("null")) {
					
					String username = splitted[0];
					cal.setTimeInMillis(Long.parseLong(splitted[1]));
					String key = getKey(cal);
					String celllac = splitted[3]; 
					if(within==null || within.contains(celllac)) {				
						if(plsmap.startTime == null || plsmap.startTime.after(cal)) plsmap.startTime = (Calendar)cal.clone();
						if(plsmap.endTime == null || plsmap.endTime.before(cal)) plsmap.endTime = (Calendar)cal.clone();
						Set<String> users = plsmap.usr_counter.get(key);
						if(users == null) users = new TreeSet<String>();
						users.add(username);
						plsmap.usr_counter.put(key, users);
						Integer count = plsmap.pls_counter.get(key);
						plsmap.pls_counter.put(key, count == null ? 0 : count+1);	
					}
				}
			}
			in.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return plsmap;
	}
	
	
	public static DescriptiveStatistics[] getStats(PLSTimeCounter plsmap) {
		DescriptiveStatistics pls_stats = new DescriptiveStatistics();
		DescriptiveStatistics usr_stats = new DescriptiveStatistics();
		
		Calendar cal = (Calendar)plsmap.startTime.clone();
		while(!cal.after(plsmap.endTime)) {
			String key = getKey(cal);
			Integer pls_count = plsmap.pls_counter.get(key);
			double pls = pls_count == null ? 0 : (double)pls_count;
			double usr = plsmap.usr_counter.get(key) == null ? 0 : plsmap.usr_counter.get(key).size();	
			pls_stats.addValue(pls);
			usr_stats.addValue(usr);
			cal.add(Calendar.HOUR, 1);
		}
		return new DescriptiveStatistics[]{pls_stats,usr_stats};
	}
	
	
	public String getJSMap(Object[] plsDayCounter) {
		String[] domain = (String[])plsDayCounter[0];
		double[] data = (double[])plsDayCounter[1];
		StringBuffer sb = new StringBuffer();
		sb.append("var data = google.visualization.arrayToDataTable([['Day', 'PLS']");
		for(int i=0; i<domain.length;i++) {
			sb.append(",['"+domain[i].substring(1,domain[i].length()-1)+"',  "+data[i]+"]");
		}
		sb.append("]);");
		return sb.toString();
	}
	
	
	static final String[] MONTHS = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	public static String getKey(Calendar cal) {
		return cal.get(Calendar.DAY_OF_MONTH)+"-"+
			 	MONTHS[cal.get(Calendar.MONTH)]+"-"+
			 	cal.get(Calendar.YEAR)+":"+
			 	cal.get(Calendar.HOUR_OF_DAY);
	}
	
	
	private static final SimpleDateFormat F = new SimpleDateFormat("yyyy-MM-dd-hh");
	public Object[] process(String sday,String shour,String eday, String ehour, double lon1, double lat1, double lon2, double lat2) {
		try {
			EventFilesFinder eff = new EventFilesFinder();
			String dir = eff.find(sday,shour,eday,ehour,lon1,lat1,lon2,lat2);
			if(dir == null) return null;
				
			Config.getInstance().pls_folder = FileUtils.getFile("DATASET/PLS/file_pls/"+dir).toString(); 
			Config.getInstance().pls_start_time.setTime(F.parse(sday+"-"+shour));
			Config.getInstance().pls_end_time.setTime(F.parse(eday+"-"+ehour));
			Config.getInstance().pls_end_time.add(Calendar.HOUR_OF_DAY, 1); // add one to take into account minutes and seconds
			double lon = (lon1+lon2)/2;
			double lat = (lat1+lat2)/2;
			LatLonPoint p1 = new LatLonPoint(lat1,lon1);
			LatLonPoint p2 = new LatLonPoint(lat2,lon2);
			int r = (int)LatLonUtils.getHaversineDistance(p1, p2) / 2;
			String n = "tmp";
			Placemark p = new Placemark(n,new double[]{lat,lon},r);
			
			
			DataFactory.getPLSEventsAroundAPlacemark().process(p);
			String file = "BASE/PLSEventsAroundAPlacemark/"+Config.getInstance().get_pls_subdir()+"/"+p.getName()+"_"+p.getRadius()+".txt";
			PLSTimeCounter plsmap = getPLSTimeCounter(file,null);
			//(new File(file)).delete();
			
			
			
			DescriptiveStatistics[] stats = getStats(plsmap);
			
			/*
			// compute z
			//double[] pls_data = stats[0].getValues();
			double[] usr_data = stats[1].getValues();
			//double[] z_pls_data = getZ(stats[0],plsmap.startTime);
			double[] z_usr_data =  StatsUtils.getZH(stats[1],plsmap.startTime);
			*/
			
			
			return new Object[]{plsmap.getDomain(),stats[1].getValues()}; // domain, user_stat		
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) throws Exception { 
		
		PLSTimeCounter pbia = new PLSTimeCounter();
		//Object[] plsdata = pbia.process("2014-03-13","0","2014-03-15","10",12.3238,45.4425,12.3238,45.4425);
		Object[] plsdata = pbia.process("2014-03-10","18","2014-03-11","1",11.2523,43.7687,11.2545,43.7672);
		
		
		
		if(plsdata!=null)
			System.out.println(pbia.getJSMap(plsdata));
		Logger.logln("Done!");
	}
	
}
