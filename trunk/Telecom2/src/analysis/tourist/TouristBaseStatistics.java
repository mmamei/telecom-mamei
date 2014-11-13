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
import analysis.PLSEvent;
import analysis.RadiusOfGyration;


public class TouristBaseStatistics {
	
	public String user_id;
	
	// feature vector
	public String mnt;
	public int num_pls;
	public int num_days;
	public int num_days_in_area;
	public int days_interval;
	public int max_h_interval;
	
	

	/* String events is in the form:
	 * user_id, mnt, num_pls, num,_days, 2013-5-23:Sun:cellac,....
	 * EXAMPLE:
	 * 1b44888ff4f,22201,3,1,2013-5-23:Sun:13:4018542484,2013-5-23:Sun:17:4018542495,2013-5-23:Sun:13:4018542391,
	*/
	
	private static final SimpleDateFormat F = new SimpleDateFormat("yyyy-MM-dd");
	
	public TouristBaseStatistics(String events, RegionMap map) throws Exception {
		
		
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
		
		
		stat_pls_per_day.addValue(num_pls/num_days);
		//stat_radius_of_gyration.addValue(RadiusOfGyration.computeGyrationRadius(pe));
		stat_num_days_in_area.addValue(num_days_in_area);
		mnt = mnt.substring(0,3);
		Integer c = stat_mnt.get(mnt);
		if(c == null) c = 0;
		stat_mnt.put(mnt, c+1);
	}
	

	
	static DescriptiveStatistics stat_pls_per_day = new DescriptiveStatistics();
	static DescriptiveStatistics stat_radius_of_gyration = new DescriptiveStatistics();
	static DescriptiveStatistics stat_num_days_in_area = new DescriptiveStatistics();
	static Map<String,Integer> stat_mnt = new HashMap<String,Integer>();
	
	
	private static Placemark placemark;
	public static void main(String[] args) throws Exception {
	
		/*
		String city = "Torino";
		String cellXHourFile = Config.getInstance().base_folder+"/UserEventCounter/Torino_cellXHour.csv";
		String gt_ser_file = "Firenze_gt_profiles.ser";
		*/
		
		String city = "Venezia";
		placemark = Placemark.getPlacemark(city);
		String cellXHourFile =Config.getInstance().base_folder+"/UserEventCounter/file_pls_ve_"+ city+"_cellXHour.csv";
		String gt_ser_file = Config.getInstance().base_folder+"/Tourist/"+city+"_gt_profiles.ser";
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/"+city+".ser"));
		process(rm,cellXHourFile,gt_ser_file,null);
		
		
		// print statistics
		
		int tot = 0;
		for(int c: stat_mnt.values())
			tot+=c;
		
		System.out.println("tot users = "+tot);
		
		System.out.print("stat_pls_per_day");
		for(int i=1;i<=100;i++) 
			System.out.print(","+stat_pls_per_day.getPercentile(i));
		System.out.println();
		
		System.out.print("stat_radius_of_gyration");
		for(int i=1;i<=100;i++) 
			System.out.print(","+stat_radius_of_gyration.getPercentile(i));
		System.out.println();
		
		System.out.print("stat_num_days_in_area");
		for(int i=1;i<=100;i++) 
			System.out.print(","+stat_num_days_in_area.getPercentile(i));
		System.out.println();
		
			
		Map<String,String> mncT = mncT();
		LinkedHashMap<String, Integer> o_stat_mnt = Sort.sortHashMapByValuesD(stat_mnt,Collections.reverseOrder());
		System.out.println("stat_mnt");
		for(String mnt: o_stat_mnt.keySet()) {
			double p = 1.0 * o_stat_mnt.get(mnt) / tot;
			if(p > 0.01)
				System.out.println(mncT.get(mnt)+","+p);
		}

		
		Logger.logln("Done");
	}
	

	
	public static void process(RegionMap rm, String cellXHourFile, String gt_ser_file, Integer max) throws Exception {
	
		BufferedReader br = new BufferedReader(new FileReader(new File(cellXHourFile)));
		
		String placemark_name = cellXHourFile.substring(cellXHourFile.lastIndexOf("/")+1,cellXHourFile.lastIndexOf("_cellXHour.csv"));
		
		
		Map<String,String> user_gt_prof = null;
		if(gt_ser_file != null)
			user_gt_prof = (Map<String,String>)CopyAndSerializationUtils.restore(new File(gt_ser_file));
		
		String s = max == null ? "" : "_"+max;
	
		
		int i=0;
		String line;
		TouristBaseStatistics td;
		while((line=br.readLine())!=null) {
			if(line.startsWith("//")) continue;
			if(max != null && i > max) break;
			
			try {
				td = new TouristBaseStatistics(line,rm);
			} catch(Exception e) {
				System.err.println(line);
				continue;
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
	
}
