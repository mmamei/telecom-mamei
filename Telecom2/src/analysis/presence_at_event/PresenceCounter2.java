package analysis.presence_at_event;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pls_parser.PLSEventsAroundAPlacemark;
import utils.Config;
import utils.Logger;
import visual.GraphScatterPlotter;
import analysis.PlsEvent;
import area.CityEvent;

public class PresenceCounter2 {
	

	public static void main(String[] args) throws Exception {
		
		Collection<CityEvent> events = CityEvent.getEventsInData();
		
		double[][] result = new double[events.size()][2];
		
		int i = 0;
		for(CityEvent ce: events) {
			double c = count(ce,2000);
			Logger.logln(ce.toString()+" estimated attendance = "+(int)c+" groundtruth = "+ce.head_count);
			result[i][0] = c;
			result[i][1] = ce.head_count;
			i++;
		}
		
		new GraphScatterPlotter("Result","Estimated","GroundTruth",result);
		
		String dir = Config.getInstance().base_dir +"/PresenceCounter2";
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
		
	public static double count(CityEvent event, double e_radius) throws Exception {	
		event.spot.changeRadius(e_radius);
		String file = Config.getInstance().base_dir+"/PLSEventsAroundAPlacemark/"+event.spot.name+"_"+event.spot.radius+".txt";
		File f = new File(file);
		if(!f.exists()) {
			Logger.logln(file+" does not exist");
			Logger.logln("Executing PLSEventsAroundAPlacemark.process()");
			PLSEventsAroundAPlacemark.process(event.spot);
		}
		else Logger.logln(file+" already exists!");
		
		
		Calendar start = (Calendar)event.st.clone();
		start.add(Calendar.DAY_OF_MONTH, -5);
		
		Calendar end = (Calendar)event.et.clone();
		end.add(Calendar.DAY_OF_MONTH, 5);
		
		Set<String> userPresentDuringEvent = new HashSet<String>();
		Set<String> userPresentAtTheEventTimeOnOtherDays = new HashSet<String>();
		
		String line;
		Calendar cal = new GregorianCalendar();
		BufferedReader in = new BufferedReader(new FileReader(file));
		while((line = in.readLine()) != null){
			String[] splitted = line.split(",");
			if(splitted.length == 5) {
				if(splitted[3].equals("null")) continue;
				cal.setTimeInMillis(Long.parseLong(splitted[1]));
				if(start.before(cal) && end.after(cal)) {
					if(event.st.before(cal) && event.et.after(cal))
						userPresentDuringEvent.add(splitted[0]);
					else
						userPresentAtTheEventTimeOnOtherDays.add(splitted[0]);
				}
			}
			else System.out.println("Problems: "+line);
		}
		in.close();
		
		userPresentDuringEvent.removeAll(userPresentAtTheEventTimeOnOtherDays);
		return userPresentDuringEvent.size();
	}
	
}
