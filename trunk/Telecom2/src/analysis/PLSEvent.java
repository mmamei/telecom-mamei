package analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gps.utils.LatLonUtils;

import dataset.db.NetworkMapFactory;
import region.RegionI;
import region.RegionMap;
import utils.Config;

public class PLSEvent implements Comparable<PLSEvent>, Cloneable, Serializable {
	
	private static RegionMap NM = null;
	private String username;
	private String imsi;
	private String cellac;
	private long timestamp;
	
	public PLSEvent(String username, String imsi, String cellac, String timestamp){
		this.username = username;
		this.imsi = imsi;
		this.cellac = cellac;
		this.timestamp = Long.parseLong(timestamp);
		if(NM == null) NM = NetworkMapFactory.getNetworkMap(getCalendar());
	}
	
	public PLSEvent clone(){
		try {
			return (PLSEvent)super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
	
	public int compareTo(PLSEvent e) {
		if((timestamp - e.getTimeStamp()) > 0)
			return 1;
		if((timestamp - e.getTimeStamp()) < 0)
			return -1;
		if(this.equals(e))
			return 0;
		return 1;
	}
	
	public boolean equals(Object e){
		PLSEvent o = (PLSEvent) e;
		return (o.getCellac().equals(cellac) && o.getIMSI().equals(imsi) && o.timestamp==timestamp && o.getUsername().equals(username));
	}
	

	public String getIMSI(){
		return imsi;
	}
	public Calendar getCalendar(){
		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(this.getTimeStamp());
		return cal;
	}
	
	public String getCellac(){
		return cellac;
	}
	public void setCellac(String celllac){
		this.cellac = celllac;
	}
	
	private static final SimpleDateFormat F = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	
	public String getTime(){
		//if(getCalendar().get(Calendar.DAY_OF_MONTH) < 10) return "0"+F.format(getCalendar().getTime());
		return F.format(getCalendar().getTime());
	}

	
	public long getTimeStamp(){
		return timestamp;
	}
	public String getUsername(){
		return username;
	}
	
	public String toKml(){
		RegionI c = NM.getRegion(cellac);
		return c.toKml("#7f770077");
	}
	
	public String toString(){
		RegionI nc = NM.getRegion(cellac);
		String cn = nc == null ? "null" : nc.getName();
		return username+","+getCalendar().getTime()+","+imsi+","+cellac+","+cn;
	}
	
	public String toCSV(){
		RegionI nc = NM.getRegion(cellac);
		String cn = nc == null ? "null" : nc.getName();
		return username+","+getCalendar().getTimeInMillis()+","+imsi+","+cellac+","+cn;
	}
	
	public static List<PLSEvent> readEvents(File f) throws Exception {
		return readEvents(f,Config.getInstance().pls_start_time,Config.getInstance().pls_end_time);
		
	}
	
	public static List<PLSEvent> readEvents(File base_f, Calendar start, Calendar end) {
		File[] files = null;
		if(base_f.isFile()) files = new File[]{base_f};
		if(base_f.isDirectory()) files = base_f.listFiles();
		
		List<PLSEvent> events = new ArrayList<PLSEvent>();
		Calendar cal = new GregorianCalendar();
		for(File fx: files) {
			String line = null;
			try{
				BufferedReader in = new BufferedReader(new FileReader(fx));
				while((line = in.readLine()) != null){
					line = line.trim();
					if(line.length() < 1) continue; // extra line at the end of file
					String[] splitted = line.split(",");
					if(splitted.length == 5) {
						if(splitted[3].equals("null")) continue;
						cal.setTimeInMillis(Long.parseLong(splitted[1]));
						if(start.before(cal) && end.after(cal)) {
							PLSEvent e = new PLSEvent(splitted[0], splitted[2], splitted[3], splitted[1]);
							events.add(e);
						}
					}
					//else System.out.println("Problems: "+line+" in "+fx.getAbsolutePath());
				}
				in.close();
			} catch(Exception e) {
				System.err.println(fx+": file length =  "+fx.length());
				System.err.println(line);
				e.printStackTrace();
				System.exit(0);
			}
		}
		Collections.sort(events);
		return events;
	}
	
	
	public static int countDays(List<PLSEvent> pe) {
		Set<String> days = new HashSet<String>();
		for(PLSEvent e: pe) {
			int y = e.getCalendar().get(Calendar.YEAR);
			int m = e.getCalendar().get(Calendar.MONTH);
			int d = e.getCalendar().get(Calendar.DAY_OF_MONTH);
			days.add(y+"-"+m+"-"+d);
		}
		return days.size();
	}
	
	
	
	
	public static List<PLSEvent> clone(List<PLSEvent> pe) {
		List<PLSEvent> c = new ArrayList<PLSEvent>();
		for(PLSEvent e: pe)
			c.add(e.clone());
		return c;
	}

	
	public double spatialDistance(PLSEvent x) {		
		return LatLonUtils.getHaversineDistance(NM.getRegion(cellac).getCenterPoint(),NM.getRegion(x.cellac).getCenterPoint());
	}
	
}