package pls_parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import network.NetworkCell;
import network.NetworkMap;
import utils.Config;
import utils.Logger;
import analysis.PlsEvent;
import area.CityEvent;
import area.Placemark;

public class PLSEventsAroundAnEvent extends BufferAnalyzer {	

	private List<PlsEvent> plsAround;
	
	private String outputfile;
	private CityEvent cevent;
	
	
	public PLSEventsAroundAnEvent(CityEvent ce, int time_shift, double space_shift) {
		String dir = Config.getInstance().base_dir+"/"+this.getClass().getSimpleName();
		File fd = new File(dir);
		if(!fd.exists()) fd.mkdirs();
		this.outputfile = dir+"/"+ce.toFileName();
		cevent = CityEvent.expand(ce,time_shift,space_shift);
		plsAround = new ArrayList<PlsEvent>();
		Logger.logln("Extracting pls events  generated close to: "+cevent.toString());
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
	
	public void analyze(String line) {
		fields = line.split("\t");
		username = fields[0];
		imsi = fields[1];
		celllac = fields[2];
		timestamp = fields[3];
		
		if(!cevent.spot.contains(celllac)) return;
		
		plsAround.add(new PlsEvent(username,imsi,Long.parseLong(celllac),timestamp));
	}
	
	public void finish() {
		try {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(outputfile))));
		for(PlsEvent e: plsAround)
			out.println(e.toCSV());
		out.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
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
