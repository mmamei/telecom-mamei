package db;

import java.io.File;
import java.net.UnknownHostException;
import java.util.Date;

import utils.FileUtils;
import utils.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;

public class TimeCoverageTable {
	
	public static void main(String[] args) {
		insert();
		Logger.logln("Done!");
	}
	
	
	public static void insert() {
		MongoClient mongo = null;
		try {
			mongo = new MongoClient( "localhost" , 27017 );
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return;
		}
		DB dbt = mongo.getDB("telecom");
		DBCollection tc = dbt.getCollection("TimeCoverage");
		tc.createIndex(new BasicDBObject("pls_dir", 1).append("time", 1),new BasicDBObject().append("unique", true).append("dropDups", true));
		
		File[] basedirs = FileUtils.getFiles("DATASET/PLS/file_pls");
		for(File bdir: basedirs) 
		for(File dir: bdir.listFiles()) {
			analyzeDirectory(dir,tc,dir.getName());
		}
	}
	
	
	private static void analyzeDirectory(File directory, DBCollection tc, String pls_dir) {
		Logger.logln(pls_dir+"\t"+directory.getAbsolutePath());
		File[] items = directory.listFiles();
		
		for(int i=0; i<items.length;i++){
			File item = items[i];
			if(item.isFile()) {
				String n = item.getName();
				try {
					tc.insert(new BasicDBObject("pls_dir",pls_dir).append("time", new Date(Long.parseLong(n.substring(n.lastIndexOf("_")+1, n.indexOf(".zip"))))));
				} catch(MongoException e) {
				}
			}
			else if(item.isDirectory())
				analyzeDirectory(item,tc,pls_dir);
		}	
	}
	
}
