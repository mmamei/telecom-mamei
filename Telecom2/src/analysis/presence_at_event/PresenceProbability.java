package analysis.presence_at_event;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import pls_parser.UsersCSVCreator;
import utils.Config;
import utils.Logger;
import visual.GraphPlotter;
import visual.KMLPath;
import analysis.PlsEvent;
import area.CityEvent;

public class PresenceProbability {
	

	public static void main(String[] args) throws Exception {
		CityEvent event = CityEvent.getEvent("Stadio Silvio Piola (NO),11/03/2012");
		process(event);
		Logger.logln("Done");
	}
		
	public static void process(CityEvent event) throws Exception {	
		
		String inputdir = Config.getInstance().base_dir +"/UsersCSVCreator/"+ event.toString();
		if(!new File(inputdir).exists()) {
			Logger.logln(inputdir+" Does not exist!");
			Logger.logln("Running UsersCSVCreator.create()");
			UsersCSVCreator.create(event);
		}
		else {
			Logger.logln(inputdir+" already exists!");
		}
		
		
		File[] files = new File(inputdir).listFiles();
		
		String outputdir = Config.getInstance().base_dir +"/PresenceProbability/"+ event.toString();
		try {
			FileUtils.deleteDirectory(new File(outputdir));
		} catch (IOException e) {
			e.printStackTrace();
		}
		boolean ok = new File(outputdir).mkdirs();
		if(ok)
			Logger.logln("Create directory: "+outputdir);
		else {
			Logger.logln("Failed to create directory: "+outputdir);
			System.exit(0);
		}
		
		KMLPath.openFile(outputdir+"/user_traces.kml");
		DescriptiveStatistics stats = new DescriptiveStatistics();
	
		for(int i=0; i<files.length; i++) {
			File f = files[i];	
			if(!f.isFile()) continue;
			String filename = f.getName();
			String username = filename.substring(0, filename.indexOf(".csv"));
			List<PlsEvent> plsEvents = PlsEvent.readEvents(f);
			double p = presenceProbability(username,plsEvents,event);			
			
			printDebugEvents(outputdir,filename,plsEvents,event);
			stats.addValue(p);
			if(p > 0.8) KMLPath.print(username, plsEvents);
		}
		int n = 10;
		String[] domain = new String[n];
		for(int i=0; i<n;i++) 
			domain[i] = (i*100/n)+"-"+((i+1)*100/n)+"%";
			
		double[] data = hist(stats.getSortedValues(),n);
		GraphPlotter.drawGraph("prob", "prob", "", "prob", "n users", domain, data);
		KMLPath.closeFile();
	}
	
	private static double[] hist(double[] vals, int n) {
		double[] h = new double[n];
		for(double v: vals) {
			h[(int)Math.floor((v*n))]++;
		}
		return h;
	}
	
	
	
	private static void printDebugEvents(String dir, String filename, List<PlsEvent> plsEvents, CityEvent event) throws Exception {
		File d = new File(dir+"/DEBUG");
		if(!d.exists()) {
			boolean ok = d.mkdirs();
			if(ok) Logger.logln("Creating: "+d);
			else Logger.logln("Failed to Create: "+d);
		}
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(dir+"/DEBUG/"+filename)));
		for(PlsEvent pe: plsEvents) {	
			out.print(pe.getTime()+","+pe.getCellac());
			if(pe.getCalendar().after(event.st) && pe.getCalendar().before(event.et)) 
				out.print(",in time");
			if(event.spot.contains(pe.getCellac()))
				out.print(",in space");
			out.println();
		}
		out.close();
	}
	
	
	private static final DecimalFormat DF = new DecimalFormat("#.##",new DecimalFormatSymbols(Locale.US));
	
	public static double presenceProbability(String username, List<PlsEvent> plsEvents, CityEvent event) {
		double f1 = fractionOfTimeInWhichTheUserWasAtTheEvent(plsEvents,event,false);
		//Logger.logln("Radius = "+event.spot.radius);
		double f2 = fractionOfTimeInWhichTheUserIsUsuallyInTheEventArea(plsEvents,event,1000,false);
		//Logger.logln(username+" = "+f1+", "+f2);
		return f1 * (1-f2);
	}
	
	
	
	/*
	 * This just returns 1 if the user was present during the event, but he was never present otherwise.
	 * It returns 0 if the user was also present at other times
	 */
	public static double presenceProbabilityTest(String username, List<PlsEvent> plsEvents, CityEvent event) {	
		for(PlsEvent pe: plsEvents) {
			if((pe.getCalendar().before(event.st) || pe.getCalendar().after(event.et)) && event.spot.contains(pe.getCellac()))
				return 0;
		}
		
		return 1;
		
	}
	
	
	public static double fractionOfTimeInWhichTheUserWasAtTheEvent(List<PlsEvent> plsEvents, CityEvent event, boolean verbose) {
		Calendar first = null;
		Calendar last = null;
		boolean inEvent = false;
		for(PlsEvent pe: plsEvents) {	
			
			//if(verbose) System.err.println("-"+event);
			//if(verbose) System.err.println("-"+pe);
			
			if(event.st.before(pe.getCalendar()) && event.et.after(pe.getCalendar())) {
				//Logger.logln(">"+pe.getCalendar().getTime().toString());
				if(event.spot.contains(pe.getCellac())){
					if(inEvent==false) {
						//Logger.logln("The user enters the event!");
						inEvent = true;
					}
					first = (first == null || first.after(pe.getCalendar())) ? pe.getCalendar() : first;
					last = (last == null || last.before(pe.getCalendar())) ? pe.getCalendar() : last;
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
		
		first.add(Calendar.MINUTE, -10);
		if(first.before(event.st)) first = event.st;
		last.add(Calendar.MINUTE, 10);
		if(last.after(event.et)) last = event.et;
		return 1.0 * (last.getTimeInMillis() - first.getTimeInMillis()) / (event.et.getTimeInMillis() - event.st.getTimeInMillis());
	}
	
	
	
	public static double fractionOfTimeInWhichTheUserWasAtTheEvent(List<PlsEvent> plsEvents, CityEvent event, CityEvent exclude, boolean verbose) {
		Calendar first = null;
		Calendar last = null;
		boolean inEvent = false;
		
		if(verbose) { 
			System.err.println("EVENT = "+event);
			System.err.println("EXCLUDE = "+exclude);
		}
		
		for(PlsEvent pe: plsEvents) {	
			
			Calendar cal = pe.getCalendar();
			
			if(event.st.before(cal) && event.et.after(cal))
			if(verbose) System.err.println("-"+pe);
			
			if(event.st.before(cal) && event.et.after(cal) && (cal.before(exclude.st) || cal.after(exclude.et))) {
				
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
		
		first.add(Calendar.MINUTE, -10);
		if(first.before(event.st)) first = event.st;
		last.add(Calendar.MINUTE, 10);
		if(last.after(event.et)) last = event.et;
		
		double ev_s = event.st.getTimeInMillis(); // event start
		double ev_e = event.et.getTimeInMillis(); // event end
		double ex_s = exclude.st.getTimeInMillis(); // exclude start
		double ex_e = exclude.et.getTimeInMillis(); // exclude end
		double f = first.getTimeInMillis(); // first
		double l = last.getTimeInMillis(); // last
		double ev_lenght = ev_e - ev_s;
		double ex_lenght = ex_e - ex_s;
		double ot_lenght = l - f;
		double max = ev_lenght - ex_lenght;
		
		double f2 = (f < ex_s && l > ex_e) ? (ot_lenght - ex_lenght) / max : ot_lenght / max;
		
		if(f2<=0) {
			System.err.println(event);
			System.err.println(exclude);
			System.err.println(first.getTime()+" - "+last.getTime());
		}
		
		return f2;
	}
	
	
	
	public static double fractionOfTimeInWhichTheUserIsUsuallyInTheEventArea(List<PlsEvent> plsEvents, CityEvent event, int days, boolean verbose) {
		
		Map<String,List<PlsEvent>> eventsPerDay = new TreeMap<String,List<PlsEvent>>();
				
		for(int d=-days; d<=days; d++) {
			if(d == 0) continue; // do not consider the day of the event
			Calendar cal = (Calendar)event.st.clone();
			cal.add(Calendar.DAY_OF_MONTH, d);
			eventsPerDay.put(getKey(cal), new ArrayList<PlsEvent>());
		}
		
		
		for(PlsEvent pe: plsEvents) {	
			List<PlsEvent> de = eventsPerDay.get(getKey(pe.getCalendar()));
			if(de != null) 
				de.add(pe);
		}
		
		double f = 0;
		
		int count = 0;
		for(String k: eventsPerDay.keySet()) {
			String[] dmy = k.split("-");
			int day = Integer.parseInt(dmy[0]);
			int month = Integer.parseInt(dmy[1]);
			int year = Integer.parseInt(dmy[2]);
			
			//if(verbose) System.err.println(k+" = "+eventsPerDay.get(k).size());
			
			CityEvent ce = event.changeDay(day, month, year);
			
			if(count > 0) {
				ce.st.set(Calendar.HOUR_OF_DAY, 0);
				ce.st.set(Calendar.MINUTE, 0);
				ce.st.set(Calendar.SECOND, 0);
			}
			if(count < eventsPerDay.size() - 1) {
				ce.et.set(Calendar.HOUR_OF_DAY, 23);
				ce.et.set(Calendar.MINUTE, 59);
				ce.et.set(Calendar.SECOND, 59);
			}
			
			if(eventsPerDay.get(k)!=null && eventsPerDay.get(k).size() > 0) {
				double frac = fractionOfTimeInWhichTheUserWasAtTheEvent(eventsPerDay.get(k),ce,verbose);
				if(verbose) System.err.println(k+", frac = "+frac+", size = "+eventsPerDay.get(k).size()+" ... "+ce);
				f += frac;
			}
			count ++;
		}
		
		return f / eventsPerDay.size();
	}
	
	public static String getKey(Calendar cal) {
		return cal.get(Calendar.DAY_OF_MONTH)+"-"+cal.get(Calendar.MONTH)+"-"+cal.get(Calendar.YEAR);
	}
	
}
