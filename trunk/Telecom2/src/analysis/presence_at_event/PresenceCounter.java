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

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import region.CityEvent;
import region.Placemark;
import utils.Config;
import utils.Logger;
import visual.java.GraphScatterPlotter;
import analysis.Constraints;
import analysis.PLSEvent;
import dataset.DataFactory;
import dataset.PLSEventsAroundAPlacemarkI;

public class PresenceCounter {
	
	public static boolean USE_PROBABILITY = true;
	public static boolean USE_INDIVIDUAL_EVENT = true;
	public static boolean PLOT = false;
	public static boolean WRITE_PROB_SCORES = true;
	
	private static double O_RADIUS = 0;
	private static int DAYS = 3;
	
	
	public static File ODIR = new File(Config.getInstance().base_folder+"/PresenceCounter/"+Config.getInstance().get_pls_subdir());
	public static String IM = PlacemarkRadiusExtractor.USE_INDIVIDUAL_EVENT? "individual" : "multiple";
	public static String SDIFF = PlacemarkRadiusExtractor.DIFF ? "_diff" : "";
	public static String OFILE = "result_"+IM+"_"+O_RADIUS+"_"+DAYS+SDIFF+".csv";
	
	
	public static void main(String[] args) throws Exception {	
		List<CityEvent> events = CityEvent.getEventsInData();
		process(events);
	}
		
	public static void process(List<CityEvent> events) throws Exception {
		Logger.logln("Processing: o_radius = "+O_RADIUS+" days = "+DAYS);
		
		Map<String,Double> bestRadius = PlacemarkRadiusExtractor.readBestR(USE_INDIVIDUAL_EVENT,PlacemarkRadiusExtractor.DIFF);	
		
		//create a map that associates a Placemark with the list of events happening in there
		Map<String,List<CityEvent>> placemark_events = new HashMap<String,List<CityEvent>>();
		for(CityEvent ce: events) {
			List<CityEvent> l = placemark_events.get(ce.spot.getName());
			if(l==null) {
					l = new ArrayList<CityEvent>();
					placemark_events.put(ce.spot.getName(), l);
			}
			l.add(ce);
		}
				
		List<String> labels = new ArrayList<String>();
		List<double[][]> data = new ArrayList<double[][]>();
		SimpleRegression sr = new SimpleRegression();	
		
		for(String p : placemark_events.keySet()) {
			
			labels.add(p);
			List<CityEvent> pevents =  placemark_events.get(p);
			double[][] result = new double[pevents.size()][2];
			int i = 0;
			
			for(CityEvent ce: pevents) {
				
				String key = USE_INDIVIDUAL_EVENT ? ce.toString() : ce.spot.getName();
				double bestr = bestRadius.get(key);
				ce.spot.changeRadius(bestr);
				double c = count(ce,O_RADIUS,DAYS);
				
				//Logger.logln(ce.toString()+" estimated attendance = "+(int)c+" groundtruth = "+ce.head_count);
				System.err.println(ce.toString()+","+bestr+","+(int)c+","+ce.head_count);
				
				result[i][0] = c;
				result[i][1] = ce.head_count;
				sr.addData(result[i][0], result[i][1]);
				i++;
				//System.exit(0);
			}
			
			data.add(result);
		}
		
		Logger.logln("r="+sr.getR()+", r^2="+sr.getRSquare()+", sse="+sr.getSumSquaredErrors());
		
		if(PLOT) new GraphScatterPlotter("PC Result: o_radius = "+O_RADIUS+",days = "+DAYS+",R = "+sr.getR(),"Estimated","GroundTruth",data,labels);
		
		

		ODIR.mkdirs();
		PrintWriter out = new PrintWriter(new FileWriter(ODIR+"/"+OFILE));
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
		
	
	
	public static double count(CityEvent event, double o_radius, int days) throws Exception {	
		
		String suffix = USE_INDIVIDUAL_EVENT ? "" : "Multi";
		
		PrintWriter out = null;
		if(WRITE_PROB_SCORES) {
			String dir = Config.getInstance().base_folder+"/PresenceCounter/"+Config.getInstance().get_pls_subdir()+"/ProbScores"+suffix;
			File d = new File(dir);
			if(!d.exists()) d.mkdirs();
			out = new PrintWriter(new FileWriter(dir+"/"+event.toFileName()));
		}
		
		//Logger.logln("\n"+event.spot.name+", e_r = "+event.spot.radius);
		
		//Logger.logln("PLSAroundAPlacemark with BEST radius....");
		File file_event = getFile(event.spot.clone(),event.spot.getRadius());
		//Logger.logln("PLSAroundAPlacemark with OTHER radius....");
		File file_other = getFile(event.spot.clone(),o_radius);
		
		Set<String> userPresentDuringEvent = getUsers(file_event,event.st,event.et,null,null);
		
		Calendar start = (Calendar)event.st.clone();
		start.add(Calendar.DAY_OF_MONTH, -days);
	
		Calendar end = (Calendar)event.et.clone();
		end.add(Calendar.DAY_OF_MONTH, days);
		
		
		CityEvent e2 = new CityEvent(event.spot.clone(),start,end,event.head_count);
		e2.spot.changeRadius(o_radius);
		
		Map<String,List<PLSEvent>> usr_pls = getUsersPLS(file_event,userPresentDuringEvent);
		Map<String,List<PLSEvent>> usr_other_pls = getUsersPLS(file_other,userPresentDuringEvent);
		
		
		double prob = 0;
		
		
		for(String u: usr_pls.keySet()) {
			
			double f1 = fractionOfTimeInWhichTheUserWasAtTheEvent(usr_pls.get(u),event,null,false);
			double f2 = fractionOfTimeInWhichTheUserWasAtTheEvent(usr_other_pls.get(u),e2,event,false);
			
			if(!USE_PROBABILITY) {
				if(f1 > 0) f1 = 1;
				if(f2 > 0) f2 = 1;
			}
			
			if(WRITE_PROB_SCORES) out.println(u+";"+(f1 * (1-f2)));
			prob +=  f1*(1-f2);
		}
		
		if(WRITE_PROB_SCORES) out.close();
		return prob;
	}
	
	
	static Constraints constraints;
	
	public static File getFile(Placemark p, double radius) throws Exception{
		p.changeRadius(radius);
		
		PLSEventsAroundAPlacemarkI pap = DataFactory.getPLSEventsAroundAPlacemark();
		
		
		File f = new File(Config.getInstance().base_folder+"/PLSEventsAroundAPlacemark/"+Config.getInstance().get_pls_subdir()+"/"+p.getName()+"_"+p.getRadius()+".txt");
		if(f==null || !f.exists()) {
			Logger.logln("-------------------------------------------------------------->Executing PLSEventsAroundAPlacemark.process()"+constraints);
			pap.process(p,constraints);
			f = new File(Config.getInstance().base_folder+"/PLSEventsAroundAPlacemark/"+Config.getInstance().get_pls_subdir()+"/"+p.getName()+"_"+p.getRadius()+".txt");
		}
		return f;
	}
	
	public static Set<String> getUsers(File file, Calendar start, Calendar end, Calendar start_exclude, Calendar end_exclude) throws Exception {
		Set<String> users = new HashSet<String>();
		String line;
		Calendar cal = new GregorianCalendar();
		BufferedReader in = new BufferedReader(new FileReader(file));
		while((line = in.readLine()) != null){
			String[] splitted = line.split(",");
			if(splitted.length == 4) {
				try {
					cal.setTimeInMillis(Long.parseLong(splitted[1]));
				} catch(NumberFormatException e) {
					System.err.println(line);
					continue;
				}
				if(start.before(cal) && end.after(cal)) {
					if(start_exclude == null || end_exclude ==null)
						users.add(splitted[0]);
					else if(cal.before(start_exclude) || cal.after(end_exclude)) {
						users.add(splitted[0]);
					}
				}
			}
			//else System.out.println("Problems: "+line);
		}
		in.close();
		return users;
	}
	
	
	public static Map<String,List<PLSEvent>> getUsersPLS(File file, Set<String> users) throws Exception {
		
		Map<String,List<PLSEvent>> usr_pls = new HashMap<String,List<PLSEvent>>();
		for(String u: users)
			usr_pls.put(u, new ArrayList<PLSEvent>());
		
		String line;
		BufferedReader in = new BufferedReader(new FileReader(file));
		while((line = in.readLine()) != null){
			String[] splitted = line.split(",");
			List<PLSEvent> list = usr_pls.get(splitted[0]);
			try{
			if(list!=null) list.add(new PLSEvent(splitted[0],splitted[2],splitted[3],splitted[1]));
			}catch(NumberFormatException e) {
				System.err.println(line);
				continue;
			}
		}
		in.close();
		return usr_pls;
	}
	
	
	
	public static double fractionOfTimeInWhichTheUserWasAtTheEvent(List<PLSEvent> plsEvents, CityEvent event, CityEvent exclude, boolean verbose) {
		Calendar first = null;
		Calendar last = null;
		boolean inEvent = false;
		
		if(verbose) { 
			System.out.println("EVENT = "+event);
			System.out.println("EXCLUDE = "+exclude);
		}
		
		for(PLSEvent pe: plsEvents) {	
			
			Calendar cal = pe.getCalendar();
			
			if(event.st.before(cal) && event.et.after(cal))
			if(verbose) System.out.println("-"+pe);
			
			if(event.st.before(cal) && event.et.after(cal) && (exclude == null || cal.before(exclude.st) || cal.after(exclude.et))) {
				
					//Logger.logln(">"+pe.getCalendar().getTime().toString());
					if(event.spot.contains(pe.getCellac())){
						if(inEvent==false) {
							//Logger.logln("The user enters the event!");
							inEvent = true;
						}
						first = (first == null || first.after(cal)) ? (Calendar)cal.clone() : first;
						last = (last == null || last.before(cal)) ? (Calendar)cal.clone() : last;
					}
					else if(inEvent) {
						//Logger.logln("The user walks away before the end!");
						inEvent = false;
					}
			}
			//if(pe.getCalendar().after(event.et)) break;
			//else  Logger.logln(pe.getCalendar().getTime().toString());
		}
		
		
		if(first == null) return 0;
		
		int iet = getAvgInterEventTime(plsEvents,event);
		//System.out.println("***** "+iet);
		
		first.add(Calendar.MINUTE, -iet);
		if(first.before(event.st)) first = event.st;
		last.add(Calendar.MINUTE, iet);
		if(last.after(event.et)) last = event.et;
		
		double ev_s = event.st.getTimeInMillis(); // event start
		double ev_e = event.et.getTimeInMillis(); // event end
		
		double ex_s = 0;
		double ex_e = 0;
		
		if(exclude!=null) {
			ex_s = exclude.st.getTimeInMillis(); // exclude start
			ex_e = exclude.et.getTimeInMillis(); // exclude end
		}
		
		
		double f = first.getTimeInMillis(); // first
		double l = last.getTimeInMillis(); // last
		double ev_lenght = ev_e - ev_s;
		double ex_lenght = ex_e - ex_s;
		double ot_lenght = l - f;
		double max = ev_lenght - ex_lenght;
		
		double fract = (f < ex_s && l > ex_e) ? (ot_lenght - ex_lenght) / max : ot_lenght / max;
		
		if(fract<=0) {
			System.err.println(event+"  "+exclude+"  "+first.getTime()+" - "+last.getTime());
			for(PLSEvent pe: plsEvents)
				System.err.println("\t"+pe);
		}
		
		return fract;
	}
	
	public static int getAvgInterEventTime(List<PLSEvent> plsEvents, CityEvent e) {
		
		Calendar startTime = e.st;
		Calendar endTime = e.et;
		int startH = startTime.get(Calendar.HOUR_OF_DAY);
		int endH = endTime.get(Calendar.HOUR_OF_DAY);
		
		
		DescriptiveStatistics ustats = new DescriptiveStatistics();
		for(int j=1;j<plsEvents.size();j++) {
			
			int d1 = plsEvents.get(j).getCalendar().get(Calendar.DAY_OF_YEAR);
			int d2 = plsEvents.get(j-1).getCalendar().get(Calendar.DAY_OF_YEAR);
			
			int h1 = plsEvents.get(j).getCalendar().get(Calendar.HOUR_OF_DAY);
			int h2 = plsEvents.get(j-1).getCalendar().get(Calendar.HOUR_OF_DAY);
			
			if(h1 < startH || h1 > endH) continue;
			if(h2 < startH || h2 > endH) continue;
			
			if(d1 != d2) continue;
			
			//if(plsEvents.get(j).getCalendar().before(e.st) || plsEvents.get(j).getCalendar().after(e.et)) continue;
			//if(plsEvents.get(j-1).getCalendar().before(e.st) || plsEvents.get(j-1).getCalendar().after(e.et)) continue;
				
			double dt = (1.0 * (plsEvents.get(j).getTimeStamp() - plsEvents.get(j-1).getTimeStamp())/60000);
			ustats.addValue(dt);
		}
		
		int iet = (int)ustats.getMean();
		if(iet == 0) iet = 100;
		return iet;
	}
	
	
	
}
