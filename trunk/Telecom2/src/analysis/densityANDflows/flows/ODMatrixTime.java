package analysis.densityANDflows.flows;

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
import analysis.Constraints;
import analysis.PLSSpaceDensity;
import dataset.EventFilesFinderI;
import dataset.file.DataFactory;
import dataset.file.LocationsXUserAroundAnEvent;

public class ODMatrixTime {
	public static void main(String[] args) throws Exception {
		ODMatrixTime od = new ODMatrixTime();
		//String js = od.runAll("2014-04-20", "00", "2014-04-21", "00", 7.6203,45.0945,7.6969,45.0774, "FIX_Piemonte.ser", "");
		//String js = od.runAll("2012-04-11", "19", "2012-04-11", "23", 9.0937,45.4896,9.150,45.4655, "grid5", ";users_event_probscores=C:/BASE/PresenceCounter/C_DATASET_PLS_file_pls_file_pls_lomb/ProbScores/Stadio_San_Siro_(MI)-11_04_2012_19_00-11_04_2012_23_00.txt");	
		//String js = od.runAll("2014-05-01", "08", "2014-05-01", "11", 12.3135,45.4483,12.3611,45.4263, "grid20", "mnt=!22201");
		String js = od.runAll("2014-08-23", "18", "2014-08-23", "19", 16.6415,41.2241,18.3719,39.9519, "grid10", "");
	
		
		System.out.println(js);
	}
	
	public String runAll(String sday,String shour,String eday, String ehour, double lon1, double lat1, double lon2, double lat2, String regionMap, String sconstraints) {
		return runAll(sday,shour,eday,ehour,lon1,lat1,lon2,lat2,regionMap,new Constraints(sconstraints));
	}
	
	private static final SimpleDateFormat F = new SimpleDateFormat("yyyy-MM-dd-hh");
	
	public String runAll(String sday,String shour,String eday, String ehour, double lon1, double lat1, double lon2, double lat2, String regionMap, Constraints constraints) {	
		String allRoads = null;
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
		
			RegionMap nm = DataFactory.getNetworkMapFactory().getNetworkMap(Config.getInstance().pls_start_time);
		
			Map<Move,Double> list_od = new HashMap<Move,Double>();
			
			
			LocationsXUserAroundAnEvent.process(p, Config.getInstance().pls_start_time, Config.getInstance().pls_end_time, constraints);
			File file = new File(LocationsXUserAroundAnEvent.getOutputFile(p, Config.getInstance().pls_start_time, Config.getInstance().pls_end_time));
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while((line=br.readLine())!=null) {
				String[] user_locations = line.split(",");
				String user = user_locations[0];
				String locations = user_locations[1];
				String[] cells = locations.split(" ");
				for(int i=1;i<cells.length;i++) {
					RegionI nc1 = nm.getRegion(cells[i-1]);
					RegionI nc2 = nm.getRegion(cells[i]);
				
					//System.out.print(nc1.getBarycentreLongitude()+","+nc1.getBarycentreLatitude()+" ---> ");
					//System.out.println(nc2.getBarycentreLongitude()+","+nc2.getBarycentreLatitude());
				
					RegionI r1 = rm.get(nc1.getLatLon()[1], nc1.getLatLon()[0]);
					RegionI r2 = rm.get(nc2.getLatLon()[1], nc2.getLatLon()[0]);
				
					//if(!r1.getName().equals(r2.getName()))
					//System.out.println("Movement from: "+r1.getName()+" to "+r2.getName());
					if(r1!=null && r2!=null) {
						Move m = new Move(r1,r2);
						Double c = list_od.get(m);
						c = c == null ? 1 : c + constraints.weight(user);
						list_od.put(m, c);
						//System.err.println(m);
					}	
				}
			}
			br.close();		
			String n = file.getName();
			allRoads = ODMatrixVisual.draw("ODMatrixTime_"+n.substring(0,n.lastIndexOf(".")), list_od,false,dir);

		} catch(Exception e) {
			e.printStackTrace();
		}
		return allRoads;
	}
}
