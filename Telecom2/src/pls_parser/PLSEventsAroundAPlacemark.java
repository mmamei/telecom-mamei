package pls_parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import network.NetworkCell;
import network.NetworkMap;
import network.NetworkMapFactory;
import utils.Config;
import utils.Logger;
import area.Placemark;

public class PLSEventsAroundAPlacemark extends BufferAnalyzer {	

	private List<PrintWriter> outs;
	private List<Placemark> placemarks;
	private NetworkMap nm = NetworkMapFactory.getNetworkMap();
	
	public PLSEventsAroundAPlacemark(List<Placemark> ps, double[] radii) {
		
		outs = new ArrayList<PrintWriter>();
		placemarks = new ArrayList<Placemark>();
		
		try {
			String dir = Config.getInstance().base_dir+"/"+this.getClass().getSimpleName();
			File fd = new File(dir);
			if(!fd.exists()) fd.mkdirs();	
			
			for(Placemark p: ps)
			for(double r: radii) {
				outs.add(new PrintWriter(new BufferedWriter(new FileWriter(new File(dir+"/"+p.name+"_"+r+".txt")))));
				placemarks.add(new Placemark(p.region,p.name,p.center,r));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
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
		long cellac = Long.parseLong(celllac);
		NetworkCell nc = nm.get(cellac);
		
		for(int i=0; i<placemarks.size();i++) {
			if(placemarks.get(i).contains(celllac)) {
				if(nc == null) outs.get(i).println(username+","+timestamp+","+imsi+",null");
				else outs.get(i).println(username+","+timestamp+","+imsi+","+cellac+","+nc.getCellName());
			}
		}
	}
	
	public void finish() {
		for(PrintWriter out: outs)
			out.close();
	}
	
	public static void process(Placemark p) throws Exception {
		List<Placemark> ps = new ArrayList<Placemark>();
		ps.add(p);
		PLSEventsAroundAPlacemark ba = new PLSEventsAroundAPlacemark(ps, new double[]{p.radius});
		PLSParser.parse(ba);
		ba.finish();
	}
	
	public static void process(List<Placemark> p, double[] r) throws Exception {
		PLSEventsAroundAPlacemark ba = new PLSEventsAroundAPlacemark(p, r);
		PLSParser.parse(ba);
		ba.finish();
	}
	
	
	static double[] rs = new double[]{1500,1400,1300,1200,1100,1000,900,800,700,600,500,400,300,200,100,0,-100,-200,-300,-400,-500};
	static List<Placemark> ps = new ArrayList<Placemark>();
	static {
		/*
		ps.add(Placemark.getPlacemark("Juventus Stadium (TO)"));
		*/
		ps.add(Placemark.getPlacemark("Stadio Olimpico (TO)"));
		/*
		ps.add(Placemark.getPlacemark("Stadio Silvio Piola (NO)"));
		ps.add(Placemark.getPlacemark("Stadio San Siro (MI)"));
		ps.add(Placemark.getPlacemark("Stadio Atleti Azzurri d'Italia (BG)"));
		ps.add(Placemark.getPlacemark("Stadio Mario Rigamonti (BS)"));
		ps.add(Placemark.getPlacemark("Stadio Franco Ossola (VA)"));
		*/
		
		ps.add(Placemark.getPlacemark("Piazza San Carlo (TO)"));
		ps.add(Placemark.getPlacemark("Piazza Castello (TO)"));
		ps.add(Placemark.getPlacemark("Piazza Vittorio (TO)"));
		ps.add(Placemark.getPlacemark("Parco Dora (TO)"));
	}
	
	public static void main(String[] args) throws Exception {
		process(ps,rs);
		Logger.logln("Done");
	}
}
