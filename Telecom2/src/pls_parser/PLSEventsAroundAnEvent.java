package pls_parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;

import network.NetworkCell;
import network.NetworkMap;

import utils.Config;
import utils.Logger;
import analysis.PlsEvent;
import area.CityEvent;

public class PLSEventsAroundAnEvent extends BufferAnalyzer {	

	private CityEvent cevent;
	private PrintWriter out = null;
	
	public PLSEventsAroundAnEvent(CityEvent ce, int time_shift, double space_shift) {
		String dir = Config.getInstance().base_dir+"/"+this.getClass().getSimpleName();
		File fd = new File(dir);
		if(!fd.exists()) fd.mkdirs();
		cevent = CityEvent.expand(ce,time_shift,space_shift);
		Logger.logln("Extracting pls events  generated close to: "+cevent.toString());
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(new File(dir+"/"+ce.toFileName()))));
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	public Calendar getStartTime() {
		return cevent.st;
	}
	public Calendar getEndTime() {
		return cevent.et;
	}
	
	
	
	/*
	<HASH_MSISDN> - identifica in modo univoco l’utente;
	<prefisso_IMSI> - contiene MCC (prime tre cifre del campo) e MNC (ultime due cifre del campo);
	<CELLLAC> - individua la posizione dell’utente e si ottiene combinando il CELLID e il LACID con la seguente formula:
	<TIMESTAMP> - è lo UNIX timestamp relativo all’istante esatto in cui è stata effettuata l’attività dall’utente.
	*/
	
	String[] fields;
	String username;
	String imsi;
	String celllac;
	String timestamp;
	Calendar cal = new GregorianCalendar();
	NetworkMap nm = NetworkMap.getInstance();
	public void analyze(String line) {
		fields = line.split("\t");
		username = fields[0];
		imsi = fields[1];
		celllac = fields[2];
		timestamp = fields[3];
		
		if(!cevent.spot.contains(celllac)) return;
		
		long cellac = Long.parseLong(celllac);
		NetworkCell nc = nm.get(cellac);
		if(nc == null) out.println(username+","+timestamp+","+imsi+",null");
		else out.println(username+","+timestamp+","+imsi+","+cellac+","+nc.getCellName());
	}
	
	public void finish() {
		out.close();
	}
	
	
	public static void process(CityEvent e, int time_shift, double space_shift) throws Exception {
		PLSEventsAroundAnEvent ba = new PLSEventsAroundAnEvent(e,time_shift,space_shift);
		PLSParser.parse(ba);
		ba.finish();
	}
	
	
	public static void main(String[] args) throws Exception {
		CityEvent ce = CityEvent.getEvent("Stadio Silvio Piola (NO),11/03/2012");
		process(ce,10,3000);
		Logger.logln("Done");
	}
}
