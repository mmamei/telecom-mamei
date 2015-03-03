package analysis.densityANDflows.density;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.gps.utils.LatLonPoint;
import org.gps.utils.LatLonUtils;

import region.CityEvent;
import region.CreatorRegionMapGrid;
import region.Placemark;
import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;
import visual.html.HeatMapGoogleMaps;
import visual.kml.KMLHeatMap;
import analysis.Constraints;
import analysis.PLSSpaceDensity;
import dataset.DataFactory;
import dataset.EventFilesFinderI;
import dataset.file.UserEventCounterCellacXHour;

public class PopulationDensity {
	
	
	public static void main(String[] args) throws Exception {
		/*
		PopulationDensity pd = new PopulationDensity();
		String city = "Torino";
		Map<String,String> constraints = null;
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/"+city+".ser"));
		File pls_space_density_file = new File(Config.getInstance().base_folder+"/PLSSpaceDensity/"+city+".txt");
		Map<String,Double> space_density = pd.computeSpaceDensity(pls_space_density_file,rm,constraints);
		pd.plotSpaceDensity(city+pd.getFileSuffix(constraints), space_density, rm,0);
		*/
		/*
		PopulationDensity pd = new PopulationDensity();
		String js = pd.runAll("2014-04-20", "00", "2014-04-20", "03", 7.6203,45.0945,7.6969,45.0774, "FIX_Piemonte.ser","");
		System.out.println(js);
		
		*/
		
		PopulationDensity pd = new PopulationDensity();
		
		CityEvent target_event = CityEvent.getEvent("Melpignano,22/08/2014");	
		tmp_file = target_event.toString()+"_STR";
		pd.runAll("2014-08-21", "00", "2014-08-22", "00", 16.7514,41.1621,18.6795,39.7368, "FIX_Puglia.ser","mnt=!222;users_event_probscores=C:/BASE/UsersAroundAnEvent/"+target_event.toFileName());
		tmp_file = target_event.toString()+"_ITA";
		pd.runAll("2014-08-21", "00", "2014-08-22", "00", 16.7514,41.1621,18.6795,39.7368, "FIX_Puglia.ser","mnt=222;users_event_probscores=C:/BASE/UsersAroundAnEvent/"+target_event.toFileName());
		
		
		target_event = CityEvent.getEvent("Lecce,14/08/2014");	
		tmp_file = target_event.toString()+"_STR";
		pd.runAll("2014-08-13", "00", "2014-08-14", "00", 16.7514,41.1621,18.6795,39.7368, "FIX_Puglia.ser","mnt=!222;users_event_probscores=C:/BASE/UsersAroundAnEvent/"+target_event.toFileName());
		tmp_file = target_event.toString()+"_ITA";
		pd.runAll("2014-08-13", "00", "2014-08-14", "00", 16.7514,41.1621,18.6795,39.7368, "FIX_Puglia.ser","mnt=222;users_event_probscores=C:/BASE/UsersAroundAnEvent/"+target_event.toFileName());
		
		
		target_event = CityEvent.getEvent("Lecce,24/08/2014");	
		tmp_file = target_event.toString()+"_STR";
		pd.runAll("2014-08-23", "00", "2014-08-24", "00", 16.7514,41.1621,18.6795,39.7368, "FIX_Puglia.ser","mnt=!222;users_event_probscores=C:/BASE/UsersAroundAnEvent/"+target_event.toFileName());
		tmp_file = target_event.toString()+"_ITA";
		pd.runAll("2014-08-23", "00", "2014-08-24", "00", 16.7514,41.1621,18.6795,39.7368, "FIX_Puglia.ser","mnt=222;users_event_probscores=C:/BASE/UsersAroundAnEvent/"+target_event.toFileName());
		
		
		
		
		//String js = pd.runAll("2014-08-21", "00", "2014-08-22", "00", 16.7514,41.1621,18.6795,39.7368, "FIX_Puglia.ser","mnt=!222;users_event_probscores=C:/BASE/UsersAroundAnEvent/"+target_event.toFileName());
		
		Logger.logln("Done!");
	}
	
	private static final SimpleDateFormat F = new SimpleDateFormat("yyyy-MM-dd-hh");
	
	public String runAll(String sday,String shour,String eday, String ehour, double lon1, double lat1, double lon2, double lat2, String regionMap, String sconstraints) {
		return runAll(sday,shour,eday,ehour,lon1,lat1,lon2,lat2,regionMap,new Constraints(sconstraints));
	}
	
	public static String tmp_file = "tmp";
	public String runAll(String sday,String shour,String eday, String ehour, double lon1, double lat1, double lon2, double lat2, String regionMap, Constraints constraints) {
		try {
			EventFilesFinderI eff = DataFactory.getEventFilesFinder();
			String dir = eff.find(sday,shour,eday,ehour,lon1,lat1,lon2,lat2);
			if(dir == null) return null;
			System.out.println(dir);
			Config.getInstance().pls_folder = Config.getInstance().pls_root_folder+"/"+dir;
			Config.getInstance().pls_start_time.setTime(F.parse(sday+"-"+shour));
			Config.getInstance().pls_end_time.setTime(F.parse(eday+"-"+ehour));
			Config.getInstance().pls_end_time.add(Calendar.HOUR_OF_DAY, 1); // add one to take into account minutes and seconds
			double lon = (lon1+lon2)/2;
			double lat = (lat1+lat2)/2;
			LatLonPoint p1 = new LatLonPoint(lat1,lon1);
			LatLonPoint p2 = new LatLonPoint(lat2,lon2);
			int r = (int)LatLonUtils.getHaversineDistance(p1, p2) / 2;
			Placemark p = new Placemark(tmp_file,new double[]{lat,lon},r);
			
			PopulationDensity pd = new PopulationDensity();
			
			// load the region map
			
			RegionMap rm = null;
			if(regionMap.startsWith("grid")) {
				if(lon1 != lon2 && lat1 != lat2) {
					double[][] lonlat_bbox = new double[][]{{lon1,lat1},{lon2,lat2}};
					int size = Integer.parseInt(regionMap.substring("grid".length()));
					rm = CreatorRegionMapGrid.process("grid", lonlat_bbox, size);
				}
			}
			else rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/"+regionMap));
			
			// Create UserEventCounterCellacXHour and PLSSpaceDenstiy files
			// Then compute the population density.
			
			UserEventCounterCellacXHour.process(p,false); // file name is called as the placemark
			Map<String,Double> space_density = pd.computeSpaceDensity(new File(Config.getInstance().base_folder+"/UserEventCounter/"+p.getName()+"_cellXHour.csv"),rm,constraints);
			
			
			// percent
			double tot = 0;
			for(double x: space_density.values())
				tot += x;
			for(String k: space_density.keySet())
				space_density.put(k, 100*space_density.get(k)/tot);
			
			
			plotSpaceDensity(p.getName(), space_density, rm,0);
			
			StringBuffer sb = new StringBuffer();
			sb.append("var heatMapData = [\n");
			for(String key: space_density.keySet()) {
				double[] latlon = rm.getRegion(key).getLatLon();
				sb.append("{location: new google.maps.LatLng("+latlon[0]+", "+latlon[1]+"), weight: "+space_density.get(key)+"},");
			}
			sb.append("];\n");
			
			
			return sb.toString();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	
	public void plotSpaceDensity(String city, Map<String,Double> space_density, RegionMap rm, double threshold) throws Exception {
		File d = new File(Config.getInstance().web_kml_folder);
		d.mkdirs();
		KMLHeatMap.drawHeatMap(d.getAbsolutePath()+"/"+city+"_"+rm.getName()+".kml",space_density,rm,"",true);
		HeatMapGoogleMaps.draw(d.getAbsolutePath()+"/"+city+"_"+rm.getName()+".html", city, space_density, rm, threshold);
	}
	
	private static final SimpleDateFormat F2 = new SimpleDateFormat("yyyy-MM-dd");
	public Map<String,Double> computeSpaceDensity(File cellXHourFile, RegionMap rm, Constraints constraints) throws Exception {
				
		BufferedReader br = new BufferedReader(new FileReader(cellXHourFile));
		
		Map<String,Double> sd = new HashMap<String,Double>();
		for(RegionI r: rm.getRegions())
			sd.put(r.getName(), 0.0);
		
		String line;
		while((line=br.readLine())!=null) {
			if(line.startsWith("//")) continue;
			
			String[] p = line.split(",");
			String user_id = p[0];
			String mnt = p[1];
			int num_pls = 0, num_days = 0, days_interval = 0;
			try {
				num_pls = Integer.parseInt(p[2]);
				num_days = Integer.parseInt(p[3]);
				days_interval = Integer.parseInt(p[4]);
	
				if(constraints.okConstraints(mnt,num_days)) {			
					for(int i=5;i<p.length;i++) {
						String[] x = p[i].split(":");
						if(x.length!=4) continue;
						long celllac =Long.parseLong(x[3].trim());
						float[] ai = rm.computeAreaIntersection(celllac,F2.parse(x[0]).getTime());
						if(!Double.isNaN(ai[0]))
						for(int j=0; j<ai.length;j++) {
							String rname = rm.getRegion(j).getName();
							double v = constraints.weight(user_id) * ai[j];	
							sd.put(rname,sd.get(rname)+v);
						}
					}
				}
			}catch(Exception e) {
				System.out.println("BAD LINE: "+line);
				continue;
			}		
		}
		br.close();
		
		return sd;
	}
}
