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
		Set<String> tables = dbt.getCollectionNames();
		 
		for(String coll : tables){
			System.out.println(coll);
		}
		
		
		BasicDBObject document = new BasicDBObject();
		document.put("name", "mkyong");
		document.put("age", 30);
		table.insert(document);
		
		BasicDBObject searchQuery = new BasicDBObject();
		searchQuery.put("name", "mkyong");
	 
		DBCursor cursor = table.find(searchQuery);
	 
		while (cursor.hasNext()) {
			System.out.println(cursor.next());
		}
		
	}
	
}
