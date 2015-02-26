package analysis.tourist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import region.Placemark;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;
import utils.Sort;
import visual.r.RPlotter;
import analysis.PLSEvent;
import analysis.RadiusOfGyration;


public class TouristBaseStatistics {
	
	private String user_id;
	
	// feature vector
	private String mnt;
	private int num_pls;
	private int num_days;
	private int num_days_in_area;
	private int days_interval;
	private int max_h_interval;
	
	

	/* String events is in the form:
	 * user_id, mnt, num_pls, num,_days, 2013-5-23:Sun:cellac,....
	 * EXAMPLE:
	 * 1b44888ff4f,22201,3,1,2013-5-23:Sun:13:4018542484,2013-5-23:Sun:17:4018542495,2013-5-23:Sun:13:4018542391,
	*/
	
	private static final SimpleDateFormat F = new SimpleDateFormat("yyyy-MM-dd");
	
	private TouristBaseStatistics(String events, RegionMap map, Placemark placemark, boolean COMPUTE_RADIUS_OF_GYRATION) throws Exception {
		
		
		String[] p = events.split(",");
		user_id = p[0];
		mnt = p[1];
		num_pls = Integer.parseInt(p[2]);
		num_days = Integer.parseInt(p[3]);
		days_interval = Integer.parseInt(p[4]);
		
		Set<String> days_in_area = new HashSet<String>();
		
		
		// the following three variables are used to compute the maximum time interval in which the user is inside the city 
		// this is useful to classify user in transit (their maximum time interval must be small)
		boolean inarea = false; // boolean variable to see if the user is currently in the city
		long prevTime = -1; // the last time the user entered the city
		int max_dh = -1; // the maximum time interval (in hours) in which the user has been in the city
		
		List<PLSEvent> pe = new ArrayList<PLSEvent>();
		
		for(int i=5;i<p.length;i++) {
			try {
				// 2013-5-23:Sun:13:4018542484
				String[] x = p[i].split(":");
				
				//System.out.println("---> "+x[0]+" --> "+F.parse(x[0]));
				
				
				int h = Integer.parseInt(x[2]);
				long celllac =Long.parseLong(x[3].trim());
				
				Calendar cal = new GregorianCalendar();
				cal.setTime(F.parse(x[0]));
				cal.set(Calendar.HOUR_OF_DAY, h);
				long time = cal.getTimeInMillis();
				
				pe.add(new PLSEvent(user_id,mnt,String.valueOf(celllac),String.valueOf(time)));
				
				if(placemark.contains(celllac)) {
					days_in_area.add(x[0]);
					if(!inarea) { // before I was out
						prevTime = time;		
					}
					else { // before I was in
						int dh = (int)((time - prevTime)/(3600 * 1000));
						if(max_dh == -1 || dh > max_dh) max_dh = dh;
					}
					inarea = true;
				}
				else {
					// event outside the city area
					inarea = false;
					prevTime = time;
				}
			} catch(Exception e) {
				System.out.println("Problems with "+p[i]);
				e.printStackTrace();
			}
		}
		if(max_dh == -1) max_dh = 1000;
		max_h_interval = max_dh;
		num_days_in_area = days_in_area.size();
		
		
		stat_pls_per_day.addValue(1.0*num_pls/num_days);
		stat_radius_of_gyration.addValue(COMPUTE_RADIUS_OF_GYRATION ? RadiusOfGyration.computeGyrationRadius(pe) : 0.0);
		stat_num_days_in_area.addValue(num_days_in_area);
		mnt = mnt.substring(0,3);
		Integer c = stat_mnt.get(mnt);
		if(c == null) c = 0;
		stat_mnt.put(mnt, c+1);
	}
	

	
	
	private static DescriptiveStatistics stat_pls_per_day = new DescriptiveStatistics();
	private static DescriptiveStatistics stat_radius_of_gyration = new DescriptiveStatistics();
	private static DescriptiveStatistics stat_num_days_in_area = new DescriptiveStatistics();
	private static Map<String,Integer> stat_mnt = new HashMap<String,Integer>();
	
	
	public static void runProcess(String pre, String city, String month, String classes_ser_file, boolean COMPUTE_RADIUS_OF_GYRATION) throws Exception {
		/*
		String city = "Torino";
		String cellXHourFile = Config.getInstance().base_folder+"/UserEventCounter/Torino_cellXHour.csv";
		String gt_ser_file = "Firenze_gt_profiles.ser";
		*/
		
		
		Placemark placemark = Placemark.getPlacemark(city);
		String cellXHourFile =Config.getInstance().base_folder+"/UserEventCounter/"+pre+ city+"_cellXHour_"+month+".csv";
		
		
		
		
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/"+city+".ser"));
		process(rm,cellXHourFile,classes_ser_file,null,placemark,COMPUTE_RADIUS_OF_GYRATION);
		
		
		double[] p = new double[100];
		double[] pls_per_day = new double[100];
		double[] radius_of_gyration = new double[100];
		double[] num_days_in_area = new double[100];
		
		for(int i=1;i<=100;i++) {
			p[i-1] = (double)i/100;
			pls_per_day[i-1] = stat_pls_per_day.getPercentile(i);
			radius_of_gyration[i-1] = stat_radius_of_gyration.getPercentile(i);
			num_days_in_area[i-1] = stat_num_days_in_area.getPercentile(i);
		}
		
		int tot = 0;
		for(int c: stat_mnt.values())
			tot+=c;
		
		Map<String,String> mncT = mncT();
		LinkedHashMap<String, Integer> o_stat_mnt = Sort.sortHashMapByValuesD(stat_mnt,Collections.reverseOrder());
		
		String[] countries = new String[11];
		double[] prob = new double[countries.length];
		double otherp = 100;
		int i=0;
		for(String mnt: o_stat_mnt.keySet()) { 
			countries[i] = mncT.get(mnt);
			prob[i] = (int)(100.0 * o_stat_mnt.get(mnt) / tot);
			otherp -= prob[i];
			i++;
			if(i>= countries.length-1) break;
		}
		countries[countries.length-1] = "Other";
		prob[prob.length-1] = otherp;
		
		String suffix = "_"+city+"_"+month;
		suffix = classes_ser_file != null ? suffix+"_tourists" : suffix;
		RPlotter.drawScatter(pls_per_day, p, "pls per day", "cdf", Config.getInstance().base_folder+"/Images/pls_per_day"+suffix+".pdf", "geom_line()");	
		RPlotter.drawScatter(radius_of_gyration, p, "radius of gyration", "cdf", Config.getInstance().base_folder+"/Images/radius_of_gyration"+suffix+".pdf", "geom_line()");	   
		RPlotter.drawScatter(num_days_in_area, p, "num days in area", "cdf", Config.getInstance().base_folder+"/Images/num_days_in_area"+suffix+".pdf", "geom_line()");	
		RPlotter.drawBar(countries, prob, "countries", "%", Config.getInstance().base_folder+"/Images/stat_mnt"+suffix+".pdf", null);
		
	}
	
	
	public static void runCorssMonthAnalysis(String cellXHourFile, String gt_ser_file) throws Exception {
		
		Map<String,Double> profilesCrossProb = corssMonthAnalysis(gt_ser_file,cellXHourFile);
		String[] profiles = new String[profilesCrossProb.size()];
		double[] corssProb = new double[profiles.length];
		int i=0;
		for(String profile: profilesCrossProb.keySet()) {
			profiles[i] = profile;
			corssProb[i] = profilesCrossProb.get(profile);
			i++;
		}
		
		String fname = gt_ser_file.substring(gt_ser_file.lastIndexOf("/")+1,gt_ser_file.lastIndexOf(".ser"))+"_"+cellXHourFile.substring(cellXHourFile.lastIndexOf("/")+1,cellXHourFile.lastIndexOf(".csv"));

		RPlotter.drawBar(profiles, corssProb, "profiles", "cross %", Config.getInstance().base_folder+"/Images/cross_month_"+fname+".pdf", null);
	}
	

	
	
	
	public static void process(RegionMap rm, String cellXHourFile, String classes_ser_file, Integer max,Placemark placemark,boolean COMPUTE_RADIUS_OF_GYRATION) throws Exception {
	
		BufferedReader br = new BufferedReader(new FileReader(new File(cellXHourFile)));
		
		Map<String,String> user_prof = null;
		if(classes_ser_file != null)
			user_prof = (Map<String,String>)CopyAndSerializationUtils.restore(new File(classes_ser_file));
		
		int i=0;
		String line;
		while((line=br.readLine())!=null) {
			if(line.startsWith("//")) continue;
			if(max != null && i > max) break;
			
			boolean ok = true;
			if(user_prof != null) {
				String profile = user_prof.get(line.split(",")[0]);
				if(profile == null) ok = false;
				else ok = profile.equals("Tourist") || profile.equals("Excursionist");
			}
			
			if(ok) {
			try {
				new TouristBaseStatistics(line,rm,placemark,COMPUTE_RADIUS_OF_GYRATION);
			} catch(Exception e) {
				System.err.println(line);
				continue;
			}
			}
			
			
			i++;
			if(i % 10000 == 0) {
				Logger.logln("Processed "+i+"th users...");
			}
		}
		br.close();
	}
	
	private static Map<String,String> mncT() throws Exception {
		Map<String,String> mncT = new HashMap<String,String>();
		
		BufferedReader br = new BufferedReader(new FileReader("G:/DATASET/PLS/MCC-MNC.csv"));
		String line;
		while((line=br.readLine()) != null) {
			//country;operator;mcc;mnc;operatorid
			String[] e = line.split(";");
			mncT.put(e[2], e[0]);
		}
		br.close();
		
		return mncT;
	}
	
	/*
	 * This method counts how many users of the gt_ser_file can be found in the cellXHourFile.
	 * It is useful as a cross month measure. Tourists in the GT file of one month should not appear in the cellXHourFile of another month.
	 * Vice versa Residents in the GT file of one month should appear in the cellXHourFile of another month.
	 * 
	 * It is also interesting the possiblity of looking for people in two area at the same time (same month). They are for example tourists visiting both Venice and Florence
	 * 
	 * The method returns a map associating for each profile the fraction of users in cellXHourFile that are also in gt_ser_file
	 */
	
	public static Map<String,Double> corssMonthAnalysis(String classes_ser_file, String cellXHourFile) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(new File(cellXHourFile)));
		
		Map<String,String> user_prof = (Map<String,String>)CopyAndSerializationUtils.restore(new File(classes_ser_file));
		
		Map<String,Double> denominator = new HashMap<String,Double>();
		for(String profile: user_prof.values()) {
			Double d = denominator.get(profile);
			if (d == null) d = 0.0;
			denominator.put(profile, d+1);
		}
		
		Map<String,Double> numerator = new HashMap<String,Double>();
		for(String profile: denominator.keySet()) 
			numerator.put(profile, 0.0);
		
		
		String line;
		while((line=br.readLine())!=null) {
			if(line.startsWith("//")) continue;
			String user = line.split(",")[0];
			String profile = user_prof.get(user);
			if(profile!=null) numerator.put(profile, numerator.get(profile)+1);
		}
		br.close();
		
		
		for(String profile: denominator.keySet())
			numerator.put(profile, numerator.get(profile) / denominator.get(profile));
		
		return numerator;
	}
	
	
	public static void main(String[] args) throws Exception {		
		//runProcess("file_pls_ve_","Venezia","July2013",Config.getInstance().base_folder+"/Tourist/Venezia_July2013_classes.ser",false);
		runCorssMonthAnalysis(Config.getInstance().base_folder+"/UserEventCounter/file_pls_fi_Firenze_cellXHour_July2013.csv", Config.getInstance().base_folder+"/Tourist/Venezia_July2013_classes.ser");
		System.out.println("Done!");	
	}
}
