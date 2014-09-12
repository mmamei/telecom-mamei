package dataset.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import region.CityEvent;
import utils.Config;
import dataset.UsersAroundAnEventI;

public class UsersAroundAnEvent extends BufferAnalyzer implements UsersAroundAnEventI {
	
	
	private Set<String> usersAround;
	
	private String outputfile;
	CityEvent event;
	Calendar startTime;
	Calendar endTime;
	
	
	UsersAroundAnEvent() {
		
	}
	
	UsersAroundAnEvent(CityEvent c) {
		event = c;
		usersAround = new HashSet<String>();
		String dir = Config.getInstance().base_folder+"/UsersAroundAnEvent";
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
	String mnt;
	String celllac;
	Calendar cal = new GregorianCalendar();
	
	protected void analyze(String line) {
		fields = line.split("\t");
		username = fields[0];
		mnt = fields[1];
		celllac = fields[2];
		cal.setTimeInMillis(Long.parseLong(fields[3]));
		
		if(cal.before(startTime) || cal.after(endTime) || !event.spot.contains(celllac)) return;
				
		usersAround.add(username+","+mnt);
	}
	
	protected void finish() {
		try {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(outputfile))));
		for(String user: usersAround)
			out.println(user);
		out.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	
	public Set<String> process(CityEvent ce) {
		return process(ce,false);
	}
		
	public Set<String> process(CityEvent ce, boolean saveFile) {
		try {
			UsersAroundAnEvent ba = new UsersAroundAnEvent(ce);
			//UsersAroundAnEvent ba = new UsersAroundAnEvent(CityEvent.GAME_OLIMPICO_12_3_2012);
			PLSParser.parse(ba);
			if(saveFile) ba.finish();
		return ba.usersAround;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		UsersAroundAnEvent uae = new UsersAroundAnEvent();
		uae.process(CityEvent.getEvent("Stadio Olimpico (TO),12/03/2012"),true);
	}
	
}
