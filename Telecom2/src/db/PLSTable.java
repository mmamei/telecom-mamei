package db;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.mongodb.MongoException;

public class PLSTable {
	
	public static void main(String[] args) {
		try {
			
			String name = "pls_ve";
			Map<String,List<File>> filesByDay = new HashMap<String,List<File>>();
			organizeFilesByDay(new File("G:/DATASET/PLS/file_pls/file_"+name),filesByDay);
			DB dbt = DBConnection.getDB();
			for(String day: filesByDay.keySet()) {
				DBCollection t = dbt.getCollection(name+"_"+day);
				//t = DBConnection.dropAndRecreate(name);
				for(File f: filesByDay.get(day))
					insertFile(f, t);
				createIndexes(t);
				Logger.logln(day+" completed!");
			}
			
			Mail.send("completed!");
			Logger.logln("Done");
		}catch (Exception e) {
			Mail.send(e.getMessage());
		}
	}
	
	
	private static void organizeFilesByDay(File dir, Map<String,List<File>> map) {
		File[] items = dir.listFiles();
		for(int i=0; i<items.length;i++){
			File item = items[i];
			if(item.isFile()) {
				String n = item.getName();
				String day = getDay(Long.parseLong(n.substring(n.lastIndexOf("_")+1, n.indexOf(".zip"))));
				List<File> l = map.get(day);
				if(l == null) {
					l = new ArrayList<File>();
					map.put(day, l);
				}
				l.add(item);
			}
			else if(item.isDirectory())
				organizeFilesByDay(item, map);
		}
	}
	
	private static final SimpleDateFormat F = new SimpleDateFormat("yyyyMMdd");
	private static String getDay(long time) {
		return F.format(new Date(time));
	}
	

	
	private static void createIndexes(DBCollection t) {
		t.createIndex(new BasicDBObject("username", 1).append("time", 1).append("imsi", 1).append("loc", "2dsphere"),new BasicDBObject().append("unique", true).append("dropDups", true));
	}
	
	
	private static void insertFile(File plsFile, DBCollection t) {	
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
					//Logger.logln(nm.getName()+" has null region --> "+celllac+" at time "+cal.getTime());
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
