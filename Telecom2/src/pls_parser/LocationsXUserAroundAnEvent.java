package pls_parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import region.CityEvent;
import region.Placemark;
import region.network.NetworkMap;
import region.network.NetworkMapFactory;
import utils.Config;
import utils.Logger;

public class LocationsXUserAroundAnEvent extends BufferAnalyzer {	

	private CityEvent cevent;
	private Map<String,StringBuffer> user_locations;
	
	public LocationsXUserAroundAnEvent(CityEvent cevent) {
		this.cevent = cevent;
		user_locations = new HashMap<String,StringBuffer>();
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
	StringBuffer sb;
	public void analyze(String line) {
		fields = line.split("\t");
		username = fields[0];
		imsi = fields[1];
		celllac = fields[2];
		timestamp = fields[3];
		
		if(!cevent.spot.contains(celllac)) return;
		
		sb = user_locations.get(username);
		if(sb == null) {
			sb = new StringBuffer();
			user_locations.put(username, sb);
		}
		sb.append(" ");
		sb.append(celllac);
	}
	
	public void finish() {
		try {
			
			String dir = "BASE/LocationsXUserAroundAnEvent";
			File fd = new File(dir);
			if(!fd.exists()) fd.mkdirs();
			
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(dir+"/"+cevent.toFileName())));
			
			for(String un : user_locations.keySet()) {
				String loc = user_locations.get(un).substring(1);
				if(loc.contains(" "))
					out.println(un+","+loc);
			}
			out.close();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		
		CityEvent ce = CityEvent.getEvent("Juventus Stadium (TO),20/03/2012");
		ce = CityEvent.expand(ce, 1, 10000);
		
		//Placemark p = new Placemark("Torino",new double[]{45.073036,7.679733},5000);
		//CityEvent ce = new CityEvent(p,"11/03/2012 17:00","11/03/2012 19:00",-1);
		
		LocationsXUserAroundAnEvent ba = new LocationsXUserAroundAnEvent(ce);
		PLSParser.parse(ba);
		ba.finish();
		Logger.logln("Done");
	}
}
