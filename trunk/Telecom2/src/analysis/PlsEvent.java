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

import network.NetworkCell;
import network.NetworkMap;
import network.NetworkMapFactory;

import org.gps.utils.LatLonUtils;

import utils.Config;

public class PlsEvent implements Comparable<PlsEvent>, Cloneable, Serializable {
	
	static NetworkMap NM = null;
	
	private String username;
	private String imsi;
	private long cellac;
	private long timestamp;
	
	public PlsEvent(String username, String imsi, long cellac, String timestamp){
		this.username = username;
		this.imsi = imsi;
		this.cellac = cellac;
		this.timestamp = Long.parseLong(timestamp);
		if(NM == null) NM = NetworkMapFactory.getNetworkMap(getTime());
	}
	
	public PlsEvent clone(){
		try {
			return (PlsEvent)super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
	
	public int compareTo(PlsEvent e) {
		if((timestamp - e.getTimeStamp()) > 0)
			return 1;
		if((timestamp - e.getTimeStamp()) < 0)
			return -1;
		if(this.equals(e))
			return 0;
		return 1;
	}
	
	public boolean equals(Object e){
		PlsEvent o = (PlsEvent) e;
		return (o.getCellac()==cellac && o.getIMSI().equals(imsi) && o.timestamp==timestamp && o.getUsername().equals(username));
	}
	

	public String getIMSI(){
		return imsi;
	}
	public Calendar getCalendar(){
		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(this.getTimeStamp());
		return cal;
	}
	
	public long getCellac(){
		return cellac;
	}
	public void setCellac(long celllac){
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
		NetworkCell c = NM.get(cellac);
		return c.toKml();
	}
	
	public String toString(){
		NetworkCell nc = NM.get(cellac);
		String cn = nc == null ? "null" : nc.getCellName();
		return username+","+getCalendar().getTime()+","+imsi+","+cellac+","+cn;
	}
	
	public String toCSV(){
		NetworkCell nc = NM.get(cellac);
		String cn = nc == null ? "null" : nc.getCellName();
		return username+","+getCalendar().getTimeInMillis()+","+imsi+","+cellac+","+cn;
	}
	
	public static List<PlsEvent> readEvents(File f) throws Exception {
		return readEvents(f,Config.getInstance().pls_start_time,Config.getInstance().pls_end_time);
		
	}
	
	public static List<PlsEvent> readEvents(File base_f, Calendar start, Calendar end) {
		File[] files = null;
		if(base_f.isFile()) files = new File[]{base_f};
		if(base_f.isDirectory()) files = base_f.listFiles();
		
		List<PlsEvent> events = new ArrayList<PlsEvent>();
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
							PlsEvent e = new PlsEvent(splitted[0], splitted[2], Long.parseLong(splitted[3]), splitted[1]);
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
	
	
	public static int countDays(List<PlsEvent> pe) {
		Set<String> days = new HashSet<String>();
		for(PlsEvent e: pe) {
			int y = e.getCalendar().get(Calendar.YEAR);
			int m = e.getCalendar().get(Calendar.MONTH);
			int d = e.getCalendar().get(Calendar.DAY_OF_MONTH);
			days.add(y+"-"+m+"-"+d);
		}
		return days.size();
	}
	
	
	
	
	public static List<PlsEvent> clone(List<PlsEvent> pe) {
		List<PlsEvent> c = new ArrayList<PlsEvent>();
		for(PlsEvent e: pe)
			c.add(e.clone());
		return c;
	}

	
	public double spatialDistance(PlsEvent x) {		
		return LatLonUtils.getHaversineDistance(NM.get(cellac).getPoint(),NM.get(x.cellac).getPoint());
	}
	
}
