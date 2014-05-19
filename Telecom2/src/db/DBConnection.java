package db;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.UnknownHostException;

import utils.Logger;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;

public class DBConnection {
	private static final String DB_NAME = "telecom";
	private static DB db = null;
	public static DB getDB() {
		if(db == null) {
			MongoClient mongo = null;
			try {
				mongo = new MongoClient( "localhost" , 27017 );
				mongo.setWriteConcern(WriteConcern.UNACKNOWLEDGED);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				return null;
			}
			db = mongo.getDB("telecom");
		}
		return db;
	}
	
	public static DBCollection dropAndRecreate(String tableName) {
		try {
			DB db = getDB();
			if(db.getCollectionNames().contains(tableName)) {
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				Logger.logln("Drop Table "+tableName+"? yes/no");
				String confirm = br.readLine().toLowerCase();
				if(confirm.equals("yes")) {
					Logger.logln("Dropping "+tableName);
					DBCollection t = db.getCollection(tableName);
					t.drop();
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return db.getCollection(tableName);
	}
}
