package dataset.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import analysis.PLSMap;
import region.Placemark;
import region.RegionI;
import region.RegionMap;
import region.network.NetworkMapFactory;
import utils.Config;
import utils.FileUtils;
import utils.Logger;

public class PLSEventsAroundAPlacemark extends BufferAnalyzer {	

	private List<PrintWriter> outs;
	private List<Placemark> placemarks;
	private RegionMap nm = NetworkMapFactory.getNetworkMap(Config.getInstance().pls_start_time);
	
	PLSEventsAroundAPlacemark(List<Placemark> ps, double[] radii) {
		
		outs = new ArrayList<PrintWriter>();
		placemarks = new ArrayList<Placemark>();
		
		try {
			String dir = FileUtils.createDir("BASE/PLSEventsAroundAPlacemark").getAbsolutePath();
			dir = dir+"/"+Config.getInstance().get_pls_subdir();
			
			System.out.println("Output Dir = "+dir);
			
			File fd = new File(dir);
			if(!fd.exists()) fd.mkdirs();	
			
			for(Placemark p: ps)
			for(double r: radii) {
				outs.add(new PrintWriter(new BufferedWriter(new FileWriter(new File(dir+"/"+p.getName()+"_"+r+".txt")))));
				placemarks.add(new Placemark(p.getName(),p.getLatLon(),r));
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
	
	protected void analyze(String line) {
		try {
		fields = line.split("\t");
		username = fields[0];
		imsi = fields[1];
		celllac = fields[2];
		timestamp = fields[3];
		RegionI nc = nm.getRegion(celllac);
		
		for(int i=0; i<placemarks.size();i++) {
			if(placemarks.get(i).contains(celllac)) {
				if(nc == null) outs.get(i).println(username+","+timestamp+","+imsi+",null");
				else outs.get(i).println(username+","+timestamp+","+imsi+","+celllac+","+nc.getName());
			}
		}
		}catch(Exception e) {
			System.err.println(line);
		}
	}
	
	protected void finish() {
		for(PrintWriter out: outs)
			out.close();
	}
	
	public static void process(Placemark p) throws Exception {
		List<Placemark> ps = new ArrayList<Placemark>();
		ps.add(p);
		PLSEventsAroundAPlacemark ba = new PLSEventsAroundAPlacemark(ps, new double[]{p.getRadius()});
		PLSParser.parse(ba);
		ba.finish();
	}
	
	public static void process(List<Placemark> p, double[] r) throws Exception {
		PLSEventsAroundAPlacemark ba = new PLSEventsAroundAPlacemark(p, r);
		PLSParser.parse(ba);
		ba.finish();
	}
	
	
	public static PLSMap getPLSMap(String file, Placemark within) {
		
		//System.out.println(file);
		//System.out.println(FileUtils.getFile(file));
		
		PLSMap plsmap = new PLSMap();
		String[] splitted;
		String line;
		
		Calendar cal = new GregorianCalendar();
		try {
			BufferedReader in = new BufferedReader(new FileReader(FileUtils.getFile(file)));
			while((line = in.readLine()) != null){
				line = line.trim();
				if(line.length() < 1) continue; // extra line at the end of file
				splitted = line.split(",");
				if(splitted.length == 5 && !splitted[3].equals("null")) {
					
					String username = splitted[0];
					cal.setTimeInMillis(Long.parseLong(splitted[1]));
					String key = getKey(cal);
					String celllac = splitted[3]; 
					if(within==null || within.contains(celllac)) {				
						if(plsmap.startTime == null || plsmap.startTime.after(cal)) plsmap.startTime = (Calendar)cal.clone();
						if(plsmap.endTime == null || plsmap.endTime.before(cal)) plsmap.endTime = (Calendar)cal.clone();
						Set<String> users = plsmap.usr_counter.get(key);
						if(users == null) users = new TreeSet<String>();
						users.add(username);
						plsmap.usr_counter.put(key, users);
						Integer count = plsmap.pls_counter.get(key);
						plsmap.pls_counter.put(key, count == null ? 0 : count+1);	
					}
				}
			}
			in.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return plsmap;
	}
	static final String[] MONTHS = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	public static String getKey(Calendar cal) {
		return cal.get(Calendar.DAY_OF_MONTH)+"-"+
			 	MONTHS[cal.get(Calendar.MONTH)]+"-"+
			 	cal.get(Calendar.YEAR)+":"+
			 	cal.get(Calendar.HOUR_OF_DAY);
	}
	
	
	public static void main(String[] args) throws Exception {
		
		double[] rs = new double[]{1500,1400,1300,1200,1100,1000,900,800,700,600,500,400,300,200,100,0,-100,-200,-300,-400,-500};
		List<Placemark> ps = new ArrayList<Placemark>();
		ps.add(Placemark.getPlacemark("Stadio San Siro (MI)"));
		ps.add(Placemark.getPlacemark("Stadio Atleti Azzurri d'Italia (BG)"));
		ps.add(Placemark.getPlacemark("Stadio Mario Rigamonti (BS)"));
		ps.add(Placemark.getPlacemark("Stadio Franco Ossola (VA)"));
				
		//ps.add(Placemark.getPlacemark("Juventus Stadium (TO)"));
		//ps.add(Placemark.getPlacemark("Stadio Olimpico (TO)"));
		//ps.add(Placemark.getPlacemark("Stadio Silvio Piola (NO)"));
		//ps.add(Placemark.getPlacemark("Piazza San Carlo (TO)"));
		//ps.add(Placemark.getPlacemark("Piazza Castello (TO)"));
		//ps.add(Placemark.getPlacemark("Piazza Vittorio (TO)"));
		//ps.add(Placemark.getPlacemark("Parco Dora (TO)"));
		
		process(ps,rs);
		Logger.logln("Done");
	}
}
