package zzz_misc_code;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import region.network.NetworkCell;
import visual.kml.KML;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;

public class MongoTest2 {
	
	public static void main(String[] args) throws Exception {
		remove();
		insert();
		addIndexes();
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
			NetworkCell cell = NetworkCell.bson2NetworkCell((BasicDBObject)cursor.next());
			out.println(cell.toKml("#7f770077"));
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
			String description = splitted[0];
			int barycentre = Integer.parseInt(splitted[1]);
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
			NetworkCell cell = new NetworkCell(celllac,description,barycentre,lac,cell_id,barycentre_lat,barycentre_lon,radius);
			net.insert(cell.getBSON());
		}
		br.close();
	}
	
}
