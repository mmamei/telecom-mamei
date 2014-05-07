package zzz_misc_code;

import java.util.List;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

public class MongoTest {
	
	public static void main(String[] args) throws Exception {
		
		MongoClient mongo = new MongoClient( "localhost" , 27017 );
		DB dbt = mongo.getDB("test");
		
		List<String> dbs = mongo.getDatabaseNames();
		for(String db : dbs)
			System.out.println(db);		
		
		DBCollection table = dbt.getCollection("user");
		table.drop();
		table = dbt.getCollection("user");
		
		BasicDBObject document = new BasicDBObject();
		document.put("name", "marco");
		document.put("surname", "mamei");
		document.put("age", 30);
		table.insert(document);
		
		document = new BasicDBObject();
		document.put("name", "marco");
		document.put("surname", "picci");
		document.put("age", 30);
		table.insert(document);
		
		
		document = new BasicDBObject();
		document.put("name", "marco");
		document.put("surname", "picci");
		document.put("age", 30);
		table.insert(document);
		
		table.createIndex(new BasicDBObject("name", 1).append("surname", 1),new BasicDBObject().append("unique", true).append("dropDups", true));
		
	 
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("name", "marco");
		
		DBCursor cursor = table.find(searchQuery);
	 
		while (cursor.hasNext()) {
			System.out.println(cursor.next());
		}
		
	}
	
}
