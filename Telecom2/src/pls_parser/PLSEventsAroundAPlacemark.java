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

public class PLSEventsAroundAPlacemark extends BufferAnalyzer {	

	private List<PlsEvent> plsAround;
	
	private String outputfile;
	private Placemark placemark;
	
	
	public PLSEventsAroundAPlacemark(Placemark p) {
		String dir = Config.getInstance().base_dir+"/"+this.getClass().getSimpleName();
		File fd = new File(dir);
		if(!fd.exists()) fd.mkdirs();
		this.outputfile = dir+"/"+p.name+".txt";
		placemark = p;
		plsAround = new ArrayList<PlsEvent>();
		Logger.logln("Extracting pls events  generated close to: "+p.name);
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
		
		if(!placemark.contains(celllac)) return;
		
		plsAround.add(new PlsEvent(username,imsi,Long.parseLong(celllac),timestamp));
	}
	
	public void finish() {
		try {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(outputfile))));
		for(PlsEvent e: plsAround)
			out.println(e.toCSV());
		out.flush();
		out.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void process(Placemark p) throws Exception {
		PLSEventsAroundAPlacemark ba = new PLSEventsAroundAPlacemark(p);
		PLSParser.parse(ba);
		ba.finish();
	}
	
	public static void main(String[] args) throws Exception {
		Placemark p = Placemark.getPlacemark("Stadio Olimpico (TO)");
		process(p);
		Logger.logln("Done");
	}
}
