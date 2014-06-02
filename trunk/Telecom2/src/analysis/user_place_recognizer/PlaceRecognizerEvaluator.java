package analysis.user_place_recognizer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gps.utils.LatLonPoint;
import org.gps.utils.LatLonUtils;

import region.RegionMap;
import dataset.file.DataFactory;

public class PlaceRecognizerEvaluator {
	
	private double maxdist;
	private Map<String, List<LatLonPoint>> groundtruth;
	
	public PlaceRecognizerEvaluator(double maxdist) {
		this.maxdist = maxdist;
		groundtruth = new HashMap<String, List<LatLonPoint>>();
		try {
			BufferedReader in = new BufferedReader(new FileReader("config/groundtruth.txt"));
			String line;
			while((line=in.readLine()) != null){
				String[] splitted = line.split("\t");
				String username = splitted[0];
				for(int i=1; i<splitted.length;i++) {
					String[] gt_elements = splitted[i].split(",");
					String kind_of_place = gt_elements[0].toUpperCase();
					double lat = Double.parseDouble(gt_elements[1]);
					double lon = Double.parseDouble(gt_elements[2]);
					List<LatLonPoint> lp = new ArrayList<LatLonPoint>();
					lp.add(new LatLonPoint(lat,lon));
					groundtruth.put(username+"_"+kind_of_place,lp);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}		
	}
	/*
	public void evaluate(Map<String, List<LatLonPoint>> allResults) {
		Map<String,int[]> stats = new HashMap<String,int[]>();
		for(String x: allResults.keySet()) {
			String kind_of_place = x.split("_")[1];
			List<LatLonPoint> gt = groundtruth.get(x);
			if(gt.size()!=1) {
				Logger.logln("Warning: "+x+" has multiple or none groundtruth");
				continue;
			}
			int[] r = analyze(gt.get(0),allResults.get(x),.FIX ME..);
			int[] tot_r = stats.get(kind_of_place);
			if(tot_r == null) {
				tot_r = new int[6];
			}
			tot_r[0] += r[0]; // tp
			tot_r[1] += r[1]; // fp
			tot_r[2] += r[2]; // fn
			if(r[0] > 0) {
				tot_r[3] += r[3]; // avg dist num
				tot_r[4] ++; // avg dist den
				tot_r[5] = Math.max(tot_r[5], r[3]); // max dist
			}
			stats.put(kind_of_place, tot_r);
			
			if(r[0] > 0)	Logger.logln(x+" tp = "+r[0]+" fp = "+r[1]+" fn = "+r[2]+" dist = "+r[3]);
			else	Logger.logln(x+" tp = "+r[0]+" fp = "+r[1]+" fn = "+r[2]);
				
		} 
		
		Logger.logln("\n");
		for(String place: stats.keySet()) {
			int r[] = stats.get(place);
			int recall = 100 * r[0] / (r[0] + r[2]);
			int precision = 100 * r[0] / (r[0] + r[1]);
			Logger.logln(place+" tp = "+r[0]+" fp = "+r[1]+" fn = "+r[2]+" avg_dist = "+r[3]/r[4]+" max_dist = "+r[5]+" recall = "+recall+"% precision = "+precision+"%");
		}
		Logger.logln("\n");
		
		
	}
	*/
	
	public int[] analyze(LatLonPoint ref, List<LatLonPoint> res, Calendar cal) {
		int tp = 0;
		int fp = 0;
		int fn = 0;
		
		RegionMap nm = DataFactory.getNetworkMapFactory().getNetworkMap(cal);
		double avg_r = nm.getAvgRegionRadiusAround(ref, 4000);
		
		double max_dist = 1.0 * (maxdist + avg_r); // avg_r * UserNew.RESULT_FACTOR; 
		//max_dist = Math.max(4000, avg_r * factor);
		//System.out.println("[tolerance = "+(int)max_dist+"] ");
		
		double minDist = Double.MAX_VALUE;
		for(LatLonPoint p: res) 
			minDist = Math.min(minDist, LatLonUtils.getHaversineDistance(ref, p));
		if(minDist <= max_dist) 
			tp = 1;
		fn = 1 - tp;
		fp = res.size() - tp;
		return new int[]{tp,fp,fn,(int)minDist};
	}
}
