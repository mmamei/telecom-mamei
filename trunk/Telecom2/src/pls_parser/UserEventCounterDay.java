package pls_parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import region.Placemark;
import utils.Config;
import utils.FileUtils;
import utils.Logger;

public class UserEventCounterDay extends BufferAnalyzerConstrained {
	
	static final String[] MONTHS = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	
	
	private Map<String,UserInfo> users_info;
	
	UserEventCounterDay(String placemark_name, String user_list_name) {
		super(placemark_name,user_list_name);
		users_info = new HashMap<String,UserInfo>();
	}
	
	
	
	void analyze(String username, String imsi, String celllac, long timestamp, Calendar cal,String header) {
		UserInfo info = users_info.get(username);
		if(info == null) {
			info = new UserInfo();
			info.imsi = imsi;
			users_info.put(username, info);
		}
		info.num_pls++;
		info.time_of_first_pls = Math.min(info.time_of_first_pls,timestamp);
		info.time_of_last_pls = Math.max(info.time_of_last_pls, timestamp);
		info.days.add(dayString(cal));
	}
	
	
	private String dayString(Calendar c) {
		return c.get(Calendar.YEAR)+"-"+c.get(Calendar.MONTH)+"-"+c.get(Calendar.DAY_OF_MONTH);
	}
	
	
	void finish() {
		try {
			File dir = FileUtils.createDir("BASE/UserEventCounter");
			PrintWriter out = new PrintWriter(new FileWriter(dir+"/"+this.getString()+"_day.csv"));
			for(String user: users_info.keySet())
				out.println(user+","+users_info.get(user));
			out.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		String city = "Torino";
		BufferAnalyzerConstrained ba = new UserEventCounterDay(null,FileUtils.getFile("BASE/UserSetCreator")+"/"+city+".csv");
		ba.run();
		Logger.logln("Done!");
	}
	
	
	/***********************************************************************************************************************/
	/* USER INFO INNER CLASS */
	
	private class UserInfo {
		
		String imsi;
		long time_of_first_pls = Long.MAX_VALUE;
		long time_of_last_pls = 0;
		int num_pls = 0;
		Set<String> days = new HashSet<String>();
		
		public String toString() {
			
			Calendar first = Calendar.getInstance();
			first.setTimeInMillis(time_of_first_pls);
			
			Calendar last = Calendar.getInstance();
			last.setTimeInMillis(time_of_last_pls);
			
			int dt = (int)((time_of_last_pls - time_of_first_pls)/(1000*3600*24));
			
			int sday =  first.get(Calendar.DAY_OF_MONTH);
			int smonth = first.get(Calendar.MONTH);
			int syear = first.get(Calendar.YEAR);
			
			int eday =  last.get(Calendar.DAY_OF_MONTH);
			int emonth = last.get(Calendar.MONTH);
			int eyear = last.get(Calendar.YEAR);
			
			return imsi+","+num_pls+","+days.size()+","+dt+","+sday+"-"+MONTHS[smonth]+"-"+syear+","+eday+"-"+MONTHS[emonth]+"-"+eyear;
		}
	}
}


