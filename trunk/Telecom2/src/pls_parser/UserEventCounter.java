package pls_parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import utils.Config;
import utils.Logger;
import area.CityEvent;

public class UserEventCounter extends BufferAnalyzer {
	
	private String outputfile;
	private Map<String,Integer> users_events;
	
	public UserEventCounter() {
		users_events = new HashMap<String,Integer>();
		String dir = Config.getInstance().base_dir+"/"+this.getClass().getSimpleName();
		File fd = new File(dir);
		if(!fd.exists()) fd.mkdirs();
		String f = Config.getInstance().pls_folder;
		f = f.substring(f.lastIndexOf("/")+1);
		outputfile = dir+"/"+f+".csv";
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
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(outputfile))));
		for(String user: users_events.keySet())
			out.println(user+","+users_events.get(user));
		out.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	public static void main(String[] args) throws Exception {
		UserEventCounter ba = new UserEventCounter();
		PLSParser.parse(ba);
		ba.finish();
		Logger.logln("Done");
	}
	
}
