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
import java.util.Set;

import org.apache.commons.math.stat.regression.SimpleRegression;

import pls_parser.PLSEventsAroundAPlacemark;
import utils.Config;
import utils.Logger;
import visual.GraphScatterPlotter;
import area.CityEvent;
import area.Placemark;

public class PresenceCounter3 {
	
	
	public static void main(String[] args) throws Exception {
		
		double[] vals = new double[]{1000,2000,3000};
		for(double e_radius:vals)
		for(double o_radius=e_radius; o_radius<=3000;o_radius=o_radius+10000)
		for(int days=0;days<=0; days+=2)
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
		/*
		String dir = Config.getInstance().base_dir +"/PresenceCounter3";
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
		*/
	}
		
	public static double count(CityEvent event, double e_radius, double o_radius, int days) throws Exception {	
		
		String file_event = getFile(event.spot,e_radius);
		String file_other = getFile(event.spot,o_radius);
		
		Set<String> userPresentDuringEvent = getUsers(file_event,event.st,event.et,null,null);
		
		Calendar start = (Calendar)event.st.clone();
		start.add(Calendar.DAY_OF_MONTH, -days);
		
		Calendar end = (Calendar)event.et.clone();
		end.add(Calendar.DAY_OF_MONTH, days);
		
		Set<String> userPresentAtTheEventTimeOnOtherDays = getUsers(file_other,start,end,event.st,event.et);
		
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
	
	public static Set<String> getUsers(String file, Calendar start, Calendar end, Calendar start_exclude, Calendar end_exclude) throws Exception {
		Set<String> users = new HashSet<String>();
		String line;
		Calendar cal = new GregorianCalendar();
		BufferedReader in = new BufferedReader(new FileReader(file));
		while((line = in.readLine()) != null){
			String[] splitted = line.split(",");
			if(splitted.length == 5) {
				cal.setTimeInMillis(Long.parseLong(splitted[1]));
				if(start.before(cal) && end.after(cal)) {
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
