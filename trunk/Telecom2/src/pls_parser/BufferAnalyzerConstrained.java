package pls_parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import network.NetworkMap;
import network.NetworkMapFactory;
import utils.Config;
import area.Placemark;

public abstract class BufferAnalyzerConstrained extends BufferAnalyzer {
	
	private String placemark_name; 
	private String user_list_name;
	private Placemark placemark = null;
	private Set<String> user_list = null;
	
	/*
	 * You must pass a placemark and a userlist (that can be possibly null) to a BufferAnalyzerConstrained.
	 * The semantics of this call is the following:
	 * 
	 * (null, null) extract info from all the users in the config pls file
	 * (placemark, null) extract info from all the users that have an event in the placemark - the placemark must be contained in the config pls file
	 * (null,list) consider only users in the list
	 * 
	 * 
	 */
	
	public BufferAnalyzerConstrained(String placemark_name, String user_list_name) {
		
		this.placemark_name = placemark_name;
		this.user_list_name = user_list_name;
		
		if(placemark_name != null)
			placemark= Placemark.getPlacemark(placemark_name);
		if(user_list_name != null) {
			
			user_list = new HashSet<String>();
			String line = null;
			try {
				BufferedReader br = new BufferedReader(new FileReader(new File(user_list_name)));
				while((line = br.readLine()) != null)
					user_list.add(line.split(",")[0]);
				br.close();
			} catch(Exception e) {
				System.err.println(line);
				e.printStackTrace();
			}
		}
	}
	
	
	public String getString() {
		Config c = Config.getInstance();
		String p = placemark == null ? c.pls_folder.substring(c.pls_folder.lastIndexOf("/")+1) : placemark_name;
		if(user_list == null)
			return p;
		else {
			user_list_name = user_list_name.replaceAll("\\\\", "/");
			return p+"_"+user_list_name.substring(user_list_name.lastIndexOf("/")+1,user_list_name.indexOf("."));
		}
	}
	
	
	String[] fields;
	String username;
	String imsi;
	String celllac;
	long timestamp;
	Calendar cal = new GregorianCalendar();
	
	String header = null;
	
	
	
	NetworkMap nm = NetworkMapFactory.getNetworkMap();
	
	public void analyze(String line) {
	
		if(line.startsWith("//")) {
		 header = header == null ? line : header + line;
		 return;
		}
		try {
			fields = line.split("\t");
			username = fields[0];
			imsi = fields[1];
			celllac = fields[2];
			timestamp = Long.parseLong(fields[3]);
			cal.setTimeInMillis(timestamp);
			
			//if the celllac is not in the networkmap, do not process the pls
			if(nm.get(Long.parseLong(celllac)) == null) return;
			
			
			boolean check_users = user_list == null || user_list.contains(username);
			boolean check_placemark = placemark == null || placemark.contains(celllac);
			
			if(check_users && check_placemark)
				analyze(username,imsi,celllac,timestamp,cal,header);
		} catch(Exception e) {
		}
	}
	
	public abstract void analyze(String username, String imsi,String celllac,long timestamp, Calendar cal, String header);
}
