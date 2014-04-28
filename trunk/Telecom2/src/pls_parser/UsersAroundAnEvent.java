package pls_parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import network.NetworkCell;
import network.NetworkMap;
import utils.Config;
import utils.Logger;
import area.region.CityEvent;

public class UsersAroundAnEvent extends BufferAnalyzer {
	
	
	private Set<String> usersAround;
	
	private String outputfile;
	CityEvent event;
	Calendar startTime;
	Calendar endTime;
	
	
	public UsersAroundAnEvent(CityEvent c) {
		event = c;
		usersAround = new HashSet<String>();
		String dir = "BASE/"+this.getClass().getSimpleName();
		File fd = new File(dir);
		if(!fd.exists()) fd.mkdirs();
		
		this.outputfile = dir+"/"+c.toFileName();
		startTime = c.st;
		endTime = c.et;
		//Logger.logln("Extracting users who were close to: "+c);
	}
	
	public Calendar getStartTime() {
		return startTime;
	}
	public Calendar getEndTime() {
		return endTime;
	}

	
	/*
	<HASH_MSISDN> - identifica in modo univoco l’utente;
	<prefisso_IMSI> - contiene MCC (prime tre cifre del campo) e MNC (ultime due cifre del campo);
	<CELLLAC> - individua la posizione dell’utente e si ottiene combinando il CELLID e il LACID con la seguente formula:
	<TIMESTAMP> - è lo UNIX timestamp relativo all’istante esatto in cui è stata effettuata l’attività dall’utente.
	*/
	
	String[] fields;
	String username;
	String celllac;
	Calendar cal = new GregorianCalendar();
	
	public void analyze(String line) {
		fields = line.split("\t");
		username = fields[0];
		celllac = fields[2];
		cal.setTimeInMillis(Long.parseLong(fields[3]));
		
		if(cal.before(startTime) || cal.after(endTime) || !event.spot.contains(celllac)) return;
				
		usersAround.add(username);
	}
	
	public void finish() {
		try {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(outputfile))));
		for(String user: usersAround)
			out.println(user);
		out.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public Set<String> getUsers() {
		return usersAround;
	}
	
	
	
	public static void process(CityEvent ce) throws Exception {
		UsersAroundAnEvent ba = new UsersAroundAnEvent(ce);
		//UsersAroundAnEvent ba = new UsersAroundAnEvent(CityEvent.GAME_OLIMPICO_12_3_2012);
		PLSParser.parse(ba);
		ba.finish();
		Logger.logln("Done");
	}
	
	public static void main(String[] args) throws Exception {
		process(CityEvent.getEvent("Stadio Olimpico (TO),12/03/2012"));
	}
	
}
