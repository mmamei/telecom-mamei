package analysis.lda;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import network.NetworkCell;
import utils.CopyAndSerializationUtils;
import utils.FileUtils;
import utils.Logger;
import visual.kml.KMLPath;
import area.region.Region;
import area.region.RegionMap;

public class CreateBagOfWords {
	public static final boolean KML = true;
	public static int MIN_DAYS = 14;
	public static int MIN_PLS = 200;
	public static int MIN_PLACES = 5;
	
	static String city = "Venezia";
	static RegionMap rm;
	static String output_dir = "Topic/"+city;
	public static void main(String[] args) throws Exception {			
		rm = (RegionMap)CopyAndSerializationUtils.restore(FileUtils.getFile("RegionMap/"+city+".ser"));
		BufferedReader br = FileUtils.getBR("UserEventCounter/Venezia_cellXHour.csv");
		if(KML) KMLPath.openFile(FileUtils.create("Topic").getAbsolutePath()+"/"+city+".kml");	
		String line;
		int cont = 0;
		while((line=br.readLine())!=null) {
			if(line.startsWith("//")) {Logger.logln(line); continue;}
			process(line);
			cont ++;
			if(cont % 1000 == 0) Logger.log(".");
			if(cont % 100000 == 0) Logger.logln("");
		}
		
		br.close();
		if(KML) KMLPath.closeFile();
		Logger.logln("\nDone!");
	}
	
	
	public static final String[] HP = new String[]{"N","N","N","N","N","N","N","M", 
		  										   "M","M","M","M","M","M","A","A", 
		  										   "A","A","A","E","E","E","E","N"};

	
	static String user_id;
	static String mnt;
	static int num_pls;
	static int num_days;
	static int days_interval;
	static NetworkCell nc;
	
	public static void process(String events) {
		String[] p = events.split(",");
		user_id = p[0];
		mnt = p[1];
		num_pls = Integer.parseInt(p[2]);
		num_days = Integer.parseInt(p[3]);
		days_interval = Integer.parseInt(p[4]);
		
		if(num_pls < MIN_PLS || num_days < MIN_DAYS) return;
		
		
		Map<String,StringBuffer> dailyPatterns = new HashMap<String,StringBuffer>();
		Set<String> places = new HashSet<String>();
		for(int i=5;i<p.length;i++) {
			// 2013-5-23:Sun:13:4018542484
			String[] x = p[i].split(":");
			String day = x[0]+"-"+x[1];
			int h = Integer.parseInt(x[2]);
			long celllac =Long.parseLong(x[3]);
			Region r = rm.getClosest(celllac);
			String rname = r == null ? "OUT" : r.getName().replaceAll(",", "-");
			places.add(rname);
			StringBuffer sb = dailyPatterns.get(day);
			if(sb == null) {
				sb = new StringBuffer();
				dailyPatterns.put(day, sb);
			}
			sb.append(HP[h]+"-"+rname+" ");
		}
		
		if(places.size() < MIN_PLACES) return;
		
		if(KML) KMLPath.print(user_id,KMLPath.getDataFormUserEventCounterCellacXHourLine(events));	
		PrintWriter pw = FileUtils.getPW(output_dir, user_id+".txt");
		for(String day : dailyPatterns.keySet())
			pw.println(day+"\tX\t"+dailyPatterns.get(day));
		pw.close();
	}

}
