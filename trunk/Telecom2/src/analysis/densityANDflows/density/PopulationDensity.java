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

import region.CreatorRegionMapGrid;
import region.Placemark;
import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;
import visual.html.HeatMapGoogleMaps;
import visual.kml.KMLHeatMap;
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
		PopulationDensity pd = new PopulationDensity();
		String js = pd.runAll("2014-04-20", "00", "2014-04-21", "00", 7.6203,45.0945,7.6969,45.0774, "FIX_Piemonte.ser", "");
		System.out.println(js);
		
		Logger.logln("Done!");
	}
	
	private static final SimpleDateFormat F = new SimpleDateFormat("yyyy-MM-dd-hh");
	
	public String runAll(String sday,String shour,String eday, String ehour, double lon1, double lat1, double lon2, double lat2, String regionMap, String sconstraints) {
		Map<String,String> constraints = new HashMap<String,String>();
		if(sconstraints.contains("=")) {
			String[] elements = sconstraints.split(";");
			for(String e: elements) {
				String[] nameval = e.split("=");
				constraints.put(nameval[0],nameval[1]);
			}
		}
		return runAll(sday,shour,eday,ehour,lon1,lat1,lon2,lat2,regionMap,constraints);
	}
	
	public String runAll(String sday,String shour,String eday, String ehour, double lon1, double lat1, double lon2, double lat2, String regionMap, Map<String, String> constraints) {
		
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
			Placemark p = new Placemark("tmp",new double[]{lat,lon},r);
			
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
			
			UserEventCounterCellacXHour.process(p); // file name is called as the placemark
			PLSSpaceDensity.process(rm, Config.getInstance().base_folder+"/UserEventCounter/"+p.getName()+"_cellXHour.csv", null, null);
			File pls_space_density_file = new File(Config.getInstance().base_folder+"/PLSSpaceDensity/"+p.getName()+"_"+rm.getName()+".csv");
			Map<String,Double> space_density = pd.computeSpaceDensity(pls_space_density_file,rm,constraints);
			
			plotSpaceDensity(p.getName()+getFileSuffix(constraints), space_density, rm,0);
			
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
	
	
	private String getFileSuffix(Map<String,String> constraints) {
		String suffix = "";
		if(constraints != null) {
			for(String key: constraints.keySet())
				suffix += "_"+key+"_"+constraints.get(key);
		}
		return suffix;
	}
	
	public void plotSpaceDensity(String city, Map<String,Double> space_density, RegionMap rm, double threshold) throws Exception {
		File d = new File(Config.getInstance().web_kml_folder);
		d.mkdirs();
		KMLHeatMap.drawHeatMap(d.getAbsolutePath()+"/"+city+"_"+rm.getName()+".kml",space_density,rm,city,false);
		HeatMapGoogleMaps.draw(d.getAbsolutePath()+"/"+city+"_"+rm.getName()+".html", city, space_density, rm, threshold);
	}
	
	
	public Map<String,Double> computeSpaceDensity(File pls_space_density_file, RegionMap rm, Map<String,String> constraints) throws Exception {
		String city = rm.getName();
		
		BufferedReader br = new BufferedReader(new FileReader(pls_space_density_file));
		
		Map<String,Double> sd = new HashMap<String,Double>();
		for(RegionI r: rm.getRegions())
			sd.put(r.getName(), 0.0);
		
		String line;
		while((line=br.readLine())!=null) {
			String[] p = line.split(",");
			
			String username = p[0];
			String mnt = p[1];
			int num_pls = Integer.parseInt(p[2]);
			int num_days = Integer.parseInt(p[3]);
			int days_interval = Integer.parseInt(p[4]);
			
			
			if(okConstraints(mnt,num_days,constraints)) {
				for(int i=5;i<p.length;i++) {
					String[] x = p[i].split(":");
					String rname = rm.getRegion(Integer.parseInt(x[2])).getName();
					double v = Double.parseDouble(x[3]);	
					sd.put(rname,sd.get(rname)+v);
				}
			}
		}
		br.close();
		
		for(String k : sd.keySet()) {
			double val = sd.get(k);
			double area = rm.getRegion(k).getGeom().getArea();
			sd.put(k, val/area);
		}
		
		
		
		return sd;
	}
	
	
	private boolean okConstraints(String ui_mnt, int ui_num_days, Map<String,String> constraints) {
		if(constraints!=null) {
			String mnt  = constraints.get("mnt");
			if(mnt!=null) {
				if(mnt.startsWith("!")) { 
					//System.err.println(ui.mnt+"VS"+mnt.substring(1));
					if(ui_mnt.equals(mnt.substring(1))) return false;
				}
				else
					if(!ui_mnt.equals(mnt)) return false;
			}
			String mindays = constraints.get("mindays");
			if(mindays!=null) 
				if(ui_num_days < Integer.parseInt(mindays)) return false;
			
			
			String maxdays = constraints.get("maxdays");
			if(maxdays!=null) 
				if(ui_num_days > Integer.parseInt(maxdays)) return false;
		}
		return true;
	}
}
