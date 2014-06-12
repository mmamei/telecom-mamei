package dataset.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import region.Placemark;
import utils.Config;
import utils.Logger;
import analysis.Constraints;
import analysis.UserTrace;
import dataset.PLSEventsAroundAPlacemarkI;

class PLSEventsAroundAPlacemark extends BufferAnalyzer implements PLSEventsAroundAPlacemarkI  {	
	private Constraints constraints;
	
	private List<Placemark> placemarks;
	private List<Map<String,UserTrace>> userInfos;
	
	//private RegionMap nm = DataFactory.getNetworkMapFactory().getNetworkMap(Config.getInstance().pls_start_time);
	
	PLSEventsAroundAPlacemark() {
	}
	
	PLSEventsAroundAPlacemark(List<Placemark> ps, Constraints constraints) {
		this.constraints = constraints;
		userInfos = new ArrayList<Map<String,UserTrace>>();
		placemarks = new ArrayList<Placemark>();
		for(Placemark p: ps)  {
			placemarks.add(new Placemark(p.getName(),p.getLatLon(),p.getRadius()));
			userInfos.add(new HashMap<String,UserTrace>());
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
	String mnt;
	String celllac;
	String timestamp;
	
	protected void analyze(String line) {
		try {
		fields = line.split("\t");
		username = fields[0];
		mnt = fields[1];
		celllac = fields[2];
		timestamp = fields[3];
		for(int i=0; i<placemarks.size();i++) {
			if(placemarks.get(i).contains(celllac)) {
				UserTrace ui = userInfos.get(i).get(username);
				if(ui == null) {
					ui = new UserTrace(username,false);
					userInfos.get(i).put(username, ui);
				}
				ui.addEvent(mnt,celllac,timestamp);
			}
		}
		}catch(Exception e) {
			System.err.println(line);
		}
	}
	
	protected void finish() {
		try {
			File fd = new File(Config.getInstance().base_folder+"/PLSEventsAroundAPlacemark/"+Config.getInstance().get_pls_subdir());
			fd.mkdirs();
			
			//System.out.println("Output Dir = "+fd);
			
			
			for(int i=0; i<placemarks.size();i++) {
				Placemark p = placemarks.get(i);
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(fd+"/"+p.getName()+"_"+p.getRadius()+".txt"))));
				
				for(UserTrace ui: userInfos.get(i).values()) {
					if(constraints==null || constraints.okConstraints(ui.mnt, ui.getNumDays())) out.println(ui);
				}
				
				out.close();
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void process(Placemark p, Constraints constraints) throws Exception {
		List<Placemark> ps = new ArrayList<Placemark>();
		ps.add(p);
		PLSEventsAroundAPlacemark ba = new PLSEventsAroundAPlacemark(ps,constraints);
		PLSParser.parse(ba);
		ba.finish();
	}
	
	private void process(List<Placemark> p, Constraints constraints) throws Exception {
		PLSEventsAroundAPlacemark ba = new PLSEventsAroundAPlacemark(p,constraints);
		PLSParser.parse(ba);
		ba.finish();
	}
	
	
	
	
	
	public static void main(String[] args) throws Exception {
		
		
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
		
		Constraints constraints = null;
		PLSEventsAroundAPlacemark pap = new PLSEventsAroundAPlacemark();
		pap.process(ps,constraints);
		Logger.logln("Done");
	}
}
