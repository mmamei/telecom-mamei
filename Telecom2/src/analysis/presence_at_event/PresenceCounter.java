package analysis.presence_at_event;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pls_parser.UsersCSVCreator;
import utils.Config;
import utils.Logger;
import visual.GraphScatterPlotter;
import analysis.PlsEvent;
import area.CityEvent;

public class PresenceCounter {
	

	public static void main(String[] args) throws Exception {
		
		Collection<CityEvent> events = CityEvent.getEventsInData();
		
		
		//create a map that associates a Placemark with the list of events happening in there
		Map<String,List<CityEvent>> placemark_events = new HashMap<String,List<CityEvent>>();
		for(CityEvent ce: events) {
			List<CityEvent> l = placemark_events.get(ce.spot.name);
			if(l==null) {
				l = new ArrayList<CityEvent>();
				placemark_events.put(ce.spot.name, l);
			}
			l.add(ce);
		}
		
		List<String> labels = new ArrayList<String>();
		List<double[][]> data = new ArrayList<double[][]>();
		
		for(String p : placemark_events.keySet()) {
			List<CityEvent> pevents =  placemark_events.get(p);
			double[][] result = new double[pevents.size()][2];
			int i = 0;
			for(CityEvent ce: pevents) {
				double c = count(ce);
				Logger.logln(ce.toString()+" estimated attendance = "+(int)c+" groundtruth = "+ce.head_count);
				result[i][0] = c;
				result[i][1] = ce.head_count;
				i++;
			}
			data.add(result);
		}
		
		new GraphScatterPlotter("Result","Estimated","GroundTruth",data,labels);
		
		
		
		String dir = Config.getInstance().base_dir +"/PresenceCounter";
		File d = new File(dir);
		if(!d.exists()) d.mkdirs();
		PrintWriter out = new PrintWriter(new FileWriter(dir+"/result.csv"));
		out.println("event,estimated,groundtruth");
		
		int i = 0;	
		for(String p : placemark_events.keySet()) {
			List<CityEvent> pevents =  placemark_events.get(p);
			double[][] result = data.get(i);
			for(int j=0; j<pevents.size();j++) 
				if(result[j][0] > 0)
					out.println(pevents.get(j).toString()+","+(int)result[j][0]+","+(int)result[j][1]);
			i++;
		}
		out.close();
		Logger.logln("Done!");
	}
		
	public static double count(CityEvent event) throws Exception {	
		
		String inputdir = Config.getInstance().base_dir +"/UsersCSVCreator/"+ event.toString();
		if(!new File(inputdir).exists()) {
			Logger.logln(inputdir+" Does not exist!");
			Logger.logln("Running UsersCSVCreator.create()");
			UsersCSVCreator.create(event);
		}
		else 
			Logger.logln(inputdir+" already exists!");
		
		File[] files = new File(inputdir).listFiles();
		
		double count = 0;
		
		for(int i=0; i<files.length; i++) {
			File f = files[i];	
			if(!f.isFile()) continue;
			String filename = f.getName();
			String username = filename.substring(0, filename.indexOf(".csv"));
			List<PlsEvent> plsEvents = PlsEvent.readEvents(f);
			double p = PresenceProbability.presenceProbability(username,plsEvents,event,1000);	
			count += p;	
		}
		return count;
	}
}
