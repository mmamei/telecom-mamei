package dataset.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import region.CityEvent;
import region.Placemark;
import utils.Config;
import utils.Logger;

public class UsersAroundMultipleEvents extends BufferAnalyzerConstrained {
	
	private Map<String,Set<String>> usersAround;
	private List<CityEvent> events;
	private String outputdir;
	Calendar startTime;
	Calendar endTime;
	
	UsersAroundMultipleEvents(Placemark placemark, String user_list_name, List<CityEvent> events, String outputdir) {
		super(placemark,user_list_name);
		this.events = events;
		this.outputdir = outputdir;
		usersAround = new HashMap<String,Set<String>>();
		for(CityEvent ce: events)
			usersAround.put(ce.toString(), new HashSet<String>());
		
		startTime = (Calendar)events.get(0).st.clone();
		endTime = (Calendar)events.get(0).et.clone();
		for(CityEvent ce: events) {
			if(ce.st.before(startTime)) startTime = (Calendar)ce.st.clone();
			if(ce.et.after(endTime)) endTime = (Calendar)ce.st.clone();
		}
	}
	
	public Calendar getStartTime() {
		return startTime;
	}
	public Calendar getEndTime() {
		return endTime;
	}
	
	
	void analyze(String username, String imsi, String celllac, long timestamp, Calendar cal, String header) {
		for(CityEvent ce: events) {
			if(cal.after(ce.st) && cal.before(ce.et) && ce.spot.contains(celllac)) 
				usersAround.get(ce.toString()).add(username+","+imsi);
		}
	}
	
	
	protected void finish() {
		try {
			new File(outputdir).mkdirs();
			for(String event: usersAround.keySet()) {
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(outputdir+"/"+event+".txt"))));
				for(String user: usersAround.get(event))
					out.println(user);
				out.close();
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	
	static String[] origin_places = new String[]{"Stazione Lecce","Aereoporto Bari","Porto Bari","Aereoporto Brindisi","Porto Brindisi"};
	public static void main(String[] args) throws Exception {
		String region = "file_pls_pu";
		Config.getInstance().pls_folder = Config.getInstance().pls_root_folder+"/"+region;
		
		
		//CityEvent target_event = CityEvent.getEvent("Melpignano,22/08/2014");	
		//CityEvent target_event = CityEvent.getEvent("Lecce,14/08/2014");	
		CityEvent target_event = CityEvent.getEvent("Lecce,24/08/2014");	
		
		UsersAroundAnEvent uae = new UsersAroundAnEvent();
		uae.process(target_event,true);
		
		List<CityEvent> origins = new ArrayList<CityEvent>();
		for(String op: origin_places) {
			Placemark p = Placemark.getPlacemark(op);
			Calendar end_cal = (Calendar)target_event.st.clone();
			Calendar start_cal = (Calendar)end_cal.clone();
			start_cal.set(Calendar.HOUR_OF_DAY, 0);
			start_cal.set(Calendar.MINUTE, 0);
			start_cal.set(Calendar.SECOND, 0);
			origins.add(new CityEvent(p,start_cal,end_cal,0));
			//start_cal.add(Calendar.DAY_OF_MONTH, -10);
			for(int i=0;i<18;i++) {
				end_cal = (Calendar)start_cal.clone();
				start_cal = (Calendar)start_cal.clone();
				start_cal.add(Calendar.DAY_OF_MONTH, -1);
				origins.add(new CityEvent(p,start_cal,end_cal,0));
			}
		}
		
		String outputdir = Config.getInstance().base_folder+"/UsersAroundAnEvent/"+target_event;
		UsersAroundMultipleEvents uame = new UsersAroundMultipleEvents(null,Config.getInstance().base_folder+"/UsersAroundAnEvent/"+target_event.toFileName(),origins,outputdir);
		PLSParser.parse(uame);
		uame.finish();
		
		
		Logger.logln("Done!");
	}
}
