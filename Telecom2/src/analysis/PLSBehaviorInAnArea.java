package analysis;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.gps.utils.LatLonPoint;
import org.gps.utils.LatLonUtils;

import region.CityEvent;
import region.Placemark;
import utils.Config;
import utils.FileUtils;
import utils.Logger;
import utils.StatsUtils;
import visual.java.PLSPlotter;
import dataset.db.PLSEventsAroundAPlacemarkDB;
import dataset.file.PLSEventsAroundAPlacemark;

public class PLSBehaviorInAnArea {
	
	
	public static final boolean VERBOSE = false;
	
	
	private static final DecimalFormat DF = new DecimalFormat("#######.####");
	
	static String[] pnames = new String[]{
		//"Juventus Stadium (TO)",
		//"Stadio Olimpico (TO)",
		//"Stadio Silvio Piola (NO)", 
		//"Stadio San Siro (MI)",
		//"Stadio Atleti Azzurri d'Italia (BG)",
		//"Stadio Mario Rigamonti (BS)",
		//"Stadio Franco Ossola (VA)",
		//"Piazza San Carlo (TO)",
		//"Piazza Castello (TO)",
		//"Piazza Vittorio (TO)",
		"Parco Dora (TO)"
	};
	
	public static void main(String[] args) throws Exception { 
		/*
		Map<String,Double> bestRadius = PlacemarkRadiusExtractor.readBestR(true,true);	
		for(String pn: pnames) {
			Placemark p = Placemark.getPlacemark(pn);
			//double bestr = bestRadiusIE.get(pn);
			double bestr = -200;
			System.out.println("BEST RADIUS = "+bestr);
			p.changeRadius(bestr);
			
			PLSBehaviorInAnArea pbia = new PLSBehaviorInAnArea();
			pbia.process(p);
		}
		*/
		
		
		
		PLSBehaviorInAnArea pbia = new PLSBehaviorInAnArea();
		Object[] plsdata = pbia.process("2014-03-13","0","2014-03-15","10",12.3238,45.4425,12.3238,45.4425);
		if(plsdata!=null)
			System.out.println(pbia.getJSMap(plsdata));
		Logger.logln("Done!");
	}
	
	
	
	private static final SimpleDateFormat F = new SimpleDateFormat("yyyy-MM-dd-hh");
	public Object[] process(String sday,String shour,String eday, String ehour, double lon1, double lat1, double lon2, double lat2) {
		try {
			EventFilesFinder eff = new EventFilesFinder();
			String dir = eff.find(sday,shour,eday,ehour,lon1,lat1,lon2,lat2);
			if(dir == null) return null;
				
			Config.getInstance().pls_folder = FileUtils.getFile("DATASET/PLS/file_pls/"+dir).toString(); 
			Config.getInstance().pls_start_time.setTime(F.parse(sday+"-"+shour));
			Config.getInstance().pls_end_time.setTime(F.parse(eday+"-"+ehour));
			double lon = (lon1+lon2)/2;
			double lat = (lat1+lat2)/2;
			LatLonPoint p1 = new LatLonPoint(lat1,lon1);
			LatLonPoint p2 = new LatLonPoint(lat2,lon2);
			int r = (int)LatLonUtils.getHaversineDistance(p1, p2) / 2;
			String n = "tmp";
			Placemark p = new Placemark(n,new double[]{lat,lon},r);
			
			
			/*
			PLSEventsAroundAPlacemark.process(p);
			String file = "BASE/PLSEventsAroundAPlacemark/"+Config.getInstance().get_pls_subdir()+"/"+p.getName()+"_"+p.getRadius()+".txt";
			PLSMap plsmap = PLSEventsAroundAPlacemark.getPLSMap(file,null);
			(new File(file)).delete();
			*/
			PLSMap plsmap = PLSEventsAroundAPlacemarkDB.processdb(p);
			
			DescriptiveStatistics[] stats = getStats(plsmap);
			return new Object[]{plsmap.getDomain(),stats[1].getValues()}; // domain, user_stat		
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	public String getJSMap(Object[] plsdata) {
		String[] domain = (String[])plsdata[0];
		double[] data = (double[])plsdata[1];
		StringBuffer sb = new StringBuffer();
		sb.append("var data = google.visualization.arrayToDataTable([['Day', 'PLS']");
		for(int i=0; i<domain.length;i++) {
			sb.append(",['"+domain[i].substring(1,domain[i].length()-1)+"',  "+data[i]+"]");
		}
		sb.append("]);");
		return sb.toString();
	}
	

	public void process(Placemark p) throws Exception {
		
		Logger.logln("Processing... "+p.getName());
		
		String subdir = Config.getInstance().get_pls_subdir();
		
		String file = "BASE/PLSEventsAroundAPlacemark/"+subdir+"/"+p.getName()+"_"+p.getRadius()+".txt";
		File f = new File(file);
		if(!f.exists()) {
			Logger.logln(file+" does not exist");
			Logger.logln("Executing PLSEventsAroundAPlacemark.process()");
			PLSEventsAroundAPlacemark.process(p);
		}
		
		
		// get the city events to be considered to find relevant cells for this placemark
		Collection<CityEvent> events = CityEvent.getEventsInData();
		List<CityEvent> relevantEvents = new ArrayList<CityEvent>();
		for(CityEvent e : events) 
			if(e.spot.getName().equals(p.getName())) relevantEvents.add(e);
		
		for(CityEvent re : relevantEvents)
			System.out.println("- "+re.toFileName());
		
		PLSMap plsmap = PLSEventsAroundAPlacemark.getPLSMap(file,null);
		
		DescriptiveStatistics[] stats = getStats(plsmap);
				
		// compute data
		//double[] pls_data = stats[0].getValues();
		double[] usr_data = stats[1].getValues();
		//double[] z_pls_data = getZ(stats[0],plsmap.startTime);
		double[] z_usr_data =  StatsUtils.getZH(stats[1],plsmap.startTime);
			
		//StatsUtils.checkNormalDistrib(z_pls_data,true,p.name+" hourly z");
		//StatsUtils.checkNormalDistrib(getZ3(stats[0]),true,p.name+" val z");
			
		if(VERBOSE) {
			PrintWriter out = new PrintWriter(new FileWriter("BASE/PLSBehaviorInAnArea/"+p.getName()+"_"+p.getRadius()+".csv"));
			out.println("time,n_user,z_score");
			for(int i=0; i<plsmap.getDomain().length;i++) {
				out.println(plsmap.getDomain()[i]+";"+(int)usr_data[i]+";"+DF.format(z_usr_data[i]));
			}
				
			out.close();
		}
		PLSPlotter.drawGraph(p.getName()+"_"+p.getRadius(),plsmap.getDomain(),null,usr_data,null,z_usr_data,plsmap,relevantEvents);
	}
	
	
	public static DescriptiveStatistics[] getStats(PLSMap plsmap) {
		DescriptiveStatistics pls_stats = new DescriptiveStatistics();
		DescriptiveStatistics usr_stats = new DescriptiveStatistics();
		
		Calendar cal = (Calendar)plsmap.startTime.clone();
		while(!cal.after(plsmap.endTime)) {
			String key = PLSEventsAroundAPlacemark.getKey(cal);
			Integer pls_count = plsmap.pls_counter.get(key);
			double pls = pls_count == null ? 0 : (double)pls_count;
			double usr = plsmap.usr_counter.get(key) == null ? 0 : plsmap.usr_counter.get(key).size();	
			pls_stats.addValue(pls);
			usr_stats.addValue(usr);
			cal.add(Calendar.HOUR, 1);
		}
		return new DescriptiveStatistics[]{pls_stats,usr_stats};
	}
	
	
	
	
	
	
	
}