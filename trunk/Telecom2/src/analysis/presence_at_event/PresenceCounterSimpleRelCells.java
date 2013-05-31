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

import org.apache.commons.math.stat.regression.SimpleRegression;

import pls_parser.PLSEventsAroundAPlacemark;
import utils.Config;
import utils.Logger;
import visual.GraphScatterPlotter;
import area.CityEvent;
import area.Placemark;

public class PresenceCounterSimpleRelCells {
	
	
	public static void main(String[] args) throws Exception {
		
		double e_radius = 200;
		double o_radius = 1000;
		int days = 5;
	
		process(e_radius,o_radius,days);
		
	}
		
	public static void process(double e_radius, double o_radius, int days) throws Exception {
		
		Logger.log("Processing: e_radius = "+e_radius+" o_radius = "+o_radius+" days = "+days+" ");
		
		Collection<CityEvent> events = CityEvent.getEventsInData();
		
		double[][] result = new double[events.size()][2];
		
		int i = 0;
		for(CityEvent ce: events) {
			double c = count(ce,e_radius,o_radius,days);
			//Logger.logln(ce.toString()+" estimated attendance = "+(int)c+" groundtruth = "+ce.head_count);
			result[i][0] = c;
			result[i][1] = ce.head_count;
			i++;
		}
		
		SimpleRegression sr = new SimpleRegression();
		sr.addData(result);
		Logger.logln("r="+sr.getR()+", r^2="+sr.getRSquare()+", sse="+sr.getSumSquaredErrors());
		
		
		new GraphScatterPlotter("Result: e_radius = "+e_radius+", o_radius = "+o_radius+",days = "+days,"Estimated","GroundTruth",result);
		
		String dir = Config.getInstance().base_dir +"/PresenceCounterSimple";
		File d = new File(dir);
		if(!d.exists()) d.mkdirs();
		PrintWriter out = new PrintWriter(new FileWriter(dir+"/result_"+e_radius+"_"+o_radius+"_"+days+".csv"));
		out.println("event,estimated,groundtruth");
		i=0;
		for(CityEvent ce: events) {
			if(result[i][0] > 0)
				out.println(ce.toString()+","+(int)result[i][0]+","+(int)result[i][1]);
			i++;
		}
		out.close();
		//Logger.logln("Done!");
	}
		
	public static double count(CityEvent event, double e_radius, double o_radius, int days) throws Exception {	
		
		String file_event = getFile(event.spot,event.spot.radius);
		String file_other = getFile(event.spot,o_radius);
		/*
		List<String> relevant_cells = ReleventCellsExtractor.process(event.spot);
		System.out.println(event+" rel cells = "+relevant_cells.size());
		*/
		Set<String> userPresentDuringEvent = getUsers(null,file_event,event.st,event.et,null,null);
		
		Calendar start = (Calendar)event.st.clone();
		start.add(Calendar.DAY_OF_MONTH, -days);
		
		Calendar end = (Calendar)event.et.clone();
		end.add(Calendar.DAY_OF_MONTH, days);
		
		Set<String> userPresentAtTheEventTimeOnOtherDays = getUsers(null,file_other,start,end,event.st,event.et);
		
		userPresentDuringEvent.removeAll(userPresentAtTheEventTimeOnOtherDays);
		
		return userPresentDuringEvent.size();
	}
	
	public static String getFile(Placemark p, double radius) throws Exception{
		p.changeRadius(radius);
		String file = Config.getInstance().base_dir+"/PLSEventsAroundAPlacemark/"+p.name+"_"+p.radius+".txt";
		File f = new File(file);
		if(!f.exists()) {
			Logger.logln(file+" does not exist");
			Logger.logln("Executing PLSEventsAroundAPlacemark.process()");
			PLSEventsAroundAPlacemark.process(p);
		}
		//else Logger.logln(file+" already exists!");
		return file;
	}
	
	public static Set<String> getUsers(List<String> relevant_cells, String file, Calendar start, Calendar end, Calendar start_exclude, Calendar end_exclude) throws Exception {
		Set<String> users = new HashSet<String>();
		String line;
		Calendar cal = new GregorianCalendar();
		BufferedReader in = new BufferedReader(new FileReader(file));
		while((line = in.readLine()) != null){
			String[] splitted = line.split(",");
			if(splitted.length == 5) {
				cal.setTimeInMillis(Long.parseLong(splitted[1]));
				if(start.before(cal) && end.after(cal) &&  (relevant_cells==null || relevant_cells.contains(splitted[3]))) {
					if(start_exclude == null || end_exclude ==null)
						users.add(splitted[0]);
					else if(cal.before(start_exclude) || cal.after(end_exclude))
						users.add(splitted[0]);
				}
			}
			else System.out.println("Problems: "+line);
		}
		in.close();
		return users;
	}
}
