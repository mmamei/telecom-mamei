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
import dataset.EventFilesFinderI;
import dataset.file.DataFactory;
import dataset.file.LocationsXUserAroundAnEvent;

public class ODMatrixTime {
	public static void main(String[] args) throws Exception {
		ODMatrixTime od = new ODMatrixTime();
		String js = od.runAll("2014-04-20", "00", "2014-04-21", "00", 7.6203,45.0945,7.6969,45.0774, "FIX_Piemonte.ser", "");
		System.out.println(js);
	}
	
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
	
	private static final SimpleDateFormat F = new SimpleDateFormat("yyyy-MM-dd-hh");
	
	public String runAll(String sday,String shour,String eday, String ehour, double lon1, double lat1, double lon2, double lat2, String regionMap, Map<String, String> constraints) {	
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
			
			
			LocationsXUserAroundAnEvent.process(p, Config.getInstance().pls_start_time, Config.getInstance().pls_end_time);
			File file = new File(LocationsXUserAroundAnEvent.getOutputFile(p, Config.getInstance().pls_start_time, Config.getInstance().pls_end_time));
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while((line=br.readLine())!=null) {
				line = line.substring(line.indexOf(",")+1).trim();
				String[] cells = line.split(" ");
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
						c = c == null ? 1 : c+1;
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
