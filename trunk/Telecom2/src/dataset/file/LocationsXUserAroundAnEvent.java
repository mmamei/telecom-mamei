package dataset.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import analysis.Constraints;
import region.CityEvent;
import region.Placemark;
import utils.Config;
import utils.Logger;

public class LocationsXUserAroundAnEvent extends BufferAnalyzer {	

	private CityEvent cevent;
	private Map<String,StringBuffer> user_locations;
	private Constraints constraints;;
	public LocationsXUserAroundAnEvent(CityEvent cevent, Constraints constraints) {
		this.cevent = cevent;
		this.constraints = constraints;
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
	protected void analyze(String line) {
		fields = line.split("\t");
		username = fields[0];
		imsi = fields[1];
		celllac = fields[2];
		timestamp = fields[3];
		
		if(!cevent.spot.contains(celllac)) return;
		if(!constraints.okConstraints(imsi, -1)) return;
		
		
		sb = user_locations.get(username);
		if(sb == null) {
			sb = new StringBuffer();
			user_locations.put(username, sb);
		}
		sb.append(" ");
		sb.append(celllac);
	}
	
	protected void finish() {
		try {
			
			String dir = Config.getInstance().base_folder+"/LocationsXUserAroundAnEvent";
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

	
	public static String getOutputFile(Placemark p, Calendar st, Calendar et) throws Exception {
		//"Stadio_Silvio_Piola_(NO)-11_03_2012_18_00-12_03_2012_00_00.txt";//"Torino-11_03_2012_17_00-11_03_2012_19_00.txt";
		return Config.getInstance().base_folder+"/LocationsXUserAroundAnEvent/"+new CityEvent(p,st,et,0).toFileName();
	}
	
	
	public static void process(Placemark p, Calendar st, Calendar et, Constraints constraints) throws Exception {
		CityEvent ce = new CityEvent(p,st,et,0);
		process(ce, constraints);
	}
	
	private static void process(CityEvent ce, Constraints constraints) throws Exception {
		LocationsXUserAroundAnEvent ba = new LocationsXUserAroundAnEvent(ce, constraints);
		PLSParser.parse(ba);
		ba.finish();
	}
	
	
	public static void main(String[] args) throws Exception {
		Config.getInstance().pls_folder = Config.getInstance().pls_root_folder+"/file_pls_piem";
		CityEvent ce = CityEvent.getEvent("Stadio Silvio Piola (NO),11/03/2012");
		ce = CityEvent.expand(ce, 1, 10000);
		process(ce,null);	
		Logger.logln("Done");
	}
}
