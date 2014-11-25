package analysis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.gps.utils.LatLonPoint;
import org.gps.utils.LatLonUtils;

import region.RegionI;
import dataset.NetworkMapFactoryI;
import dataset.file.DataFactory;

public class RadiusOfGyrationFlickrTwitter {
	
	public static void main(String[] args) throws Exception {
	
		//BufferedReader br = new BufferedReader(new FileReader("G:/DATASET/Twitter-Flickr/AllTwitterUsers.csv"));
		BufferedReader br = new BufferedReader(new FileReader("G:/DATASET/Twitter-Flickr/TotalFlickrUsers4MatchingNoDuplicates.csv"));
		String line;
		
		Map<String,List<double[]>> map = new HashMap<String,List<double[]>>();
		String[] elements;
		
		while((line = br.readLine())!=null) {
			//elements = line.split(",");
			//String user = elements[1];
			//double lat = Double.parseDouble(elements[3]);
			//double lon = Double.parseDouble(elements[4]);
			
			elements = line.split("\t");
			String user = elements[0];
			double lat = Double.parseDouble(elements[3]);
			double lon = Double.parseDouble(elements[4]);
			
			List<double[]> latlon = map.get(user);
			if(latlon == null) {
				latlon = new ArrayList<double[]>();
				map.put(user, latlon);
			}
			latlon.add(new double[]{lat,lon});			
		}
		br.close();
		
		DescriptiveStatistics stats = new DescriptiveStatistics();
		for(List<double[]> event: map.values()) {
			stats.addValue(computeGyrationRadius(event));
		}
		
		for(int i=1; i<100;i++)
			System.out.println(i+","+stats.getPercentile(i));
	}
	
	
	public static double computeGyrationRadius(List<double[]> events) {
		// compute centriod
		double clon = 0;
		double clat = 0;
		double cont = 0;
		
		for(double[] latlon: events) {
			clat += latlon[0];
			clon += latlon[1];
			cont++;
		}
		clat = clat / cont;
		clon = clon / cont;
		LatLonPoint c = new LatLonPoint(clat,clon);
		
		double sum_sq_d = 0; // sum square distances
		for(double[] latlon: events) {
			LatLonPoint p = new LatLonPoint(latlon[0],latlon[1]);
			double d = LatLonUtils.getHaversineDistance(c, p);	
			sum_sq_d += d*d;
		}
		return Math.sqrt(sum_sq_d / cont);
	}
}
