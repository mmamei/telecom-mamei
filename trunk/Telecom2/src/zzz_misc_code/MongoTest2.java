package zzz_misc_code;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import visual.kml.KML;
import network.NetworkCell;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;

public class MongoTest2 {
	
	public static void main(String[] args) throws Exception {
		//remove();
		//insert();
		//addIndexes();
		query();
	}
	
	public static final double earth_radius_km = 6373;
	public static final double earth_radius_m = 6372795;
	public static void query() throws Exception {
		MongoClient mongo = new MongoClient( "localhost" , 27017 );
		DB dbt = mongo.getDB("test2");
		DBCollection net = dbt.getCollection("network");
		
		
		BasicDBObject query = new BasicDBObject("loc",
			 new BasicDBObject("$geoWithin", 
					 new BasicDBObject("$centerSphere", new Object[]{new double[]{12.3350,45.4375},2000/earth_radius_m})
			 )
		);
		
		KML kml = new KML();
		PrintWriter out = new PrintWriter(new FileWriter("MongoTest2.kml"));
		kml.printHeaderDocument(out, "Venice");
	 	
		
		DBCursor cursor = net.find(query);
		while (cursor.hasNext()) {
			NetworkCell cell = new NetworkCell((BasicDBObject)cursor.next());
			out.println(cell.toKml());
		}
		
		kml.printFooterDocument(out);
		out.close();
	}
	
	
	public static void addIndexes() throws Exception {
		MongoClient mongo = new MongoClient( "localhost" , 27017 );
		DB dbt = mongo.getDB("test2");
		DBCollection net = dbt.getCollection("network");
		//net.createIndex(new BasicDBObject("celllac", 1),new BasicDBObject().append("unique", true).append("dropDups", true));
		net.createIndex(new BasicDBObject("loc", "2dsphere"));
	}
	
	public static void remove() throws Exception {
		MongoClient mongo = new MongoClient( "localhost" , 27017 );
		DB dbt = mongo.getDB("test2");
		DBCollection net = dbt.getCollection("network");
		net.drop();
	}
	
	public static void insert() throws Exception {	
		MongoClient mongo = new MongoClient( "localhost" , 27017 );
		mongo.setWriteConcern(WriteConcern.UNACKNOWLEDGED);
		DB dbt = mongo.getDB("test2");
		DBCollection net = dbt.getCollection("network");
		
		BufferedReader br = new BufferedReader(new FileReader(new File("C:/DATASET/PLS/file_rete/dfl_network_20130718.txt")));
		String line;
		while((line = br.readLine())!=null) {
			String [] splitted = line.split(":");
			NetworkCell cell = new NetworkCell(splitted);
			net.insert(cell.getBSON());
		}
		br.close();
	}
	
}
