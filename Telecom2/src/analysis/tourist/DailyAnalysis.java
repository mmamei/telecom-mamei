package analysis.tourist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import utils.Config;
import utils.CopyAndSerializationUtils;
import visual.r.RPlotter;

public class DailyAnalysis {
	
	public static void main(String[] args) throws Exception {
		
		
		/*
		 * LEGGI LEGGI LEGGI LEGGI LEGGI LEGGI LEGGI LEGGI LEGGI LEGGI LEGGI LEGGI LEGGI LEGGI LEGGI LEGGI LEGGI LEGGI LEGGI LEGGI 
		 * NOTA la visualizzazione di Torino Oct 2014 è sbagliata!!!!!!!!!!!!
		 * I dati vanno da 20 Oct 14 al 14 Nov 14
		 * Quindi particamente i giorni 1-14 sono in verità giorni di novembre.
		 * Poi c'è un buco. dal 15 al 19
		 * Poi ci sono i giorni di Ottobre
		 */
		
		String[][] data = new String[][]{
				{"fi","Firenze","March2014"},
				{"ve","Venezia","March2014"},
				{"fi","Firenze","July2014"},
				{"ve","Venezia","July2014"},
				{"piem","Torino","Oct2014"},
				{"pu","Lecce","Aug2014"},
				{"pu","Lecce","Sep2014"},
		};
		
		for(String[] d: data) {
			String region = d[0];
			String city = d[1];
			String month = d[2];
			Map<String,double[]> pdt =  getDailyTrend(Config.getInstance().base_folder+"/Tourist/"+city+"_"+month+"_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_"+region+"_"+city+"_cellXHour_"+month+".csv");
			draw(pdt,city,month);
		}
		
		System.out.println("Done");
		
	}
	
	// pdt = profile daily trend
	public static void draw(Map<String,double[]> pdt, String city, String month) {
		
		
		List<String> names = new ArrayList<String>();
		List<double[]> data = new ArrayList<double[]>();
		for(String p: pdt.keySet()) {
			names.add(p);
			data.add(pdt.get(p));
		}
		String[] days = new String[31];
		for(int i=0;i<days.length;i++)
			days[i] = String.valueOf(i+1);
		
		
		RPlotter.drawLine(days, data, names, "profiles", "days "+month, "number", Config.getInstance().paper_folder+"/img/daily/"+city+month+"Daily.pdf", "theme(legend.position=c(0.15, 0.8),legend.background = element_rect(size=1))");
		
		
	}
	
	public static Map<String,double[]> getDailyTrend(String classes_ser_file, String cellXHourFile) throws Exception {
		
		
		Map<String,double[]> profile_dailytrend = new HashMap<String,double[]>();
		for(String profile: GTExtractor.PROFILES)
			profile_dailytrend.put(profile, new double[31]);
		
		BufferedReader br = new BufferedReader(new FileReader(new File(cellXHourFile)));
		Map<String,String> user_prof = (Map<String,String>)CopyAndSerializationUtils.restore(new File(classes_ser_file));
			
		
		String line;
		while((line=br.readLine())!=null) {
			
			if(line.startsWith("//")) continue;
			String[] e = line.split(",");
			String user =e[0];
			String profile = user_prof.get(user);
			if(profile!=null) {
				// get first and last days
				int days_interval = Integer.parseInt(e[4]);
				// 2013-5-23:Sun:13:4018542484
				int first_day = Integer.parseInt(e[5].split("-|:")[2]);
				//System.out.println(line);
				
				Set<Integer> days = new HashSet<Integer>();
				for(int i=5;i<e.length;i++) 
					days.add(Integer.parseInt(e[i].split("-|:")[2]));
					
					//System.out.println("["+first_day+"-"+(first_day+days_interval)+"] =="+days);
				
					for(int i: days) 
					//for(int i=first_day; i<first_day+days_interval && i<31;i++)
						profile_dailytrend.get(profile)[i-1]++;
				
				
			}
		}
		br.close();
		
		return profile_dailytrend;
	}
}
