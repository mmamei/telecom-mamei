package pls_parser;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.TreeMap;

import utils.Config;
import utils.Logger;

public class AnalyzePLSCoverageDetailed {
	
	/*
	 * This creates a csv file representing a matrix.
	 * Each row of the matrix is a day
	 * Each row has 24 columns associated with hours
	 * Each cell contains the total file size of the pls files for that day on that hour
	 */
		
	public static void main(String[] args) throws Exception {
		String dir = Config.getInstance().pls_folder;
		Map<String,long[]> allDays = new TreeMap<String,long[]>();
		try {
			analyzeDirectory(new File(dir),allDays);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		String odir = Config.getInstance().base_dir+"/AnalyzePLSCoverageDetailed";
		PrintWriter out = new PrintWriter(new FileWriter(odir+"/"+dir.substring(dir.lastIndexOf("/")+1)+".csv"));
		for(String d : allDays.keySet()) {
			long[] data = allDays.get(d);
			out.print(d+",");
			for(long x: data)
				out.print(x+",");
			out.println();
		}
		out.close();
		Logger.logln("Done");
	}
	
	static final String[] MONTHS = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	static final String[] DAY_WEEK = new String[]{"0","Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
	private static void analyzeDirectory(File directory, Map<String,long[]> allDays) throws Exception{
		File[] items = directory.listFiles();
		for(int i=0; i<items.length;i++){
			File item = items[i];
			if(item.isFile()) {
				Calendar cal = new GregorianCalendar();
				String n = item.getName();
				cal.setTimeInMillis(Long.parseLong(n.substring(n.lastIndexOf("_")+1, n.indexOf(".zip"))));
				
				int day =  cal.get(Calendar.DAY_OF_MONTH);
				String sday = day < 10 ? "0"+day : ""+day;
				String dayweek = DAY_WEEK[cal.get(Calendar.DAY_OF_WEEK)];
				String key = cal.get(Calendar.YEAR)+"/"+MONTHS[cal.get(Calendar.MONTH)]+"/"+sday+","+dayweek;
				
				int h = cal.get(Calendar.HOUR_OF_DAY);
				
				long[] data = allDays.get(key);
				if(data == null) 
					data = new long[24];
				
				data[h] += item.length();
						
				allDays.put(key, data);
			}
			else if(item.isDirectory())
				analyzeDirectory(item,allDays);
		}	
	}
}
