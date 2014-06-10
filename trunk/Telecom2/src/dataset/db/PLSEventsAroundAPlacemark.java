package dataset.db;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Set;

import region.Placemark;
import utils.Config;
import analysis.Constraints;
import dataset.PLSEventsAroundAPlacemarkI;
import dataset.db.insert.DBConnection;
import dataset.db.insert.PLSTable;

 class PLSEventsAroundAPlacemark implements PLSEventsAroundAPlacemarkI {	


	public void process(Placemark p, Constraints constraints) throws Exception {
				
		File dir = new File(Config.getInstance().base_folder+"/PLSEventsAroundAPlacemark");
		dir.mkdirs();
		File fd = new File(dir+"/"+Config.getInstance().get_pls_subdir());
		if(!fd.exists()) fd.mkdirs();	
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(dir+"/"+p.getName()+"_"+p.getRadius()+".txt"))));
		System.out.println(dir+"/"+p.getName()+"_"+p.getRadius()+".txt");
	
		Set<String> cells = p.cellsAround;
		StringBuffer sb = new StringBuffer();
		for(String c: cells)
		sb.append(",'"+c+"'");
		String in = sb.substring(1);
		
		
		String f = Config.getInstance().pls_root_folder;
		System.out.println(f);
		f = f.substring("G:/DATASET/PLS/file_pls/file_".length());
		Set<String> tables = PLSTable.getTables(f, Config.getInstance().pls_start_time, Config.getInstance().pls_end_time);
		
		System.out.println("TABLES = "+tables);
		
		Statement s = DBConnection.getStatement();
		for(String table: tables) {
			Timestamp st = new Timestamp(Config.getInstance().pls_start_time.getTimeInMillis());
			Timestamp et = new Timestamp(Config.getInstance().pls_end_time.getTimeInMillis());
			
			String query = "select * from "+table+" where celllac in ("+in+") and (time >= '"+st+"' and time <= '"+et+"') order by time";
			//System.out.println(query);
			ResultSet r = s.executeQuery(query);
			Calendar cal = Calendar.getInstance();
			while(r.next()) {
				
				String username = r.getString("username");
				cal.setTimeInMillis(r.getTimestamp("time").getTime());
				String imsi = r.getString("imsi");
				String celllac = r.getString("celllac");
				
				out.println(username+","+cal.getTimeInMillis()+","+imsi+","+celllac+",null");
			}
			r.close();
		}
		
		
		s.close();
		DBConnection.closeConnection();
		out.close();
	}
}
