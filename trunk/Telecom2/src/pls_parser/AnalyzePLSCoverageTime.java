package pls_parser;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import utils.Config;
import utils.FileUtils;

public class AnalyzePLSCoverageTime {
	
	static Config conf = null;
	

	public static void main(String[] args) {
		AnalyzePLSCoverageTime apc = new AnalyzePLSCoverageTime();
		/*
		Map<String,String> allDays = apc.compute();
		Logger.logln("Days in the dataset:");
		for(String d:allDays.keySet()) 
			Logger.logln(d+" = "+allDays.get(d));
		System.out.println("TOT = "+allDays.size());
		*/
		/*
		Map<String,Map<String,String>> all =  apc.computeAll();
		for(String file: all.keySet()) {
			Map<String,String> allDays = all.get(file);
			Logger.logln("Days in the dataset "+file+":");
			for(String d:allDays.keySet()) 
				Logger.logln(d+" = "+allDays.get(d));
			System.out.println("TOT = "+allDays.size());
		}
		*/
		Map<String,Map<String,String>> all =  apc.computeAll();
		for(String key: all.keySet()) 
			System.out.println(key+" -> "+apc.getNumYears(all.get(key)));
		
		//String js = apc.getJSMap(all);
		//System.out.println(js);
	}
	
	SimpleDateFormat f = new SimpleDateFormat("yyyy/MMM/dd",Locale.US);
	public String getJSMap(Map<String,Map<String,String>> map) {
		StringBuffer sb = new StringBuffer();
		Calendar cal = Calendar.getInstance();
		for(String key: map.keySet()) {
			sb.append("var dataTable_"+key+" = new google.visualization.DataTable();");
			sb.append("dataTable_"+key+".addColumn({ type: 'date', id: 'Date' });");
			sb.append("dataTable_"+key+".addColumn({ type: 'number', id: 'PLS Coverage' });");
			sb.append("dataTable_"+key+".addRows([\n");
			Map<String,String> dmap = map.get(key);
			
			for(String day: dmap.keySet()) {
				try {
					cal.setTime(f.parse(day));
					int h = dmap.get(day).split("-").length;
					sb.append("[ new Date("+cal.get(Calendar.YEAR)+", "+cal.get(Calendar.MONTH)+", "+cal.get(Calendar.DAY_OF_MONTH)+"), "+h+" ],\n");
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			sb.append("]);\n");
		}
		return sb.toString();
	}
	
	public int getNumYears(Map<String,String> dmap) {
		Set<String> y = new HashSet<String>();
		for(String d: dmap.keySet()) {
			String year = d.substring(0,d.indexOf("/"));
			y.add(year);
		}
		return y.size();
	}

	
	
	public Map<String,Map<String,String>> computeAll() {
		File[] basedirs = FileUtils.getFiles("DATASET/PLS/file_pls");
		Map<String,Map<String,String>> all = new HashMap<String,Map<String,String>>();
		for(File basedir: basedirs) {
			for(File dir: basedir.listFiles()) {
				Map<String,String> val = all.get(dir.getName());
				if(val == null) {
					val = new TreeMap<String,String>();
					all.put(dir.getName(), val);
				}
				val.putAll(compute(dir));
			}
		}
		return all;
	}
   
	
	
	public Map<String,String> compute() {
		File dir = new File(Config.getInstance().pls_folder);
		return compute(dir);
	}
	
	public Map<String,String> compute(File dir) {	
		Map<String,String> allDays = new TreeMap<String,String>();
		try {
			analyzeDirectory(dir,allDays);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return allDays;
	}
	
	static final String[] MONTHS = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	private static void analyzeDirectory(File directory, Map<String,String> allDays) throws Exception {
		File[] items = directory.listFiles();
		
		for(int i=0; i<items.length;i++){
			File item = items[i];
			if(item.isFile()) {
				Calendar cal = new GregorianCalendar();
				String n = item.getName();
				cal.setTimeInMillis(Long.parseLong(n.substring(n.lastIndexOf("_")+1, n.indexOf(".zip"))));
				
				int day =  cal.get(Calendar.DAY_OF_MONTH);
				String sday = day < 10 ? "0"+day : ""+day;
				
				String key = cal.get(Calendar.YEAR)+"/"+MONTHS[cal.get(Calendar.MONTH)]+"/"+sday;
				
				String h = allDays.get(key);
				
				if(h == null) h = "";
				if(!h.contains(cal.get(Calendar.HOUR_OF_DAY)+"-")) 
					h =  h + cal.get(Calendar.HOUR_OF_DAY)+"-";
			
				allDays.put(key, h);
			}
			else if(item.isDirectory())
				analyzeDirectory(item,allDays);
		}	
	}
}
