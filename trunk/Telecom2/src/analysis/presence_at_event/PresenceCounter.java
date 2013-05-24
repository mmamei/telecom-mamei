package analysis.presence_at_event;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;

import pls_parser.UsersCSVCreator;
import utils.Config;
import utils.Logger;
import visual.GraphScatterPlotter;
import analysis.PlsEvent;
import area.CityEvent;

public class PresenceCounter {
	

	public static void main(String[] args) throws Exception {
		
		Collection<CityEvent> events = CityEvent.getEventsInData();
		

		double[][] result = new double[events.size()][2];
		
		int i = 0;
		for(CityEvent ce: events) {
			double c = count(ce);
			Logger.logln(ce.toString()+" estimated attendance = "+(int)c+" groundtruth = "+ce.head_count);
			result[i][0] = c;
			result[i][1] = ce.head_count;
			i++;
		}
		
		new GraphScatterPlotter("Result","Estimated","GroundTruth",result);
		
		String dir = Config.getInstance().base_dir +"/PresenceCounter";
		File d = new File(dir);
		if(!d.exists()) d.mkdirs();
		PrintWriter out = new PrintWriter(new FileWriter(dir+"/result.csv"));
		out.println("event,estimated,groundtruth");
		i=0;
		for(CityEvent ce: events) {
			if(result[i][0] > 0)
				out.println(ce.toString()+","+(int)result[i][0]+","+(int)result[i][1]);
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
			double p = PresenceProbability.presenceProbabilityTest(username,plsEvents,event);
			count += p;	
		}
		return count;
	}
}
