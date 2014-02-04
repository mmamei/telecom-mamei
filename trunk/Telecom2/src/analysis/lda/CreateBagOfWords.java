package analysis.lda;

import java.io.BufferedReader;
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
	public static final boolean KML = true;
	public static final int MAX_NUM = 10;
	public static final int MAX_KML = 10;
	public static int MIN_DAYS = 30;
	public static int MIN_PLS = 500;
	
	
	public static final String city = "Torino";
	public static final Bow bow = new OneDocXDayMov();
	
	public static void main(String[] args) throws Exception {	
		
		String output_dir = "Topic/"+city+"_"+bow.getClass().getSimpleName();
		String user_id;
		String mnt;
		int num_pls;
		int num_days;
		int days_interval;
		
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(FileUtils.getFile("RegionMap/"+city+".ser"));
		BufferedReader br = FileUtils.getBR("UserEventCounter/"+city+"_cellXHour.csv");
		if(KML) KMLPath.openFile(FileUtils.create("Topic").getAbsolutePath()+"/"+city+".kml");	
		String line;
		int cont = 0;
		while((line=br.readLine())!=null) {
			if(line.startsWith("//")) {Logger.logln(line); continue;}
			
			String[] p = line.split(",");
			user_id = p[0];
			mnt = p[1];
			num_pls = Integer.parseInt(p[2]);
			num_days = Integer.parseInt(p[3]);
			days_interval = Integer.parseInt(p[4]);
			
			if(num_pls < MIN_PLS || num_days < MIN_DAYS) continue;
			
			Map<String,List<String>> docs = bow.process(p,5,rm);
			
			if(docs == null) continue;
			
			
			if(KML && cont < MAX_KML) KMLPath.print(user_id,KMLPath.getDataFormUserEventCounterCellacXHourLine(line));	
			PrintWriter pw = FileUtils.getPW(output_dir, user_id+".txt");
			for(String day : docs.keySet())
				pw.println(day+"\tX\t"+toString(docs.get(day)));
			pw.close();
			
			cont ++;
			
			
			if(cont > MAX_NUM) break;
			
			
			if(cont % 10 == 0) Logger.log(".");
			if(cont % 1000 == 0) Logger.logln("");
		}
		
		br.close();
		if(KML) KMLPath.closeFile();
		Logger.logln("\nDone!");
	}
	
	public static String toString(List<String> d) {
		StringBuffer sb = new StringBuffer();
		for(String w : d)
			sb.append(" "+w);
		return sb.substring(1);
	}
}
