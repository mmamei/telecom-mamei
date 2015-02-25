package analysis.tourist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import analysis.PLSEvent;
import utils.Config;
import utils.CopyAndSerializationUtils;
import visual.kml.KMLPath;

public class ProfilesKMLPlotter {
	
	static final String PLACEMARK = "Venezia";
	static int HOW_MANY = 5; 
	
	public static void main(String[] args) throws Exception {
		
		Config.getInstance().pls_folder = Config.getInstance().pls_root_folder+"/file_pls_ve"; 
		Config.getInstance().pls_start_time = new GregorianCalendar(2013,Calendar.JULY,1,0,0,0);
		Config.getInstance().pls_end_time = new GregorianCalendar(2013,Calendar.JULY,31,23,59,59);
		
		// profile --> list of HOW_MANY users to be plotted
		Map<String,List<String>> pusers = new HashMap<String,List<String>>();
		pusers.put("Resident", new ArrayList<String>());
		pusers.put("Tourist", new ArrayList<String>());
		pusers.put("Commuter", new ArrayList<String>());
		pusers.put("Transit", new ArrayList<String>());
		pusers.put("Excursionist", new ArrayList<String>());
		
		// username --> profile
		Map<String,String> mu =  (Map<String,String>)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/Tourist/"+PLACEMARK+"_gt_profiles_July2013.ser"));
		
		for(String user: mu.keySet()) {
			String profile = mu.get(user);
			if(pusers.get(profile).size() < HOW_MANY) pusers.get(profile).add(user);  
		}
		
		Map<String,List<PLSEvent>> traces = new HashMap<String,List<PLSEvent>>();
		for(List<String> users: pusers.values())
			for(String u : users)
				traces.put(u, new ArrayList<PLSEvent>());
		
		BufferedReader br = new BufferedReader(new FileReader(Config.getInstance().base_folder+"/UserEventCounter/file_pls_ve_"+PLACEMARK+"_cellXHour_July2013.csv"));
		String line;
		while((line = br.readLine())!=null) {	
			String[] p = line.split(",");
			String user_id = p[0];
			if(traces.containsKey(user_id)) 
				traces.put(user_id, PLSEvent.getDataFormUserEventCounterCellacXHourLine(line));
		}
		br.close();
		
		KMLPath.openFile(Config.getInstance().base_folder+"/Tourist/"+PLACEMARK+"_profiles.kml");
		for(String p: pusers.keySet())
			for(String u: pusers.get(p)) {
				System.out.println(p+" --> "+u);
				KMLPath.print(p+"_"+u,traces.get(u));
			}
		KMLPath.closeFile();		
		System.out.println("Done");
	}	
}
