package db;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import region.RegionI;
import region.RegionMap;
import region.network.NetworkMapFactory;
import utils.Logger;
import utils.Mail;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;

public class PLSTable {
	
	public static void main(String[] args) {
		
		MongoClient mongo = null;
		try {
			mongo = new MongoClient( "localhost" , 27017 );
			mongo.setWriteConcern(WriteConcern.UNACKNOWLEDGED);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return;
		}
		DB dbt = mongo.getDB("telecom");
		DBCollection t = dbt.getCollection("PLS");
		t.drop();
		t = dbt.getCollection("PLS");
		insert(new File("G:/DATASET/PLS/file_pls"),t);
		createIndexes(t);
		Mail.send("completed!");
		Logger.logln("Done");
	}
	
	private static void insert(File dir, DBCollection t) {
		File[] items = dir.listFiles();
		for(int i=0; i<items.length;i++){
			File item = items[i];
			if(item.isFile()) {
				analyzeFile(item, t);
				if((i+1) % 100 == 0) Logger.logln(i+"/"+items.length+" done!");
			}
			else if(item.isDirectory())
				insert(item, t);
		}
	}
	
	private static void createIndexes(DBCollection t) {
		t.createIndex(new BasicDBObject("username", 1).append("time", 1).append("imsi", 1),new BasicDBObject().append("unique", true).append("dropDups", true));
		t.createIndex(new BasicDBObject("loc", "2dsphere"));
	}
	
	
	private static void analyzeFile(File plsFile, DBCollection t) {	
		ZipFile zf = null;
		BufferedReader br = null;
		try {
			zf = new ZipFile(plsFile);
			ZipEntry ze = (ZipEntry) zf.entries().nextElement();
			br = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)));
			String line;
			String[] fields;
			String username;
			String imsi;
			String celllac;
			long timestamp;
			RegionI region;
			double[] latlon;
			double radius;
			RegionMap nm = null;
			while((line=br.readLine())!=null)  {
				
				fields = line.split("\t");
				username = fields[0];
				imsi = fields[1];
				celllac = fields[2];
				timestamp = Long.parseLong(fields[3]);
				
				if(nm==null) {
					Calendar cal = new GregorianCalendar();
					cal.setTimeInMillis(timestamp);
					nm = NetworkMapFactory.getNetworkMap(cal);
				}
				
				region = nm.getRegion(celllac);
				if(region == null) { 
					Calendar cal = new GregorianCalendar();
					cal.setTimeInMillis(timestamp);
					Logger.logln(nm.getName()+" has null region --> "+celllac+" at time "+cal.getTime());
					continue;
				}
				
				latlon = region.getLatLon();
				radius = region.getRadius();
				
				try {
					t.insert(new BasicDBObject("username",username).append("imsi", imsi).
				    append("celllac", celllac).append("time", new Date(timestamp)).
				    append("loc", new BasicDBObject().append("type", "Point").append("coordinates", new double[]{latlon[1],latlon[0]}).
					append("radius", radius)));
				} catch(MongoException e) {
					Logger.logln("Bad read "+e);
				}
			}
				
		}catch(Exception e) {
			System.err.println("Problems with file: "+plsFile.getAbsolutePath());
			e.printStackTrace();
		}
		try {
			br.close();
			zf.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
