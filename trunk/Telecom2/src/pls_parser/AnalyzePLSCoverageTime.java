package pls_parser;

import java.io.File;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.FileUtils;
import utils.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

public class AnalyzePLSCoverageTime {
	
	static Config conf = null;
	

	public static void main(String[] args) {
		AnalyzePLSCoverageTime apc = new AnalyzePLSCoverageTime();
		
		
		Map<String,Map<String,String>> all =  apc.computeAll();
		for(String key: all.keySet()) 
			System.out.println(key+" -> "+apc.getNumYears(all.get(key)));
		
		
		System.out.println(apc.getJSMap(all));
	}
	
	SimpleDateFormat f = new SimpleDateFormat("yyyy/MMM/dd",Locale.US);
	public String getJSMap(Map<String,Map<String,String>> all) {
		StringBuffer sb = new StringBuffer();
		Calendar cal = Calendar.getInstance();
		for(String key: all.keySet()) {
			sb.append("var dataTable_"+key+" = new google.visualization.DataTable();");
			sb.append("dataTable_"+key+".addColumn({ type: 'date', id: 'Date' });");
			sb.append("dataTable_"+key+".addColumn({ type: 'number', id: 'PLS Coverage' });");
			sb.append("dataTable_"+key+".addRows([\n");
			Map<String,String> dmap = all.get(key);
			
			for(String day: dmap.keySet()) {
				try {
					cal.setTime(f.parse(day));
					int h = dmap.get(day).split("-").length;
					String s = "[ new Date("+cal.get(Calendar.YEAR)+", "+cal.get(Calendar.MONTH)+", "+cal.get(Calendar.DAY_OF_MONTH)+"), "+h+" ],\n";
					sb.append(s);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			sb.append("]);\n");
		}
		return sb.toString();
	}
	
	public int getNumYears(Map<String,String> dmap) {
		int min_year = 3000;
		int max_year = 0;
		for(String d: dmap.keySet()) {
			int year = Integer.parseInt(d.substring(0,d.indexOf("/")));
			min_year = Math.min(min_year, year);
			max_year = Math.max(max_year, year);
		}
		return max_year - min_year + 1;
	}

	static final String[] MONTHS = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	public Map<String,Map<String,String>> computeAll() {
		Map<String,Map<String,String>> map = new HashMap<String,Map<String,String>>();
		MongoClient mongo = null;
		try {
			mongo = new MongoClient( "localhost" , 27017 );
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		}
		DB dbt = mongo.getDB("telecom");
		DBCollection tc = dbt.getCollection("TimeCoverage");
		DBCursor cursor = tc.find();
		
		while (cursor.hasNext()) {
			BasicDBObject r = (BasicDBObject)cursor.next();
			String pls_dir = r.getString("pls_dir");
			Calendar cal = new GregorianCalendar();
			cal.setTime(r.getDate("time"));
			
			Map<String,String> allDays = map.get(pls_dir);
			if(allDays == null) {
				allDays = new TreeMap<String,String>();
				map.put(pls_dir, allDays);
			}
			
			int day =  cal.get(Calendar.DAY_OF_MONTH);
			String sday = day < 10 ? "0"+day : ""+day;
			
			String key = cal.get(Calendar.YEAR)+"/"+MONTHS[cal.get(Calendar.MONTH)]+"/"+sday;
			
			String h = allDays.get(key);
			
			if(h == null) h = "";
			if(!h.contains(cal.get(Calendar.HOUR_OF_DAY)+"-")) 
				h =  h + cal.get(Calendar.HOUR_OF_DAY)+"-";
		
			allDays.put(key, h);
		}
		return map;
	}
	
	
	public Map<String,String> compute() {
		String dir = Config.getInstance().pls_folder;
		dir = dir.substring(dir.indexOf("file_pls/")+9);
		dir = dir.substring(0,dir.indexOf("/"));
		return computeAll().get(dir);
		
	}
}
