package dataset.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import region.CityEvent;
import utils.Config;
import utils.Logger;
import analysis.UserTrace;




public class UsersCSVCreator extends BufferAnalyzer {

	private HashMap<String,UserTrace> traces;
	private String subdir;
	
	
	
	UsersCSVCreator(Set<String> users, String subdir) {
		this.subdir = subdir;
		traces = new HashMap<String,UserTrace>();
		for(String u: users) 
			traces.put(u, new UserTrace(u));
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
		if(dir != null) {
			Logger.logln(dir+" is already there! Manually remove before proceeding");
			System.exit(0);
		}
		dir = new File(Config.getInstance().base_folder+"/UsersCSVCreator/"+subdir);
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
		if(file == null) {
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
		Logger.logln("Done");
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
	
	
	public static void main(String[] args) throws Exception {
		File file = new File(Config.getInstance().base_folder+"/UserEventCounter/file_pls_piem_users_above_2000.txt");
		if(file == null) {
			Logger.logln(file+" Does not exist!");
			System.exit(0);
		}
		UsersCSVCreator ba = new UsersCSVCreator(getUserListFromFile(file),"file_pls_piem_users_above_2000");
		if(ba.traces.size() > 0) {
			PLSParser.parse(ba);
			ba.finish();
		}
		Logger.logln("Done");
	}
	
}
