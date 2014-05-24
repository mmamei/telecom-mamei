package dataset.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import region.Placemark;
import utils.FileUtils;
import utils.Logger;

public class UserEventCounterCellacXHour extends BufferAnalyzerConstrained {
	
	static final String[] MONTHS = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	static final String[] DAY_WEEK = new String[]{"0","Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
	
	
	private Map<String,UserInfo> users_info;

	
	UserEventCounterCellacXHour(String placemark_name, String user_list_name) {
		super(placemark_name,user_list_name);
		users_info = new HashMap<String,UserInfo>();
	}
	
	
	UserInfo info;
	String day,dayw;
	void analyze(String username, String imsi, String celllac, long timestamp, Calendar cal,String header) {
		info = users_info.get(username);
		if(info == null) {
			info = new UserInfo();
			info.imsi = imsi;
			users_info.put(username, info);
		}
		day = cal.get(Calendar.YEAR)+"-"+TwoDigits(cal.get(Calendar.MONTH))+"-"+TwoDigits(cal.get(Calendar.DAY_OF_MONTH));
		dayw = DAY_WEEK[cal.get(Calendar.DAY_OF_WEEK)];
		info.add(day,dayw,cal.get(Calendar.HOUR_OF_DAY),celllac);
	}
	
	// convert 1 to 01
	private String TwoDigits(int x) {
		return x < 10 ? "0"+x : ""+x; 
	}
	
	Set<String> days;
	private int getTotDays() {
		days = new HashSet<String>();
		for(UserInfo ui:users_info.values()) {
			days.addAll(ui.getDays());
		}
		return days.size();
	}
	
	//private static final SimpleDateFormat F = new SimpleDateFormat("dd/MM/yyyy");
	protected void finish() {
		try {
			File dir = FileUtils.createDir("BASE/UserEventCounter");
			PrintWriter out = new PrintWriter(new FileWriter(dir+"/"+this.getString()+"_cellXHour.csv"));
			out.println("// TOT. DAYS = "+getTotDays());
			for(String user: users_info.keySet())
				if(users_info.get(user).getNumDays() >= 14)
					out.println(user+","+users_info.get(user));
			out.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		//BufferAnalyzerConstrained ba = new UserEventCounterCellacXHour(null,FileUtils.getFileS("UserSetCreator/Firenze.csv"));
		BufferAnalyzerConstrained ba = new UserEventCounterCellacXHour("Torino",null);
		ba.run();
		Logger.logln("Done!");
	}	
	
	
	/*
	 * This main is places here for convenience. It just read the file and remove all the users producing few events
	 */
	public static void trim(Placemark p, int min_size) throws Exception {
		File dir = FileUtils.createDir("BASE/UserEventCounter");
		BufferedReader br = new BufferedReader(new FileReader(dir+"/"+p.getName()+"_cellacXhour.csv"));
		PrintWriter out = new PrintWriter(new FileWriter(dir+"/"+p.getName()+"_cellacXhour_trim"+min_size+".csv"));
		String line;
		while((line = br.readLine()) != null) {
			int num_pls = Integer.parseInt(line.split(",")[2]);
			if(num_pls >= min_size)
				out.println(line);
		}
		br.close();
		out.close();
		Logger.logln("Done!");
	}
	
	/***********************************************************************************************************************/
	/* USER INFO INNER CLASS */
	
	private class UserInfo {
		
		private String imsi;
		private List<String> pls = new ArrayList<String>();
		
		String s;
		public void add(String day, String dayw, int h, String cellac) {
			s = day+":"+dayw+":"+h+":"+celllac;
			if(!pls.contains(s)) pls.add(s);
		}
		
		public int getNumDays() {
			return getDays().size();
		}
		
		
		public Set<String> getDays() {
			Set<String> days = new HashSet<String>();
			for(String p:pls) 
				days.add(p.substring(0, p.indexOf(":")));
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
			for(String p:pls) {
				String[] day = p.substring(0, p.indexOf(":")).split("-"); // cal.get(Calendar.YEAR)+"-"+cal.get(Calendar.MONTH)+"-"+cal.get(Calendar.DAY_OF_MONTH);
				int year = Integer.parseInt(day[0]);
				int month = Integer.parseInt(day[1]);
				int d = Integer.parseInt(day[2]);
				Calendar c = new GregorianCalendar(year,month,d);
				if(min == null || c.before(min)) min = c;
				if(max == null || c.after(max)) max = c;
			}
			return new Calendar[]{min,max};
		}
		
		
		public String toString() {			
			StringBuffer sb = new StringBuffer();
			for(String p:pls) 
				sb.append(p+",");
			return imsi+","+pls.size()+","+getNumDays()+","+getDaysInterval()+","+sb.toString();
		}
	}

}