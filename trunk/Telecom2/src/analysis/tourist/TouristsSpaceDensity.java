package analysis.tourist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import analysis.densityANDflows.density.PopulationDensity;


/*
 * This class is used to map where the different user categories enter the region.
 * The idea is that tourists should enter near airports/ports/...
 */

public class TouristsSpaceDensity {
	
	public static void main(String[] args) throws Exception {
		Config.getInstance().pls_start_time = new GregorianCalendar(2014,Calendar.MARCH,1,0,0,0);
		Config.getInstance().pls_end_time = new GregorianCalendar(2014,Calendar.MARCH,31,23,59,59);
		//process(Config.getInstance().base_folder+"/Tourist/Venezia_March2014_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_ve_Venezia_cellXHour_March2014.csv",Config.getInstance().base_folder+"/RegionMap/VeneziaCenter.ser", false);
		process(Config.getInstance().base_folder+"/Tourist/Venezia_March2014_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_ve_Venezia_cellXHour_March2014.csv",Config.getInstance().base_folder+"/RegionMap/VeneziaProv.ser", true);
		
		//process(Config.getInstance().base_folder+"/Tourist/Firenze_March2014_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_fi_Firenze_cellXHour_March2014.csv",Config.getInstance().base_folder+"/RegionMap/FirenzeCenter.ser", false);
		process(Config.getInstance().base_folder+"/Tourist/Firenze_March2014_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_fi_Firenze_cellXHour_March2014.csv",Config.getInstance().base_folder+"/RegionMap/FirenzeProv.ser", true);
		
		
		Config.getInstance().pls_start_time = new GregorianCalendar(2013,Calendar.JULY,1,0,0,0);
		Config.getInstance().pls_end_time = new GregorianCalendar(2013,Calendar.JULY,31,23,59,59);
		//process(Config.getInstance().base_folder+"/Tourist/Venezia_July2013_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_ve_Venezia_cellXHour_July2013.csv",Config.getInstance().base_folder+"/RegionMap/VeneziaCenter.ser", false);
		process(Config.getInstance().base_folder+"/Tourist/Venezia_July2013_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_ve_Venezia_cellXHour_July2013.csv",Config.getInstance().base_folder+"/RegionMap/VeneziaProv.ser", true);
		
		//process(Config.getInstance().base_folder+"/Tourist/Firenze_July2013_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_fi_Firenze_cellXHour_July2013.csv",Config.getInstance().base_folder+"/RegionMap/FirenzeCenter.ser", false);
		process(Config.getInstance().base_folder+"/Tourist/Firenze_July2013_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_fi_Firenze_cellXHour_July2013.csv",Config.getInstance().base_folder+"/RegionMap/FirenzeProv.ser", true);
		
		
		Config.getInstance().pls_start_time = new GregorianCalendar(2014,Calendar.OCTOBER,1,0,0,0);
		Config.getInstance().pls_end_time = new GregorianCalendar(2014,Calendar.OCTOBER,31,23,59,59);
		//process(Config.getInstance().base_folder+"/Tourist/Torino_Oct2014_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_piem_Torino_cellXHour_Oct2014.csv",Config.getInstance().base_folder+"/RegionMap/TorinoCenter.ser", false);
		process(Config.getInstance().base_folder+"/Tourist/Torino_Oct2014_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_piem_Torino_cellXHour_Oct2014.csv",Config.getInstance().base_folder+"/RegionMap/TorinoProv.ser", true);
		
				
		Config.getInstance().pls_start_time = new GregorianCalendar(2014,Calendar.AUGUST,1,0,0,0);
		Config.getInstance().pls_end_time = new GregorianCalendar(2014,Calendar.AUGUST,31,23,59,59);
		//process(Config.getInstance().base_folder+"/Tourist/Lecce_Aug2014_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_pu_Lecce_cellXHour_Aug2014.csv",Config.getInstance().base_folder+"/RegionMap/LecceCenter.ser", false);
		process(Config.getInstance().base_folder+"/Tourist/Lecce_Aug2014_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_pu_Lecce_cellXHour_Aug2014.csv",Config.getInstance().base_folder+"/RegionMap/LecceProv.ser", true);	
		
		
		System.out.println("Done!");
	}
	
	
	public static void process(String classes_ser_file, String cellXHourFile, String rm_ser, boolean in_out_only) throws Exception {
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(rm_ser));
		Map<String,String> user_prof = (Map<String,String>)CopyAndSerializationUtils.restore(new File(classes_ser_file));
		BufferedReader br = new BufferedReader(new FileReader(new File(cellXHourFile)));
		PopulationDensity pd = new PopulationDensity();
		Map<String,Map<String,Double>> profile_space_density = computeSpaceDensity(new File(cellXHourFile), rm, user_prof, in_out_only);
		String name = new File(cellXHourFile).getName().replace(".csv", "");
		for(String profile: profile_space_density.keySet()) 
			pd.plotSpaceDensity(name+"_"+profile+(in_out_only ? "_inout" : ""), profile_space_density.get(profile), rm, 0);
	}
	
	
	private static final SimpleDateFormat F = new SimpleDateFormat("yyyy-MM-dd");
	// returns a map associating to each user profile a space-density map (i.e., a map associating to region names a density value)
	public static Map<String,Map<String,Double>> computeSpaceDensity(File cellXHourFile, RegionMap rm, Map<String,String> user_prof, boolean in_out_only) throws Exception {
				
		BufferedReader br = new BufferedReader(new FileReader(cellXHourFile));
		
		Map<String,Map<String,Double>> profile_space_density = new HashMap<String,Map<String,Double>>();
		for(String profile: GTExtractor.CLASSES.split(",")) {
			Map<String,Double> sd = new HashMap<String,Double>();
			for(RegionI r: rm.getRegions())
				sd.put(r.getName(), 0.0);
			profile_space_density.put(profile, sd);
		}
		
		int cont = 0;
		String line;
		while((line=br.readLine())!=null) {
			if(line.startsWith("//")) continue;
			
			cont++;
			
			String[] p = line.split(",");
			String user_id = p[0];
			String mnt = p[1];
			int num_pls = 0, num_days = 0, days_interval = 0;
			try {
				num_pls = Integer.parseInt(p[2]);
				num_days = Integer.parseInt(p[3]);
				days_interval = Integer.parseInt(p[4]);		
				
				for(int i=5;i<p.length;i++) {
					
					
					if(in_out_only && (i>5 && i<p.length-1)) continue; // if in_out_only is true, only first and last events are considered.
					
					
					String[] x = p[i].split(":");
					if(x.length!=4) continue;
					long celllac =Long.parseLong(x[3].trim());
					
					String prof = user_prof.get(user_id);
					
					if(prof == null) {
						System.out.println(prof);
						continue;
					}
					Map<String,Double> sd = profile_space_density.get(prof);
					float[] ai = rm.computeAreaIntersection(celllac,F.parse(x[0]).getTime());
					if(!Double.isNaN(ai[0]))
					for(int j=0; j<ai.length;j++) {
						String rname = rm.getRegion(j).getName();
						sd.put(rname,sd.get(rname)+ai[j]);
					}
				}	
			}catch(Exception e) {
				System.out.println("BAD LINE: "+line);
				continue;
			}		
		}
		br.close();
		
		return profile_space_density;
	}
	
}
