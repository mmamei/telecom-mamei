package pls_parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import utils.Config;
import utils.Logger;

public class UserEventCounter extends BufferAnalyzer {
	
	private String hashmap_outputfile;
	private Map<String,Integer> users_events;
	
	public UserEventCounter() {
		users_events = new HashMap<String,Integer>();
		String dir = Config.getInstance().base_dir+"/"+this.getClass().getSimpleName();
		File fd = new File(dir);
		if(!fd.exists()) fd.mkdirs();
		String f = Config.getInstance().pls_folder;
		f = f.substring(f.lastIndexOf("/")+1);
		hashmap_outputfile = dir+"/"+f+".csv";
	}

	
	/*
	<HASH_MSISDN> - identifica in modo univoco l’utente;
	<prefisso_IMSI> - contiene MCC (prime tre cifre del campo) e MNC (ultime due cifre del campo);
	<CELLLAC> - individua la posizione dell’utente e si ottiene combinando il CELLID e il LACID con la seguente formula:
	<TIMESTAMP> - è lo UNIX timestamp relativo all’istante esatto in cui è stata effettuata l’attività dall’utente.
	*/
	
	String[] fields;
	String username;
	
	public void analyze(String line) {
		fields = line.split("\t");
		username = fields[0];
		Integer n = users_events.get(username);
		users_events.put(username, n == null ? 1 : n+1);
	}
	
	public void finish() {
		try {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(hashmap_outputfile))));
		for(String user: users_events.keySet())
			out.println(user+","+users_events.get(user));
		out.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void extractUsersAboveThreshol(int n) throws Exception {
		File f = new File(hashmap_outputfile); 
		if(!f.exists()) {
			PLSParser.parse(this);
			finish();
		}
		
		String fn = hashmap_outputfile.substring(0,hashmap_outputfile.lastIndexOf("."));
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(fn+"_users_above_"+n+".txt"))));
		
		BufferedReader br = new BufferedReader(new FileReader(f));
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
		UserEventCounter ba = new UserEventCounter();
		if(!new File(ba.hashmap_outputfile).exists()) {
			PLSParser.parse(ba);
			ba.finish();
			Logger.logln("Done");
		}
		ba.extractUsersAboveThreshol(2000);
	}
	
}
