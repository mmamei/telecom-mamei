package analysis.presence_at_event;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Collection;
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
			double c = count(ce);
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
		
	public static double count(CityEvent event) throws Exception {	
		String file = Config.getInstance().base_dir+"/PLSEventsAroundAPlacemark/"+event.spot.name+".txt";
		File f = new File(file);
		if(!f.exists()) {
			Logger.logln(file+" does not exist");
			Logger.logln("Executing PLSEventsAroundAPlacemark.process()");
			PLSEventsAroundAPlacemark.process(event.spot);
		}
		else Logger.logln(file+" already exists!");
		
		List<PlsEvent> events = PlsEvent.readEvents(new File(file));
		
		int nd = PlsEvent.countDays(events) - 1;
		
		Set<String> userPresentDuringEvent = new HashSet<String>();
		Set<String> userPresentAtTheEventTimeOnOtherDays = new HashSet<String>();
		
		for(PlsEvent e : events) {
			int h = e.getCalendar().get(Calendar.HOUR_OF_DAY);
			if(event.st.before(e.getCalendar()) && event.et.after(e.getCalendar()))
				userPresentDuringEvent.add(e.getUsername());
			else if(event.st.get(Calendar.HOUR_OF_DAY) <= h && event.et.get(Calendar.HOUR_OF_DAY) >= h)
				userPresentAtTheEventTimeOnOtherDays.add(e.getUsername());
		}
		
		
		userPresentDuringEvent.removeAll(userPresentAtTheEventTimeOnOtherDays);
		return userPresentDuringEvent.size();
		
		//return (int)(1.0 * userPresentDuringEvent.size() - (1.0 *  userPresentAtTheEventTimeOnOtherDays.size()/nd));
	}
}
