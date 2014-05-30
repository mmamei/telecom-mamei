package dataset.db;

import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import region.CityEvent;
import utils.Config;
import dataset.UsersAroundAnEventI;
import dataset.db.insert.DBConnection;
import dataset.db.insert.PLSTable;

class UsersAroundAnEvent implements UsersAroundAnEventI {

	@Override
	public Set<String> process(CityEvent ce) {
		Set<String> users = new HashSet<String>();
		EventFilesFinder eff = new EventFilesFinder();
		String f = eff.find(ce.st, ce.et, ce.spot.getLatLon()[1], ce.spot.getLatLon()[0], ce.spot.getLatLon()[1], ce.spot.getLatLon()[0]);
		if(f == null) 
			return users;
		
		Set<String> tables = PLSTable.getTables(f, ce.st, ce.et);
		
		//System.out.println("TABLES = "+tables);
		
		Set<String> cells = ce.spot.cellsAround;
		StringBuffer sb = new StringBuffer();
		for(String c: cells)
		sb.append(",'"+c+"'");
		String in = sb.substring(1);
		
		
		Statement s = DBConnection.getStatement();
		for(String table: tables) {
			try {
				Timestamp st = new Timestamp(Config.getInstance().pls_start_time.getTimeInMillis());
				Timestamp et = new Timestamp(Config.getInstance().pls_end_time.getTimeInMillis());
				
				String query = "select distinct username from "+table+" where celllac in ("+in+") and (time >= '"+st+"' and time <= '"+et+"')";
				//System.out.println(query);
				ResultSet r = s.executeQuery(query);
				while(r.next()) {
					users.add(r.getString("username"));
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return users;
	}
}
