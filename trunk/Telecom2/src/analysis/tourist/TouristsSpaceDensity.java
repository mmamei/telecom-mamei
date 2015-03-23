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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import region.Placemark;
import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Sort;
import utils.kdtree.KDTree;
import visual.r.RPlotter;
import visual.text.TextPlotter;
import analysis.densityANDflows.density.PopulationDensity;


/*
 * This class is used to map where the different user categories enter the region.
 * The idea is that tourists should enter near airports/ports/...
 */

public class TouristsSpaceDensity {
	
	public static boolean LOG_PLOT = false;
	public static final Integer MAX = null;
	public static void main(String[] args) throws Exception {
		
		RPlotter.VIEW = false;
		
		Config.getInstance().pls_start_time = new GregorianCalendar(2013,Calendar.JULY,1,0,0,0);
		Config.getInstance().pls_end_time = new GregorianCalendar(2013,Calendar.JULY,31,23,59,59);
		process(Config.getInstance().base_folder+"/Tourist/Venezia_July2013_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_ve_Venezia_cellXHour_July2013.csv",Config.getInstance().base_folder+"/RegionMap/VeneziaRealCenter.ser", false);
		process(Config.getInstance().base_folder+"/Tourist/Venezia_July2013_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_ve_Venezia_cellXHour_July2013.csv",Config.getInstance().base_folder+"/RegionMap/VeneziaProv.ser", true, "Venezia");
		
		process(Config.getInstance().base_folder+"/Tourist/Firenze_July2013_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_fi_Firenze_cellXHour_July2013.csv",Config.getInstance().base_folder+"/RegionMap/FirenzeRealCenter.ser", false);
		process(Config.getInstance().base_folder+"/Tourist/Firenze_July2013_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_fi_Firenze_cellXHour_July2013.csv",Config.getInstance().base_folder+"/RegionMap/FirenzeProv.ser", true, "Firenze");
		
		
		Config.getInstance().pls_start_time = new GregorianCalendar(2014,Calendar.MARCH,1,0,0,0);
		Config.getInstance().pls_end_time = new GregorianCalendar(2014,Calendar.MARCH,31,23,59,59);
		process(Config.getInstance().base_folder+"/Tourist/Venezia_March2014_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_ve_Venezia_cellXHour_March2014.csv",Config.getInstance().base_folder+"/RegionMap/VeneziaRealCenter.ser", false);
		process(Config.getInstance().base_folder+"/Tourist/Venezia_March2014_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_ve_Venezia_cellXHour_March2014.csv",Config.getInstance().base_folder+"/RegionMap/VeneziaProv.ser", true, "Venezia");
		
		process(Config.getInstance().base_folder+"/Tourist/Firenze_March2014_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_fi_Firenze_cellXHour_March2014.csv",Config.getInstance().base_folder+"/RegionMap/FirenzeRealCenter.ser", false);
		process(Config.getInstance().base_folder+"/Tourist/Firenze_March2014_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_fi_Firenze_cellXHour_March2014.csv",Config.getInstance().base_folder+"/RegionMap/FirenzeProv.ser", true, "Firenze");
		
		
		
		Config.getInstance().pls_start_time = new GregorianCalendar(2014,Calendar.OCTOBER,1,0,0,0);
		Config.getInstance().pls_end_time = new GregorianCalendar(2014,Calendar.OCTOBER,31,23,59,59);
		process(Config.getInstance().base_folder+"/Tourist/Torino_Oct2014_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_piem_Torino_cellXHour_Oct2014.csv",Config.getInstance().base_folder+"/RegionMap/TorinoRealCenter.ser", false);
		process(Config.getInstance().base_folder+"/Tourist/Torino_Oct2014_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_piem_Torino_cellXHour_Oct2014.csv",Config.getInstance().base_folder+"/RegionMap/TorinoProv.ser", true, "Torino");
		
				
		Config.getInstance().pls_start_time = new GregorianCalendar(2014,Calendar.AUGUST,1,0,0,0);
		Config.getInstance().pls_end_time = new GregorianCalendar(2014,Calendar.AUGUST,31,23,59,59);
		process(Config.getInstance().base_folder+"/Tourist/Lecce_Aug2014_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_pu_Lecce_cellXHour_Aug2014.csv",Config.getInstance().base_folder+"/RegionMap/LecceRealCenter.ser", false);
		//process(Config.getInstance().base_folder+"/Tourist/Lecce_Aug2014_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_pu_Lecce_cellXHour_Aug2014.csv",Config.getInstance().base_folder+"/RegionMap/LecceProv.ser", true, "LecceBig");	
		process(Config.getInstance().base_folder+"/Tourist/Lecce_Aug2014_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_pu_Lecce_cellXHour_Aug2014.csv",Config.getInstance().base_folder+"/RegionMap/FIX_Puglia.ser", true, "LecceBigBig");	
		
		Config.getInstance().pls_start_time = new GregorianCalendar(2014,Calendar.SEPTEMBER,1,0,0,0);
		Config.getInstance().pls_end_time = new GregorianCalendar(2014,Calendar.SEPTEMBER,31,23,59,59);
		process(Config.getInstance().base_folder+"/Tourist/Lecce_Sep2014_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_pu_Lecce_cellXHour_Sep2014.csv",Config.getInstance().base_folder+"/RegionMap/LecceRealCenter.ser", false);
		process(Config.getInstance().base_folder+"/Tourist/Lecce_Sep2014_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_pu_Lecce_cellXHour_Sep2014.csv",Config.getInstance().base_folder+"/RegionMap/FIX_Puglia.ser", true, "LecceBigBig");	
		
		
		System.out.println("Done!");
	}
	
	public static void process(String classes_ser_file, String cellXHourFile, String rm_ser, boolean in_out_only) throws Exception {
		process(classes_ser_file,cellXHourFile,rm_ser,in_out_only,null);
	}
	private static int paragraph = 0;
	public static void process(String classes_ser_file, String cellXHourFile, String rm_ser, boolean in_out_only, String placemark) throws Exception {
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(rm_ser));
		Map<String,String> user_prof = (Map<String,String>)CopyAndSerializationUtils.restore(new File(classes_ser_file));
		//BufferedReader br = new BufferedReader(new FileReader(new File(cellXHourFile)));
		//PopulationDensity pd = new PopulationDensity();
		Placemark p = placemark == null ? null :  Placemark.getPlacemark(placemark);
		Map<String,Map<String,Double>> profile_space_density = computeSpaceDensity(new File(cellXHourFile), rm, user_prof, in_out_only, p, MAX);
		String name = new File(cellXHourFile).getName().replace(".csv", "");
		
		String dir = Config.getInstance().paper_folder+"/img/heatmap/"+name;
		dir = dir.replaceAll("_", "-");
		new File(dir).mkdirs();
		
		
		
		for(String profile: profile_space_density.keySet()) {
			Map<String,Double> density = profile_space_density.get(profile);
			//pd.plotSpaceDensity(name+"_"+profile+(in_out_only ? "_inout" : ""), density, rm, 0);
			RPlotter.dawHeatMap(dir+"/"+name+"_"+profile+(in_out_only ? "_inout" : "")+".pdf", density, rm, LOG_PLOT,profile+(in_out_only ? " (in/out)" : ""));
				
		}
		
		Map<String,List<String>> profile_descriptions =  getDescription(rm,profile_space_density,in_out_only);
		
		
		List<String> profiles_no_description = new ArrayList<String>();
		for(String prof: GTExtractor.PROFILES) 
			if(profile_descriptions.get(prof) == null) {
				profiles_no_description.add(prof);
			}
		
		//name = file-pls-pu-Lecce-cellXHour-Aug2014
				String[] x = name.split("_");
				String region = x[2];
				String city = x[3];
				String month = x[5];
				System.out.println(city+" "+month);
		Map<String,Object> tm = new HashMap<String,Object>();
		tm.put("paragraph", paragraph);
		tm.put("region", region);
		tm.put("city", city);
		tm.put("month", month);
		tm.put("inout",in_out_only);
		tm.put("profile_descriptions", profile_descriptions);
		tm.put("profiles_no_description",profiles_no_description);
		/*
		for(String profile: profile_descriptions.keySet())
			tm.put(profile+"Places", profile_descriptions.get(profile));
		*/
		TextPlotter.getInstance().run(tm, "src/analysis/tourist/TouristsSpaceDensity.ftl", Config.getInstance().paper_folder+"/img/heatmap/"+city+"-"+month+(in_out_only ? "-inout" : "")+".tex");
		paragraph ++;
	}
	
	private static KDTree kd = null;
	
	private static Map<String,List<String>> getDescription(RegionMap rm, Map<String,Map<String,Double>> profile_space_density, boolean in_out_only) {
		
		
		// load geonames data
		if(kd == null) {
			
			//http://www.geonames.org/export/codes.html
			Set<String> badClasses = new HashSet<String>();
			badClasses.add("A");badClasses.add("P");badClasses.add("U");badClasses.add("H");
			Set<String> badCodes = new HashSet<String>();
			badCodes.add("HTL");
			
			kd = new KDTree(2);
			try {
				BufferedReader br = new BufferedReader(new FileReader("G:/DATASET/GEO/geonames/IT.txt"));
				String line;
				while((line=br.readLine())!=null) {
					String[] x = line.split("\t");
					double[] lonlat = new double[]{Double.parseDouble(x[5]), Double.parseDouble(x[4])};
					String name = x[2];
					String f_class = x[6];
					String f_code = x[7];
					if(!badCodes.contains(f_code) && !badClasses.contains(f_class))
						kd.insert(lonlat, name);
				}
				br.close();
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		Map<String,List<Set<String>>> profile_topregions = new HashMap<String,List<Set<String>>>();
		Map<String,Integer> termscount = new HashMap<String,Integer>();
		for(String profile: profile_space_density.keySet()) {
			Map<String,Double> density = profile_space_density.get(profile);
			DescriptiveStatistics ds = new DescriptiveStatistics();
			for(double d: density.values()) {
				if(d > 0) ds.addValue(d);
			}
			double mean = ds.getMean();
			double sd = ds.getStandardDeviation();
			double skew = ds.getSkewness();
			
			List<Set<String>> topregions = new ArrayList<Set<String>>();
			for(String r: density.keySet()) {
				double z = (density.get(r) - mean) / sd;
				if(z > 3) {
					double[][] bboxLonLat = rm.getRegion(r).getBboxLonLat();
					Object[] places = kd.range(bboxLonLat[0], bboxLonLat[1]);
					Set<String> placeNames = new HashSet<String>();
					for(int i=0; i<places.length;i++) {
						String s = (String)places[i];
						placeNames.add(s);
						Integer c = termscount.get(s);
						if(c == null) c = 0;
						termscount.put(s,c+1);
					}
					topregions.add(placeNames);
				}
			}
			
			System.out.println(profile+": sd = "+sd+" skew = "+skew);	
			if((!in_out_only && sd > 3) || (in_out_only && sd > 1))
				profile_topregions.put(profile, topregions);
		}
		
		Map<String,Integer> o = Sort.sortHashMapByValuesD(termscount, Collections.reverseOrder());
		//for(String k:o.keySet())
		//	System.out.println("\t\t\t"+k+" ==> "+o.get(k));
		
		Map<String,List<String>> profile_descriptions = new HashMap<String,List<String>>();
		for(String profile: profile_topregions.keySet()) {
			List<String> desc = new ArrayList<String>();
			profile_descriptions.put(profile, desc);		
			for(Set<String> r: profile_topregions.get(profile)) {
				for(String k:o.keySet())
					if(r.contains(k)) {
						desc.add(k);
						break;
					}	
			}
		}
		
		/*
		for(String profile: profile_descriptions.keySet()) {
			System.out.print(profile+":");
			for(String k: profile_descriptions.get(profile))
				System.out.print(" "+k);
			System.out.println();
		}
		*/
		
		return profile_descriptions;
		
	}
	
	
	
	private static final SimpleDateFormat F = new SimpleDateFormat("yyyy-MM-dd");
	// returns a map associating to each user profile a space-density map (i.e., a map associating to region names a density value)
	public static Map<String,Map<String,Double>> computeSpaceDensity(File cellXHourFile, RegionMap rm, Map<String,String> user_prof, boolean in_out_only, Placemark exclude, Integer MAX) throws Exception {
		
		BufferedReader br = new BufferedReader(new FileReader(cellXHourFile));
		
		Map<String,Map<String,Double>> profile_space_density = new HashMap<String,Map<String,Double>>();
		
		for(String profile: GTExtractor.PROFILES) {
			Map<String,Double> sd = new HashMap<String,Double>();
			for(RegionI r: rm.getRegions())
				sd.put(r.getName(), 0.0);
			profile_space_density.put(profile, sd);
		}
		
		int cont = 0;
		String line;
		while((line=br.readLine())!=null) {
			if(line.startsWith("//")) continue;
			
			cont ++;
			if(MAX!=null && cont > MAX) break;
			
			String[] p = line.split(",");
			String user_id = p[0];
			String mnt = p[1];
			int num_pls = 0, num_days = 0, days_interval = 0;
			try {
				num_pls = Integer.parseInt(p[2]);
				num_days = Integer.parseInt(p[3]);
				days_interval = Integer.parseInt(p[4]);		
				String prof = user_prof.get(user_id);
				Map<String,Double> sd = profile_space_density.get(prof);
				if(sd == null) {
					if(prof!=null) {
						System.err.println(prof);
						System.err.println("Warning: there is a filter on computed profiles!");
					}
					continue;
				}
				
				float[] distrib = new float[rm.getNumRegions()];
				
				for(int i=5;i<p.length;i++) {
					
					
					if(in_out_only && (i>5 && i<p.length-1)) continue; // if in_out_only is true, only first and last events are considered.
					
					String[] x = p[i].split(":");
					
					
					//System.out.println("---> "+x[0]+" --> "+F.parse(x[0]));
					
					/*
					int h = Integer.parseInt(x[2]);
					Calendar cal = new GregorianCalendar();
					cal.setTime(F.parse(x[0]));
					cal.set(Calendar.HOUR_OF_DAY, h);
					*/
					
					if(x.length!=4) continue;
					long celllac =Long.parseLong(x[3].trim());
					
					if(exclude!=null && exclude.contains(celllac)) continue;
					
					
					
					if(prof == null) {
						//System.out.println(prof);
						continue;
					}
					
					
					
					float[] ai = rm.computeAreaIntersection(celllac,F.parse(x[0]).getTime());
					if(!Double.isNaN(ai[0])) {
						for(int j=0; j<distrib.length;j++)
							distrib[j] += ai[j];
					}
					
				}
				
				
				// normalize distrib so that it sums to 1
				float sum = 0;
				for(float x: distrib)
					sum+=x;
				if(sum != 0)
				for(int j=0; j<distrib.length;j++)
					distrib[j] = distrib[j] / sum;
				
				
				for(int j=0; j<distrib.length;j++) {
					String rname = rm.getRegion(j).getName();
					sd.put(rname,sd.get(rname)+distrib[j]);
				}
				
				
			}catch(Exception e) {
				System.out.println("BAD LINE: "+line);
				if(!(e instanceof NumberFormatException)) e.printStackTrace();
				//System.exit(0);
				continue;
			}		
		}
		br.close();
			
		return profile_space_density;
	}
	
}
