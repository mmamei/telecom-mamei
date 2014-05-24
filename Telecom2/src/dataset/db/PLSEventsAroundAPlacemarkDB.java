package dataset.db;

import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import region.Placemark;
import utils.Config;
import analysis.PLSMap;
import dataset.db.insert.DBConnection;

public class PLSEventsAroundAPlacemarkDB {	

	private static SimpleDateFormat F2 = new SimpleDateFormat("yyyyMMdd");
	public static PLSMap processdb(Placemark p) throws Exception {
		
		Set<String> cells = p.cellsAround;
		StringBuffer sb = new StringBuffer();
		for(String c: cells)
		sb.append(",'"+c+"'");
		String in = sb.substring(1);
		
		PLSMap plsmap = new PLSMap();
		Statement s = DBConnection.getStatement();
		
		
		// get the right tables
		Set<String> tables = new HashSet<String>();
		
		Calendar c = (Calendar)Config.getInstance().pls_start_time.clone();
		while(!c.after(Config.getInstance().pls_end_time)) {
			tables.add( "pls_ve_"+F2.format(c.getTime()));
			c.add(Calendar.DAY_OF_MONTH, 1);
		}
		
		
		System.out.println(tables);
		
		
		
		
		for(String table: tables) {
			Timestamp st = new Timestamp(Config.getInstance().pls_start_time.getTimeInMillis());
			Timestamp et = new Timestamp(Config.getInstance().pls_end_time.getTimeInMillis());
			
			String query = "select * from "+table+" where celllac in ("+in+") and (time between '"+st+"' and '"+et+"')";
			//System.out.println(query);
			ResultSet r = s.executeQuery(query);
			Calendar cal = Calendar.getInstance();
			while(r.next()) {
				
				String username = r.getString("username");
				cal.setTimeInMillis(r.getTimestamp("time").getTime());
				String key = getKey(cal);
				
				if(plsmap.startTime == null || plsmap.startTime.after(cal)) plsmap.startTime = (Calendar)cal.clone();
				if(plsmap.endTime == null || plsmap.endTime.before(cal)) plsmap.endTime = (Calendar)cal.clone();
				
				Set<String> users = plsmap.usr_counter.get(key);
				if(users == null) users = new TreeSet<String>();
				users.add(username);
				plsmap.usr_counter.put(key, users);
				Integer count = plsmap.pls_counter.get(key);
				plsmap.pls_counter.put(key, count == null ? 0 : count+1);	
			}
			
			r.close();
		}
		
		
		s.close();
		DBConnection.closeConnection();
		
		return plsmap;
		
		
	}
	
	
	static final String[] MONTHS = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	public static String getKey(Calendar cal) {
		return cal.get(Calendar.DAY_OF_MONTH)+"-"+
			 	MONTHS[cal.get(Calendar.MONTH)]+"-"+
			 	cal.get(Calendar.YEAR)+":"+
			 	cal.get(Calendar.HOUR_OF_DAY);
	}
	
}
