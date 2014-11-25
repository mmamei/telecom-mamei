package analysis;

import java.io.File;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.gps.utils.LatLonPoint;
import org.gps.utils.LatLonUtils;

import region.RegionI;
import utils.Config;
import dataset.NetworkMapFactoryI;
import dataset.file.DataFactory;

public class RadiusOfGyration {
	
	public static void main(String[] args) throws Exception {
		Config.getInstance().changeDataset("ivory-set3");
		String dir = "file_pls_ivory_users_2000_10000";
		//String dir = "file_pls_lomb_users_200_10000";
		DescriptiveStatistics stats = new DescriptiveStatistics();
		File in_dir = new File(Config.getInstance().base_folder+"/UsersCSVCreator/"+dir);
		int cont = 0;
		for(File f: in_dir.listFiles()) {
			String filename = f.getName();
			String username = filename.substring(0, filename.indexOf(".csv"));
			double gr = computeGyrationRadius(PLSEvent.readEvents(f));
			stats.addValue(gr);
			cont ++;
			if(cont%1000 == 0) System.out.println("Processed "+cont+" users.....");
		}
		for(int i=1; i<100;i++)
			System.out.println(i+","+stats.getPercentile(i));
	}
	
	
	public static double computeGyrationRadius(List<PLSEvent> events) {
		// compute centriod
		double clon = 0;
		double clat = 0;
		double cont = 0;
		
		
		NetworkMapFactoryI nmf = DataFactory.getNetworkMapFactory();
		for(PLSEvent e: events) {
			RegionI r = nmf.getNetworkMap(e.getTimeStamp()).getRegion(e.getCellac());
			if(r!=null) {
				double[] latlon = r.getLatLon();
				clat += latlon[0];
				clon += latlon[1];
				cont++;
			}
		}
		clat = clat / cont;
		clon = clon / cont;
		LatLonPoint c = new LatLonPoint(clat,clon);
		
		double sum_sq_d = 0; // sum square distances
		for(PLSEvent e: events) {
			RegionI r = nmf.getNetworkMap(e.getTimeStamp()).getRegion(e.getCellac());
			if(r!=null) {
				LatLonPoint p = r.getCenterPoint();
				double d = LatLonUtils.getHaversineDistance(c, p);	
				sum_sq_d += d*d;
			}
		}
		return Math.sqrt(sum_sq_d / cont);
	}
	
	
	
}
