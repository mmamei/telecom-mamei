package dataset.db.insert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.GregorianCalendar;

import region.NetworkCell;
import region.RegionMap;
import utils.Config;
import utils.Logger;



public class NetworkTable {
	
	
	public static void main(String[] args) throws Exception {
		//insert();
		String bestTable = findClosestNetworkTable(new GregorianCalendar(2011,10,31));
		System.out.println(bestTable);
		RegionMap rm = getNetworkMap(new GregorianCalendar(2011,10,31));
	}

	public static void insert() throws Exception {		
		
	   Statement s = DBConnection.getStatement();
		
		File dir = new File(Config.getInstance().network_map_dir);
		String[] files = dir.list();
		for(String file: files) {
			try { 
				String time = file.substring(file.lastIndexOf("_")+1,file.indexOf(".txt"));
				int year = Integer.parseInt(time.substring(0,4));
				int month = Integer.parseInt(time.substring(4,6));
				int day = Integer.parseInt(time.substring(6,8));
				String tName = "network"+time;
				
				s.executeUpdate("drop table "+tName);
				s.executeUpdate("create table "+tName+" (celllac VARCHAR(15), description VARCHAR(10), lac INTEGER, cell_id INTEGER, radius FLOAT, center POINT NOT NULL, PRIMARY KEY (celllac)) engine=MyISAM");
				s.executeUpdate("create spatial index sp_index ON "+tName+" (center)");
								
				BufferedReader in = new BufferedReader(new FileReader(dir+"/"+file));
				String line;
				while((line = in.readLine()) != null){
					String [] splitted = line.split(":");
					String description = splitted[0];
					//int barycentre = Integer.parseInt(splitted[1]);
					long lac = Long.parseLong(splitted[2]);
					long cell_id = Long.parseLong(splitted[3]);
					//String param5 = splitted[4];
					//String param6 = splitted[5];
					//String param7 = splitted[6];
					//String param8 = splitted[7];
					//String param9 = splitted[8];
					double barycentre_lat = Double.parseDouble(splitted[9]);
					double barycentre_lon = Double.parseDouble(splitted[10]);
					double radius = Double.parseDouble(splitted[11]);
					String celllac = String.valueOf(lac*65536+cell_id);		
					
					String query = "insert ignore into "+tName+" values('"+celllac+"','"+description+"',"+lac+","+cell_id+","+radius+",GeomFromText('point("+barycentre_lon+" "+barycentre_lat+")'))";
					try {
						s.executeUpdate(query);
					}catch(SQLException e) {
						System.err.println(query);
					}
					
				}
				in.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		s.close();
		
		Logger.logln("Done");
	}
	
	
	public static RegionMap getNetworkMap(Calendar cal) {
		String bestTable = findClosestNetworkTable(cal);
		RegionMap nm = new RegionMap(bestTable);
		
		Statement s = DBConnection.getStatement();
		try {
		ResultSet r = s.executeQuery("select celllac,description,lac,cell_id,radius,AsText(center) from "+bestTable);
		while(r.next()) {
			String celllac = r.getString("celllac");
			String description = r.getString("description");
			long lac = r.getInt("lac");
			long cell_id = r.getInt("cell_id");
			double radius = r.getFloat("radius");
			String lonlat = r.getString("AsText(center)");
			double lon = Double.parseDouble(lonlat.substring(lonlat.indexOf("(")+1,lonlat.indexOf(" ")));
			double lat = Double.parseDouble(lonlat.substring(lonlat.indexOf(" ")+1,lonlat.indexOf(")")));
			nm.add(new NetworkCell(celllac,description,lac,cell_id,lat,lon,radius));
		}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return nm;
	}
	

	public static String findClosestNetworkTable(Calendar target_cal) {
		String bestTable = null;
		Statement s = DBConnection.getStatement();
		try{
			ResultSet r = s.executeQuery("SELECT table_name FROM information_schema.tables");
			while(r.next()) {
				String tableName = r.getString("table_name"); 
				if(tableName.startsWith("network")) {
					Calendar cal = getCalendar(tableName);
					long dt = Math.abs(cal.getTimeInMillis() - target_cal.getTimeInMillis());
					if(bestTable == null || (cal.before(target_cal) && dt < Math.abs(getCalendar(bestTable).getTimeInMillis() - target_cal.getTimeInMillis())) )
						bestTable = tableName;
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return bestTable;
	}
	
	private static Calendar getCalendar(String tableName) {
		String time = tableName.substring("network".length());
		int year = Integer.parseInt(time.substring(0,4));
		int month = Integer.parseInt(time.substring(4,6));
		int day = Integer.parseInt(time.substring(6,8));
		return new GregorianCalendar(year,month,day);
	}
}
