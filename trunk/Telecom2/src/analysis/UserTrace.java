package analysis;


import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import utils.Logger;

public class UserTrace implements Serializable {
	
	static final SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
	static Calendar cal = Calendar.getInstance();
	
	public String username;
	public String mnt;
	private Set<PLSEvent> events;
	private boolean max_one_event_per_hour;
	
	public UserTrace(String username, boolean max_one_event_per_hour){
		this.username = username;
		this.max_one_event_per_hour = max_one_event_per_hour;
		events = new TreeSet<PLSEvent>();
	}
	
	public void addEvent(String mnt, String cellac, String timestamp){
		this.mnt = mnt;
		
		cal.setTimeInMillis(Long.parseLong(timestamp));
		PLSEvent pe = new PLSEvent(username, mnt, cellac, timestamp,max_one_event_per_hour);
		if(!max_one_event_per_hour || (max_one_event_per_hour && !events.contains(pe))) 
			events.add(pe);
	}
	
	
	/*protected boolean isTimeAvailableH(Calendar c) {
		SimpleDateFormat sdh = new SimpleDateFormat("yyyy-MM-dd-HH");
		String x = sdh.format(c.getTime());
		for(PLSEvent p: events) {
			cal.setTimeInMillis(p.getTimeStamp());
			String y = sdh.format(cal.getTime());
			if(y.equals(x)) return false;
		}
		return true;
	} 
	*/
	
	public List<PLSEvent> getEvents(){
		List<PLSEvent> l = new ArrayList<PLSEvent>();
		l.addAll(events);
		return l;
	}
	
	public String getUsername(){
		return username;
	}
	
	public boolean hasEvents(){
		if(events.size()>0){
			return true;
		}
		return false;
	}
	
	public int getNumDays() {
		return getDays().size();
	}
	
	
	
	
	public Set<String> getDays() {
		Set<String> days = new HashSet<String>();
		for(PLSEvent p:events) {
			cal.setTimeInMillis(p.getTimeStamp());
			days.add(sd.format(cal.getTime()));
		}
		return days;
	}
	
	
	public int getDaysInterval() {
		Calendar[] min_max = getTimeRange();
		Calendar min = min_max[0];
		Calendar max = min_max[1];
		return 1+(int)Math.floor((max.getTimeInMillis() - min.getTimeInMillis())/(1000*3600*24));
	}
	
	public Calendar[] getTimeRange() {
		Calendar min = null;
		Calendar max = null;
		Calendar c = Calendar.getInstance();
		for(PLSEvent p:events) {
			c.setTimeInMillis(p.getTimeStamp());
			if(min == null || c.before(min)) {min = Calendar.getInstance(); min.setTimeInMillis(c.getTimeInMillis());}
			if(max == null || c.after(max)) {max = Calendar.getInstance(); max.setTimeInMillis(c.getTimeInMillis());}
		}
		return new Calendar[]{min,max};
	}

	
	public String toString() {			
		StringBuffer sb = new StringBuffer();
		for(PLSEvent p:events) {
			sb.append(username+","+p.getTimeStamp()+","+mnt+","+p.getCellac()+"\n");
		}
		return sb.toString();
		
	}
	
	
	public String getInfoCellXHour() {		
		SimpleDateFormat sdh = new SimpleDateFormat("yyyy-MM-dd:EEE:HH",Locale.US);
		StringBuffer sb = new StringBuffer();
		
		
		for(PLSEvent p:events) {	
			cal.setTimeInMillis(p.getTimeStamp());			
			sb.append(sdh.format(cal.getTime())+":"+p.getCellac()+",");
		}
		
		return mnt+","+events.size()+","+getNumDays()+","+getDaysInterval()+","+sb.toString();
	}
	
	
	
	
	public void saveToCSV(String folder) throws Exception{
		if(!folder.endsWith("/"))
			folder = folder+"/";
		
		Logger.logln("Saving "+folder+username+".csv");
		PrintWriter out = new PrintWriter(new FileWriter(folder+username+".csv"));
		Iterator<PLSEvent> i = events.iterator();
		while(i.hasNext())
			out.println(i.next().toCSV());
		out.close();
	}
}
