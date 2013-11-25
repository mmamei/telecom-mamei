package pls_parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import utils.Config;
import utils.FileUtils;
import utils.Logger;
import area.Placemark;

public class UserEventCounterDetailed extends BufferAnalyzer {
	
	private Placemark placemark;
	private String hashmap_outputfile;
	private Map<String,UserInfo> users_info;
	
	public UserEventCounterDetailed(Placemark placemark) {
		this.placemark = placemark;
		users_info = new HashMap<String,UserInfo>();
		
		File fd = FileUtils.getFile("UserEventCounterDetailed");
		if(fd == null) {
			fd = new File("C:"+Config.getInstance().base_dir+"/UserEventCounterDetailed");
			fd.mkdirs();
		}
		hashmap_outputfile = fd.getAbsolutePath()+"/"+placemark.name+".csv";
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
	
	public void analyze(String line) {
		try {
			fields = line.split("\t");
			username = fields[0];
			imsi = fields[1];
			celllac = fields[2];
			timestamp = Long.parseLong(fields[3]);
			
			if(placemark.contains(celllac)){
				UserInfo info = users_info.get(username);
				if(info == null) {
					info = new UserInfo();
					info.imsi = imsi;
					users_info.put(username, info);
				}
				info.num_pls++;
				info.time_of_first_pls = Math.min(info.time_of_first_pls,timestamp);
				info.time_of_last_pls = Math.max(info.time_of_last_pls, timestamp);
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
		Placemark p = Placemark.getPlacemark("Venezia");
		UserEventCounterDetailed ba = new UserEventCounterDetailed(p);
		if(!new File(ba.hashmap_outputfile).exists()) {
			PLSParser.parse(ba);
			ba.finish();
			Logger.logln("Done");
		}
		else Logger.logln("file already exists!");
		trim(p,3);
	}
	
	/*
	 * This main is places here for convenience. It just read the file and remove all the users producing few events
	 */
	public static void trim(Placemark p, int min_size) throws Exception {
		BufferedReader br = FileUtils.getBR("UserEventCounterDetailed/"+p.name+".csv");
		PrintWriter out = FileUtils.getPW("UserEventCounterDetailed", p.name+"_trim"+min_size+".csv");
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
}


class UserInfo {
	String imsi;
	long time_of_first_pls = Long.MAX_VALUE;
	long time_of_last_pls = 0;
	int num_pls = 0;
	
	public String toString() {
		
		Calendar first = Calendar.getInstance();
		first.setTimeInMillis(time_of_first_pls);
		
		Calendar last = Calendar.getInstance();
		last.setTimeInMillis(time_of_last_pls);
		
		
		
		return imsi+","+num_pls+","+time_of_first_pls+","+time_of_last_pls+","+first.getTime()+","+last.getTime();
	}
}
