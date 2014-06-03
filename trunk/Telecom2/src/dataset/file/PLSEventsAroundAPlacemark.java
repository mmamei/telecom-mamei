package dataset.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import region.Placemark;
import region.RegionMap;
import utils.Config;
import utils.FileUtils;
import utils.Logger;
import dataset.PLSEventsAroundAPlacemarkI;

class PLSEventsAroundAPlacemark extends BufferAnalyzer implements PLSEventsAroundAPlacemarkI  {	
	private Map<String,Object> constraints;
	
	private List<Placemark> placemarks;
	private List<Map<String,UserInfo>> userInfos;
	
	//private RegionMap nm = DataFactory.getNetworkMapFactory().getNetworkMap(Config.getInstance().pls_start_time);
	
	PLSEventsAroundAPlacemark() {
	}
	
	PLSEventsAroundAPlacemark(List<Placemark> ps, Map<String,Object> constraints) {
		this.constraints = constraints;
		userInfos = new ArrayList<Map<String,UserInfo>>();
		placemarks = new ArrayList<Placemark>();
		for(Placemark p: ps)  {
			placemarks.add(new Placemark(p.getName(),p.getLatLon(),p.getRadius()));
			userInfos.add(new HashMap<String,UserInfo>());
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
				UserInfo ui = userInfos.get(i).get(username);
				if(ui == null) {
					ui = new UserInfo(username,mnt);
					userInfos.get(i).put(username, ui);
				}
				ui.add(timestamp, celllac);
			}
		}
		}catch(Exception e) {
			System.err.println(line);
		}
	}
	
	protected void finish() {
		try {
			String dir = FileUtils.createDir("BASE/PLSEventsAroundAPlacemark").getAbsolutePath();
			dir = dir+"/"+Config.getInstance().get_pls_subdir();
			
			System.out.println("Output Dir = "+dir);
			
			File fd = new File(dir);
			if(!fd.exists()) fd.mkdirs();	
			
			for(int i=0; i<placemarks.size();i++) {
				Placemark p = placemarks.get(i);
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(dir+"/"+p.getName()+"_"+p.getRadius()+getFileSuffix(constraints)+".txt"))));
				
				for(UserInfo ui: userInfos.get(i).values())
					if(okConstraints(ui)) out.println(ui);
				out.close();
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private boolean okConstraints(UserInfo ui) {
		String mnt  = (String)constraints.get("mnt");
		if(mnt!=null) {
			if(mnt.startsWith("!")) { 
				//System.err.println(ui.mnt+"VS"+mnt.substring(1));
				if(ui.mnt.equals(mnt.substring(1))) return false;
			}
			else
				if(!ui.mnt.equals(mnt)) return false;
		}
		Integer mindays = (Integer)constraints.get("mindays");
		if(mindays!=null) 
			if(ui.getNumDays() < mindays) return false;
		
		
		Integer maxdays = (Integer)constraints.get("maxdays");
		if(maxdays!=null) 
			if(ui.getNumDays() > maxdays) return false;
		return true;
	}
	
	public void process(Placemark p, Map<String,Object> constraints) throws Exception {
		List<Placemark> ps = new ArrayList<Placemark>();
		ps.add(p);
		PLSEventsAroundAPlacemark ba = new PLSEventsAroundAPlacemark(ps,constraints);
		PLSParser.parse(ba);
		ba.finish();
	}
	
	private void process(List<Placemark> p, Map<String,Object> constraints) throws Exception {
		PLSEventsAroundAPlacemark ba = new PLSEventsAroundAPlacemark(p,constraints);
		PLSParser.parse(ba);
		ba.finish();
	}
	
	public String getFileSuffix(Map<String,Object> constraints) {
		String suffix = "";
		if(constraints != null) {
			for(String key: constraints.keySet())
				suffix += "_"+key+"_"+constraints.get(key);
		}
		return suffix;
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
		
		Map<String,Object> constraints = null;
		PLSEventsAroundAPlacemark pap = new PLSEventsAroundAPlacemark();
		pap.process(ps,constraints);
		Logger.logln("Done");
	}
	
	
	
	
	/***********************************************************************************************************************/
	/* USER INFO INNER CLASS */
	private static SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
	private static Calendar cal = Calendar.getInstance();
	private class UserInfo {
		private String username;
		private String mnt;
		private List<String> pls;
		
		UserInfo(String username, String mnt) {
			this.username = username;
			this.mnt = mnt;
			pls = new ArrayList<String>();
		}
		
		
		public void add(String timestamp, String cellac) {
			pls.add(timestamp+":"+celllac);
		}
		
		public int getNumDays() {
			return getDays().size();
		}
		
		
		public Set<String> getDays() {
			Set<String> days = new HashSet<String>();
			for(String p:pls) {
				cal.setTimeInMillis(Long.parseLong(p.substring(0,p.indexOf(":"))));
				days.add(sd.format(cal.getTime()));
			}
			return days;
		}
		
		public int getDaysInterval() {
			Calendar[] min_max = getTimeRange();
			Calendar min = min_max[0];
			Calendar max = min_max[1];
			return 1+(int)Math.floor((max.getTimeInMillis() - min.getTimeInMillis())/(1000*3600*24));
		}
		
		public Calendar[] getTimeRange() {
			Calendar min = null;
			Calendar max = null;
			for(String p:pls) {
				String[] day = p.substring(0, p.indexOf(":")).split("-"); // cal.get(Calendar.YEAR)+"-"+cal.get(Calendar.MONTH)+"-"+cal.get(Calendar.DAY_OF_MONTH);
				int year = Integer.parseInt(day[0]);
				int month = Integer.parseInt(day[1]);
				int d = Integer.parseInt(day[2]);
				Calendar c = new GregorianCalendar(year,month,d);
				if(min == null || c.before(min)) min = c;
				if(max == null || c.after(max)) max = c;
			}
			return new Calendar[]{min,max};
		}
	
		
		public String toString() {			
			StringBuffer sb = new StringBuffer();
			for(String p:pls) {
				String[] tc = p.split(":"); 
				sb.append(username+","+tc[0]+","+mnt+","+tc[1]+"\n");
			}
			return sb.toString();
			
		}
	}

}
