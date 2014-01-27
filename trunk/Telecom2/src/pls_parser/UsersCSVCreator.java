package pls_parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;

import utils.Config;
import utils.Logger;
import analysis.UserTrace;
import area.CityEvent;




public class UsersCSVCreator extends BufferAnalyzer {

	private HashMap<String,UserTrace> traces;
	
	private String outputdir;
	
	
	public UsersCSVCreator(String usersListFile) {
		if(!new File(usersListFile).exists()) {
			Logger.logln(usersListFile+" Does not exist!");
			Logger.logln("You must get the list of usesr by running one of the pls_parser.User* classes!");
			System.exit(0);
		}
		
		String odir_name = usersListFile.substring(usersListFile.lastIndexOf("/")+1,usersListFile.lastIndexOf("."));
		outputdir = Config.getInstance().base_dir +"/"+ this.getClass().getSimpleName() +"/"+ odir_name;
		try {
			FileUtils.deleteDirectory(new File(outputdir));
		} catch (IOException e) {
			e.printStackTrace();
		}
		boolean ok = new File(outputdir).mkdirs();
		if(ok)
			Logger.logln("Create directory: "+outputdir);
		else {
			Logger.logln("Failed to create directory: "+outputdir);
			System.exit(0);
		}
			
		
		traces = new HashMap<String,UserTrace>();
		// file contains the username of the users to be processed
		try {
			BufferedReader in = new BufferedReader(new FileReader(usersListFile));
			String line;
			while((line=in.readLine())!=null) {
				String username = line.trim();
				traces.put(username,new UserTrace(username));
			}
			in.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	String[] fields;
	public void analyze(String line) {
		fields = line.split("\t");
		UserTrace ut = traces.get(fields[0].trim());
		if(ut != null)
			ut.addEvent(fields[1].trim(), Long.parseLong(fields[2].trim()), fields[3].trim());
	}
	
	public void finish() {
		for(UserTrace ut: traces.values()) 
			try {
				ut.saveToCSV(outputdir);
			} catch(Exception e) {
				e.printStackTrace();
			}
	}
	
	
	public static void create(CityEvent ce) throws Exception {
		String file = Config.getInstance().base_dir+"/UsersAroundAnEvent/"+ce.toFileName();
		if(!new File(file).exists()) {
			Logger.logln(file+" Does not exist!");
			Logger.logln("Running UsersAroundAnEvent.process()");
			try {
				UsersAroundAnEvent.process(ce);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
		else {
			Logger.logln(file+" already exists!");
		}
		UsersCSVCreator ba = new UsersCSVCreator(file);
		if(ba.traces.size() > 0) {
			PLSParser.parse(ba);
			ba.finish();
		}
		Logger.logln("Done");
	}
	
	
	public static void main(String[] args) throws Exception {
		String file = "C:/BASE/UserEventCounter/file_pls_piem_users_above_2000.txt";
		UsersCSVCreator ba = new UsersCSVCreator(file);
		if(ba.traces.size() > 0) {
			PLSParser.parse(ba);
			ba.finish();
		}
		Logger.logln("Done");
	}
	
}
