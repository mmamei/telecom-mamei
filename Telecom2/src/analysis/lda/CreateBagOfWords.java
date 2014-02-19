package analysis.lda;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import utils.CopyAndSerializationUtils;
import utils.FileUtils;
import utils.Logger;
import visual.kml.KMLPath;
import analysis.lda.bow.Bow;
import analysis.lda.bow.OneDocXDayMov;
import area.region.RegionMap;

public class CreateBagOfWords {
	
	public static final int REPETITIONS = 100;
	public static final int MAX_NUM = 1000; // negative value for infinite
	public static int MIN_DAYS = 30;
	public static int MIN_PLS = 200;
	
	public static void main(String[] args) throws Exception {
		process("Torino",new OneDocXDayMov());
	}
	
	
	public static void process(String city, Bow bow) throws Exception {
		String user_id;
		int num_pls;
		int num_days;
		
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(FileUtils.getFile("RegionMap/"+city+".ser"));
		BufferedReader br = FileUtils.getBR("UserEventCounter/"+city+"_cellXHour.csv");
		
		int n_users_processed = 0;
		
		String line;
		while((line=br.readLine())!=null) {
			if(line.startsWith("//")) {Logger.logln(line); continue;}
			
			String[] p = line.split(",");
			user_id = p[0];
			//String mnt = p[1];
			num_pls = Integer.parseInt(p[2]);
			num_days = Integer.parseInt(p[3]);
			//int days_interval = Integer.parseInt(p[4]);
			if(num_pls < MIN_PLS || num_days < MIN_DAYS) continue;
			Map<String,List<String>> docs = bow.process(p,5,rm);
			if(docs == null) continue;
			
			processUser(user_id,docs,line,rm);
			
			n_users_processed ++;
			
			if(MAX_NUM > 0 && n_users_processed > MAX_NUM) break;
		
			if(n_users_processed % 10 == 0) Logger.log(".");
			if(n_users_processed % 1000 == 0) Logger.logln("");
		}
		br.close();
		Logger.logln("\nDone!");
	}
	
	
	
	public static void processUser(String user_id,Map<String,List<String>> docs,String line,RegionMap rm) {
		
		File dir = FileUtils.getFile("Topic/"+user_id);
		if(dir != null) return;
		// create user directory
		dir = FileUtils.create("Topic/"+user_id);
		
		
		
		// create kml file
		KMLPath.openFile(dir.getAbsolutePath()+"/"+user_id+".kml");	
		
		KMLPath.addKml("<Folder><name>RegionMap</name>"+rm.getKMLBorders()+"</Folder>");
		
		KMLPath.print(user_id,KMLPath.getDataFormUserEventCounterCellacXHourLine(line));	
		KMLPath.closeFile();	
		
		try {
			// create bag of words file
			PrintWriter pw = FileUtils.getPW("Topic/"+user_id, user_id+".txt");
			for(int r=0; r<REPETITIONS;r++)
			for(String day : docs.keySet())
				pw.println(day+"\tX\t"+toString(docs.get(day),REPETITIONS));
			pw.close();
		} catch(Exception e) {
			System.out.println("ERROR:");
			System.out.println(user_id);
			System.out.println(docs.size());
		}
	}
	
	
	
	public static String toString(List<String> d, int rep) {
		StringBuffer sb = new StringBuffer();
		for(int r=0; r<rep;r++)
			for(String w : d)
				sb.append(" "+w);
		return sb.substring(1);
	}
}
