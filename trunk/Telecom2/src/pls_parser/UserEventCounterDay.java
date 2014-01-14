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

import utils.Config;
import utils.FileUtils;
import utils.Logger;
import area.Placemark;

public class UserEventCounterDay extends BufferAnalyzer {
	
	static final String[] MONTHS = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	
	private Placemark placemark;
	private Set<String> usernames;
	private String prefix;
	private String hashmap_outputfile;
	private Map<String,UserInfo> users_info;
	
	public UserEventCounterDay(Placemark placemark,Set<String> usernames, String prefix) {
		this.placemark = placemark;
		this.usernames = usernames;
		this.prefix = prefix == null ? "" : prefix;
		users_info = new HashMap<String,UserInfo>();
		
		
		File fd = FileUtils.getFile("UserEventCounter");
		if(fd == null) {
			fd = FileUtils.create("UserEventCounter");
			fd.mkdirs();
		}
				
		hashmap_outputfile = placemark!=null? fd.getAbsolutePath()+"/"+prefix+"_"+placemark.name+"_day.csv" : fd.getAbsolutePath()+"/"+prefix+"_"+Config.getInstance().pls_folder.replaceAll("/|:", "_")+"_day.csv";
		System.out.println(hashmap_outputfile);
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
			
			boolean c1 = usernames == null || usernames.contains(username);
			boolean c2 = placemark == null || placemark.contains(celllac);
			
			if(c1 && c2){
				UserInfo info = users_info.get(username);
				if(info == null) {
					info = new UserInfo();
					info.imsi = imsi;
					users_info.put(username, info);
				}
				info.num_pls++;
				info.time_of_first_pls = Math.min(info.time_of_first_pls,timestamp);
				info.time_of_last_pls = Math.max(info.time_of_last_pls, timestamp);
				info.days.add(dayString(cal));
			}
		} catch(Exception e) {
			System.err.println("Problems... "+line);
		}
	}
	
	
	public String dayString(Calendar c) {
		return c.get(Calendar.YEAR)+"-"+c.get(Calendar.MONTH)+"-"+c.get(Calendar.DAY_OF_MONTH);
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
		String city = "Venezia";
		//Placemark p = Placemark.getPlacemark("Venezia");
		process(null,read(city),city);
		Logger.logln("Done");
	}
	
	
	public static Set<String>  read(String city) throws Exception {
		Set<String> user_in_base_region = new HashSet<String>();
		BufferedReader br = FileUtils.getBR("UserEventCounter/"+city+"_day.csv");
		String line;
		while((line = br.readLine()) != null) {
			String user = line.substring(0,line.indexOf(","));
			user_in_base_region.add(user);
		}
		br.close();
		System.out.println(user_in_base_region.size());
		return user_in_base_region;
	}
	
	
	
	public static void process(Placemark p, Set<String> usernames, String prefix) throws Exception {
		UserEventCounterDay ba = new UserEventCounterDay(p,usernames,prefix);
		if(!new File(ba.hashmap_outputfile).exists()) {
			PLSParser.parse(ba);
			ba.finish();
			Logger.logln("Done");
		}
		else Logger.logln("file already exists!");
	}
	
	/*
	 * This main is places here for convenience. It just read the file and remove all the users producing few events
	 */
	public static void trim(Placemark p, int min_size) throws Exception {
		BufferedReader br = FileUtils.getBR("UserEventCounter/"+p.name+"_day.csv");
		PrintWriter out = FileUtils.getPW("UserEventCounter", p.name+"_day_trim"+min_size+".csv");
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
		long time_of_first_pls = Long.MAX_VALUE;
		long time_of_last_pls = 0;
		int num_pls = 0;
		Set<String> days = new HashSet<String>();
		
		public String toString() {
			
			Calendar first = Calendar.getInstance();
			first.setTimeInMillis(time_of_first_pls);
			
			Calendar last = Calendar.getInstance();
			last.setTimeInMillis(time_of_last_pls);
			
			int dt = (int)((time_of_last_pls - time_of_first_pls)/(1000*3600*24));
			
			int sday =  first.get(Calendar.DAY_OF_MONTH);
			int smonth = first.get(Calendar.MONTH);
			int syear = first.get(Calendar.YEAR);
			
			int eday =  last.get(Calendar.DAY_OF_MONTH);
			int emonth = last.get(Calendar.MONTH);
			int eyear = last.get(Calendar.YEAR);
			
			
			return imsi+","+num_pls+","+days.size()+","+dt+","+sday+"-"+MONTHS[smonth]+"-"+syear+","+eday+"-"+MONTHS[emonth]+"-"+eyear;
		}
	}

}


