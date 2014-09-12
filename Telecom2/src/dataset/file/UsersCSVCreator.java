package dataset.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import region.CityEvent;
import utils.Config;
import utils.Logger;
import utils.Mail;
import analysis.UserTrace;




public class UsersCSVCreator extends BufferAnalyzer {

	private HashMap<String,UserTrace> traces;
	private String subdir;
	
	
	
	UsersCSVCreator(Set<String> users, String subdir) {
		this.subdir = subdir;
		
		traces = new HashMap<String,UserTrace>();
		for(String u: users) 
			traces.put(u, new UserTrace(u,false));
	}
	
	String[] fields;
	protected void analyze(String line) {
		fields = line.split("\t");
		UserTrace ut = traces.get(fields[0].trim());
		if(ut != null)
			ut.addEvent(fields[1].trim(), fields[2].trim(), fields[3].trim());
	}
	
	
	private UserTrace get(String user) {
		return traces.get(user);
	}
	
	protected void finish() {
		File dir = new File(Config.getInstance().base_folder+"/UsersCSVCreator/"+subdir);
		dir.mkdirs();
		for(UserTrace ut: traces.values()) 
			try {
				ut.saveToCSV(dir.getAbsolutePath());
			} catch(Exception e) {
				e.printStackTrace();
			}
	}
	
	public static Set<String> getUserListFromFile(File file) {
		Set<String> users = new HashSet<String>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line;
			while((line=in.readLine())!=null) 
				users.add(line.trim());
			in.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return users;
	}
	
	
	public static void create(CityEvent ce) throws Exception {
		File file = new File(Config.getInstance().base_folder+"/UsersAroundAnEvent/"+ce.toFileName());
		if(!file.exists()) {
			Logger.logln(file+" Does not exist!");
			Logger.logln("Running UsersAroundAnEvent.process()");
			try {
				UsersAroundAnEvent uae = new UsersAroundAnEvent();
				uae.process(ce,true);
				file = new File(Config.getInstance().base_folder+"/UsersAroundAnEvent/"+ce.toFileName());
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
		else {
			Logger.logln(file+" already exists!");
		}
		UsersCSVCreator ba = new UsersCSVCreator(getUserListFromFile(file),ce.toString());
		if(ba.traces.size() > 0) {
			PLSParser.parse(ba);
			ba.finish();
		}
	}
	
	public static UserTrace process(String user) {
		try {
			Set<String> users = new HashSet<String>();
			users.add(user);
			UsersCSVCreator ba = new UsersCSVCreator(users,"");
			PLSParser.parse(ba);
			return ba.get(user);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static void main2(String[] args) throws Exception {
		Config.getInstance().changeDataset("ivory-set3");
		String region = "file_pls_ivory";
		//String region = "file_pls_lomb";
		Config.getInstance().pls_folder = Config.getInstance().pls_root_folder+"/"+region;
		Config.getInstance().pls_start_time = new GregorianCalendar(2012,Calendar.FEBRUARY,1);
		Config.getInstance().pls_end_time = new GregorianCalendar(2012,Calendar.MARCH,30);
		
		
		String filename = region+"_users_2000_10000.txt";
		File file = new File(Config.getInstance().base_folder+"/UserEventCounter/"+filename);
		if(!file.exists()) {
			Logger.logln(file+" Does not exist!");
			System.exit(0);
		}
		UsersCSVCreator ba = new UsersCSVCreator(getUserListFromFile(file),filename.substring(0,filename.indexOf(".")));
		if(ba.traces.size() > 0) {
			PLSParser.parse(ba);
			ba.finish();
		}
		Mail.send("UsersCSVCreator completed!");
		Logger.logln("Done");
	}
	
	
	public static void main(String[] args) throws Exception {
		
		String region = "file_pls_pu";
		Config.getInstance().pls_folder = Config.getInstance().pls_root_folder+"/"+region;
		Config.getInstance().pls_start_time = new GregorianCalendar(2014,Calendar.AUGUST,2);
		Config.getInstance().pls_end_time = new GregorianCalendar(2014,Calendar.AUGUST,30);
		
		Set<String> users = new HashSet<String>();
		CityEvent target_event = CityEvent.getEvent("Melpignano,22/08/2014");	
		BufferedReader br = new BufferedReader(new FileReader(Config.getInstance().base_folder+"/UsersAroundAnEvent/"+target_event.toFileName()));
		String line;
		while((line = br.readLine()) != null) {
			String[] usrmnt = line.split(",");
			if(!usrmnt[1].startsWith("222"))
				users.add(usrmnt[0]);
		}
		br.close();
		
		UsersCSVCreator ba = new UsersCSVCreator(users,target_event.toFileName()+"_STR");
		if(ba.traces.size() > 0) {
			PLSParser.parse(ba);
			ba.finish();
		}
		Mail.send("UsersCSVCreator completed!");
		Logger.logln("Done");
	}
	
}
