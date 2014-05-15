package db;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Set;

import region.RegionI;
import region.RegionMap;
import region.network.NetworkCell;
import utils.Config;
import utils.Logger;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;

public class NetworkTable {
	
	public static void main(String[] args) {
		insert();
		String bestTable = findClosestNetworkTable(new GregorianCalendar(2011,10,31));
		System.out.println(bestTable);
	}


	public static void insert() {		
		MongoClient mongo = null;
		try {
			mongo = new MongoClient( "localhost" , 27017 );
			mongo.setWriteConcern(WriteConcern.UNACKNOWLEDGED);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return;
		}
		DB dbt = mongo.getDB("telecom");
		
		File dir = new File(Config.getInstance().network_map_dir);
		String[] files = dir.list();
		for(String file: files) {
			try { 
				String time = file.substring(file.lastIndexOf("_")+1,file.indexOf(".txt"));
				int year = Integer.parseInt(time.substring(0,4));
				int month = Integer.parseInt(time.substring(4,6));
				int day = Integer.parseInt(time.substring(6,8));
				String tName = "network"+time;
				DBCollection t = dbt.getCollection(tName);
				t.drop();
				t = dbt.getCollection(tName);
				t.createIndex(new BasicDBObject("celllac", 1).append("time", 1),new BasicDBObject().append("unique", true).append("dropDups", true));
				t.createIndex(new BasicDBObject("loc", "2dsphere"));
				
				
				Date date = new GregorianCalendar(year,month-1,day).getTime();
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
					
					t.insert(new BasicDBObject("celllac",celllac).append("time", date).
						    append("description", description).append("lac", lac).append("cell_id", cell_id).append("radius", radius).
						    append("loc", new BasicDBObject().append("type", "Point").append("coordinates", new double[]{barycentre_lon,barycentre_lat})));
					
				}
				in.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		Logger.logln("Done");
	}
	
	
	
	public static RegionMap getNetworkMap(Calendar cal) {
		String bestTable = findClosestNetworkTable(cal);
		RegionMap nm = new RegionMap(bestTable);
		Iterable<DBObject> iter = query(bestTable,null);
		for(DBObject bson: iter) {
			String celllac = (String)bson.get("celllac");
			String description = (String)bson.get("description");
			long lac = (Long)bson.get("lac");
			long cell_id = (Long)bson.get("cell_id");
			BasicDBList lonlat = (BasicDBList)((BasicDBObject)bson.get("loc")).get("coordinates");
			double barycentre_lon = (Double)lonlat.get(0);
			double barycentre_lat = (Double)lonlat.get(1);
			double radius = (Double)bson.get("radius");
			nm.add(new NetworkCell(celllac,description,lac,cell_id,barycentre_lat,barycentre_lon,radius));
		}
		return nm;
	}
	

	public static String findClosestNetworkTable(Calendar target_cal) {
		MongoClient mongo = null;
		try {
			mongo = new MongoClient( "localhost" , 27017 );
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		}
		DB dbt = mongo.getDB("telecom");
		Set<String> tables = dbt.getCollectionNames();
		String bestTable = null;
		for(String tableName : tables){
			if(tableName.startsWith("network")) {
				Calendar cal = getCalendar(tableName);
				long dt = Math.abs(cal.getTimeInMillis() - target_cal.getTimeInMillis());
				if(bestTable == null || (cal.before(target_cal) && dt < Math.abs(getCalendar(bestTable).getTimeInMillis() - target_cal.getTimeInMillis())) )
					bestTable = tableName;
			}
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


	public static Iterable<DBObject> query(String table, DBObject q) {
		DBCursor cursor = null;
		MongoClient mongo = null;
		try {
			mongo = new MongoClient( "localhost" , 27017 );
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		}
		DB dbt = mongo.getDB("telecom");
		DBCollection t = dbt.getCollection(table);
		cursor = t.find(q);
		return cursor;
	}
}
