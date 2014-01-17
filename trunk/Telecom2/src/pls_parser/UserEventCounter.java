package pls_parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import utils.Config;
import utils.FileUtils;
import utils.Logger;

public class UserEventCounter extends BufferAnalyzerConstrained {
	
	private Map<String,Integer> users_events;
	
	public UserEventCounter(String placemark_name, String user_list_name) {
		super(placemark_name,user_list_name);
		users_events = new HashMap<String,Integer>();
	}

	public void analyze(String username, String imsi, String celllac, long timestamp, Calendar cal) {
		Integer n = users_events.get(username);
		users_events.put(username, n == null ? 1 : n+1);
	}
	
	public void finish() {
		PrintWriter out = FileUtils.getPW("UserEventCounter", this.getString()+"_count.csv");
		for(String user: users_events.keySet())
			out.println(user+","+users_events.get(user));
		out.close();
	}
	
	public static void extractUsersAboveThreshol(String file, int n) throws Exception {
		String outfile = file.substring(0,file.indexOf("."))+n+".csv";
		PrintWriter out = new PrintWriter(outfile);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while((line=br.readLine())!=null){
			String[] x = line.split(",");
			String username = x[0];
			int n_events = Integer.parseInt(x[1]);
			if(n_events > n)
				out.println(username);	
		}
		br.close();
		out.close();
		Logger.logln("Done!");
	}
	
	
	
	
	public static void main(String[] args) throws Exception {
		UserEventCounter ba = new UserEventCounter(null,null);
		ba.run();
		Logger.logln("Done!");
		extractUsersAboveThreshol(FileUtils.getFileS("UserEventCounter")+"/Firenze_count.csv",2000);
	}
	
}
