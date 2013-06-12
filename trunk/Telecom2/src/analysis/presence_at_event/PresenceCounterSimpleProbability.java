package analysis.presence_at_event;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math.stat.regression.SimpleRegression;

import pls_parser.PLSEventsAroundAPlacemark;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;
import visual.GraphScatterPlotter;
import analysis.PlsEvent;
import area.CityEvent;
import area.Placemark;

public class PresenceCounterSimpleProbability {
	
	
	public static void main(String[] args) throws Exception {
		
		double o_radius = 0;
		int days = 3;
		process(o_radius,days);
		
	}
		
	public static void process(double o_radius, int days) throws Exception {
		
		Logger.log("Processing: o_radius = "+o_radius+" days = "+days+" ");
		
		Map<String,Double> bestRadius = (Map<String,Double>)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_dir+"/PlacemarkRadiusExtractor/result.ser"));		
		
		List<CityEvent> events = CityEvent.getEventsInData();
		
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
		SimpleRegression sr = new SimpleRegression();	
		
		for(String p : placemark_events.keySet()) {
			
			//if(!p.equals("Juventus Stadium (TO)") && !p.equals("Stadio Mario Rigamonti (BS)")) continue;
			
			labels.add(p);
			List<CityEvent> pevents =  placemark_events.get(p);
			double[][] result = new double[pevents.size()][2];
			int i = 0;
			for(CityEvent ce: pevents) {
				double bestr = bestRadius.get(ce.spot.name);
				
				double searchr = Double.isNaN(o_radius) ? bestr : o_radius;
				
				double c = count(ce,bestr,searchr,days);
				
				Logger.logln(ce.toString()+" estimated attendance = "+(int)c+" groundtruth = "+ce.head_count);
				result[i][0] = c;
				result[i][1] = ce.head_count;
				sr.addData(result[i][0], result[i][1]);
				i++;
			}
			data.add(result);
		}
		
		Logger.logln("r="+sr.getR()+", r^2="+sr.getRSquare()+", sse="+sr.getSumSquaredErrors());
		
		
		new GraphScatterPlotter("Result: o_radius = "+o_radius+",days = "+days,"Estimated","GroundTruth",data,labels);
		
		String dir = Config.getInstance().base_dir +"/PresenceCounterSimple";
		File d = new File(dir);
		if(!d.exists()) d.mkdirs();
		PrintWriter out = new PrintWriter(new FileWriter(dir+"/result_"+o_radius+"_"+days+".csv"));
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
		//Logger.logln("Done!");
	}
		
	
	
	public static double count(CityEvent event, double e_radius, double o_radius, int days) throws Exception {	
		
		
		Logger.logln("\n"+event.spot.name+", e_r = "+e_radius+", o_r = "+o_radius);
		
		String file_event = getFile(event.spot.clone(),e_radius);
		String file_other = getFile(event.spot.clone(),o_radius);
		
		Set<String> userPresentDuringEvent = getUsers(file_event,event.st,event.et,null,null);
		Map<String,List<PlsEvent>> usr_pls = getUsersPLS(file_event,userPresentDuringEvent);
		double prob = 0;
		for(String u: usr_pls.keySet()) {
			event.spot.changeRadius(o_radius);
			prob += PresenceProbability.presenceProbabilityTest(u, usr_pls.get(u), event);
		}
		
		return prob;
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
		/*
		Logger.logln(p.name+", "+radius);
		Set<String> cells = new HashSet<String>();
		String line;
		BufferedReader in = new BufferedReader(new FileReader(file));
		while((line = in.readLine()) != null){
			String[] splitted = line.split(",");
			cells.add(splitted[3]);
		}
		in.close();
		for(String cell: cells) 
			Logger.logln(cell);
		*/
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
	
	
	public static Map<String,List<PlsEvent>> getUsersPLS(String file, Set<String> users) throws Exception {
		Map<String,List<PlsEvent>> usr_pls = new HashMap<String,List<PlsEvent>>();
		for(String u: users)
			usr_pls.put(u, new ArrayList<PlsEvent>());
		
		String line;
		BufferedReader in = new BufferedReader(new FileReader(file));
		while((line = in.readLine()) != null){
			String[] splitted = line.split(",");
			List<PlsEvent> list = usr_pls.get(splitted[0]);
			if(list!=null) list.add(new PlsEvent(splitted[0],"imsi",Long.parseLong(splitted[3]),splitted[1]));
		}
		in.close();
		return usr_pls;
	}
	
	
	
}
