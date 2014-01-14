package pls_parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import utils.FileUtils;
import utils.Logger;
import area.Placemark;

public class UserEventCounterCellacXHour extends BufferAnalyzer {
	
	private Placemark placemark;
	private String hashmap_outputfile;
	private Map<String,UserInfo> users_info;
	
	static final String[] MONTHS = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	static final String[] DAY_WEEK = new String[]{"0","Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
	
	public UserEventCounterCellacXHour(Placemark placemark) {
		this.placemark = placemark;
		users_info = new HashMap<String,UserInfo>();
		
		File fd = FileUtils.getFile("UserEventCounter");
		if(fd == null) {
			fd = FileUtils.create("UserEventCounter");
			fd.mkdirs();
		}
		hashmap_outputfile = fd.getAbsolutePath()+"/"+placemark.name+"_cellacXhour.csv";
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
	long timestamp;
	Calendar cal = new GregorianCalendar();
	public void analyze(String line) {
		try {
			fields = line.split("\t");
			username = fields[0];
			imsi = fields[1];
			celllac = fields[2];
			timestamp = Long.parseLong(fields[3]);
			cal.setTimeInMillis(timestamp);
			if(placemark.contains(celllac)){
				UserInfo info = users_info.get(username);
				if(info == null) {
					info = new UserInfo();
					info.imsi = imsi;
					users_info.put(username, info);
				}
				String day = cal.get(Calendar.YEAR)+"-"+cal.get(Calendar.MONTH)+"-"+cal.get(Calendar.DAY_OF_MONTH);
				String dayw = DAY_WEEK[cal.get(Calendar.DAY_OF_WEEK)];
				info.add(day,dayw,cal.get(Calendar.HOUR_OF_DAY),celllac);
			}
		} catch(Exception e) {
			System.err.println("Problems... "+line);
		}
	}
	
	
	public void finish() {
		try {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(hashmap_outputfile))));
		for(String user: users_info.keySet())
			out.println(user+","+users_info.get(user));
		out.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		process("Firenze");
		Logger.logln("Done");
	}	
		
	public static void process(String city) throws Exception {
		Placemark p = Placemark.getPlacemark(city);
		UserEventCounterCellacXHour ba = new UserEventCounterCellacXHour(p);
		if(!new File(ba.hashmap_outputfile).exists()) {
			PLSParser.parse(ba);
			ba.finish();
		}
		else Logger.logln("file already exists!");
		trim(p,3);
	}
	
	/*
	 * This main is places here for convenience. It just read the file and remove all the users producing few events
	 */
	public static void trim(Placemark p, int min_size) throws Exception {
		BufferedReader br = FileUtils.getBR("UserEventCounter/"+p.name+"_cellacXhour.csv");
		PrintWriter out = FileUtils.getPW("UserEventCounter", p.name+"_cellacXhour_trim"+min_size+".csv");
		String line;
		while((line = br.readLine()) != null) {
			int num_pls = Integer.parseInt(line.split(",")[2]);
			if(num_pls >= min_size)
				out.println(line);
		}
		br.close();
		out.close();
		Logger.logln("Done!");
	}
	
	/***********************************************************************************************************************/
	/* USER INFO INNER CLASS */
	
	private class UserInfo {
		
		String imsi;
		Set<String> pls = new HashSet<String>();
		
		public void add(String day, String dayw, int h, String cellac) {
			pls.add(day+":"+dayw+":"+h+":"+celllac);
		}
		
		public int getNumDays() {
			Set<String> days = new HashSet<String>();
			for(String p:pls) 
				days.add(p.substring(0, p.indexOf(":")));
			return days.size();
		}
		
		public int getDaysInterval() {
			
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
			return 1+(int)Math.floor((max.getTimeInMillis() - min.getTimeInMillis())/(1000*3600*24));
		}
		
		public String toString() {			
			StringBuffer sb = new StringBuffer();
			for(String p:pls) 
				sb.append(p+",");
			return imsi+","+pls.size()+","+getNumDays()+","+getDaysInterval()+","+sb.toString();
		}
	}

}