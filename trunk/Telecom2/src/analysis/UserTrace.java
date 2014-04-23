package analysis;


import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import utils.Logger;

public class UserTrace {
	
	private String username;
	private List<PlsEvent> events;
	
	public UserTrace(String username){
		this.username = username;
		events = new ArrayList<PlsEvent>();
	}
	
	public void addEvent(String imsi, long cellac, String timestamp){
		events.add(new PlsEvent(username, imsi, cellac, timestamp));
	}
	
	public List<PlsEvent> getEvents(){
		return events;
	}
	
	public String getUsername(){
		return username;
	}
	
	public boolean hasEvents(){
		if(events.size()>0){
			return true;
		}
		return false;
	}
	
	public void saveToCSV(String folder) throws Exception{
		if(!folder.endsWith("/"))
			folder = folder+"/";
		
		Logger.logln("Saving "+folder+username+".csv");
		PrintWriter out = new PrintWriter(new FileWriter(folder+username+".csv"));
		Iterator<PlsEvent> i = events.iterator();
		while(i.hasNext())
			out.println(i.next().toCSV());
		out.close();
	}
}
