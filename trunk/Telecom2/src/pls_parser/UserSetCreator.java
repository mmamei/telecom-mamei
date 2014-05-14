package pls_parser;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import utils.FileUtils;
import utils.Logger;


public class UserSetCreator extends BufferAnalyzerConstrained {
	
	private Set<String> users;
	
	UserSetCreator(String placemark_name, String user_list_name) {
		super(placemark_name, user_list_name);
		users = new HashSet<String>();
	}

	
	void analyze(String username, String imsi, String celllac,long timestamp, Calendar cal,String header) {
		users.add(username);
	}

	void finish() {
		try {
			File dir = FileUtils.createDir("BASE/UserSetCreator");
			PrintWriter out = new PrintWriter(new FileWriter(dir+"/"+this.getString()+".csv"));
			for(String u: users)
				out.println(u);
			out.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		BufferAnalyzerConstrained ba = new UserSetCreator("Firenze",null);
		ba.run();
		Logger.logln("Done!");
	}
}
