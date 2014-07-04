package analysis.user_place_recognizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.gps.utils.LatLonPoint;
import org.gps.utils.LatLonUtils;

import region.RegionI;
import utils.Config;
import analysis.PLSEvent;
import dataset.NetworkMapFactoryI;
import dataset.file.DataFactory;

public class LearnTimeMatrix {
	
	public static double dist_threshold = 1000;
	
	public static void main(String[] args) throws Exception {
		
		Map<String, Map<String, LatLonPoint>> gt = read("G:/DATASET/PLS/volontari/users_info.txt");
		
		String kop = "Work";
		File dir = new File("G:/DATASET/PLS/volontari/pls");
		DescriptiveStatistics stats = new DescriptiveStatistics();
		NetworkMapFactoryI nmf = DataFactory.getNetworkMapFactory();
		for(String f: dir.list()) {
			String username = f.substring(0,f.indexOf("."));
			LatLonPoint latlon = gt.get(username).get(kop);
			List<PLSEvent> list = PLSEvent.readEvents(new File(dir+"/"+f));
			for(PLSEvent p: list) {
				//System.out.println(p);
				Calendar cal = p.getCalendar();
				RegionI networkcell = nmf.getNetworkMap(p.getTimeStamp()).getRegion(p.getCellac());
				if(networkcell!=null && LatLonUtils.getHaversineDistance(latlon, networkcell.getCenterPoint()) < dist_threshold) {
					stats.addValue(cal.get(Calendar.HOUR_OF_DAY));
				}
			}
		}
		
		PrintWriter out = new PrintWriter(new FileWriter(Config.getInstance().base_folder+"/time_matrix_"+kop+".csv"));
		for(double v: stats.getSortedValues())
			out.println(v);
		out.close();
		
		System.out.println("Mean = "+stats.getMean());
		System.out.println("SD = "+stats.getStandardDeviation());
	}
	
	
	
	
	public static Map<String, Map<String, LatLonPoint>> read(String f) throws Exception {
		Map<String, Map<String,LatLonPoint>> gt = new HashMap<String, Map<String, LatLonPoint>>();
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line;
		while((line = br.readLine())!=null) {
			String[] elements = line.split("\t");
			String username = elements[0];
			
			Map<String, LatLonPoint> ugt = gt.get(username);
			if(ugt == null) {
				ugt = new HashMap<String, LatLonPoint>();
				gt.put(username, ugt);
			}
			
			for(int i=1; i<elements.length;i++) {
				String[] x = elements[i].split(",");
				String kop = x[0];
				double lat = Double.parseDouble(x[1]);
				double lon = Double.parseDouble(x[2]);
				ugt.put(kop, new LatLonPoint(lat,lon));
			}
		}
		br.close();
		return gt;
	}
	
}
