package analysis.tourist;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import network.NetworkCell;
import network.NetworkMap;
import network.NetworkMapFactory;
import pls_parser.UserEventCounterCellacXHour;
import utils.CopyAndSerializationUtils;
import utils.FileUtils;
import utils.GeomUtils;
import utils.Logger;
import area.region.Region;
import area.region.RegionMap;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;


/*
 *This class stores the following information associated to each user
 * user_id, mnt, num_pls, num,_days, 
 * {7*24*N matrix with the number of pls produced in a given area of the city in a givne day and hour - N are the areas of the city obtained from Voronoi}
 */

public class TouristData implements Serializable {
	
	
	static transient Map<String,Integer> DM = new HashMap<String,Integer>();
	static {
		DM.put("Mon", 0);
		DM.put("Tue", 1);
		DM.put("Wed", 2);
		DM.put("Thu", 3);
		DM.put("Fri", 4);
		DM.put("Sat", 5);
		DM.put("Sun", 6);
	}
	static transient Map<String,float[]> cache_intersection = new HashMap<String,float[]>();
	static transient NetworkMap nm = NetworkMapFactory.getNetworkMap();
	
	
	private String user_id;
	private String mnt;
	private int num_pls;
	private int num_days;
	private float[][][] plsMatrix;
	
	/* String events is in the form:
	 * user_id, mnt, num_pls, num,_days, 2013-5-23:Sun:cellac,....
	 * EXAMPLE:
	 * 1b44888ff4f,22201,3,1,2013-5-23:Sun:13:4018542484,2013-5-23:Sun:17:4018542495,2013-5-23:Sun:13:4018542391,
	*/
	

	public TouristData(String events, RegionMap map) {
		String[] p = events.split(",");
		user_id = p[0];
		mnt = p[1];
		num_pls = Integer.parseInt(p[2]);
		num_days = Integer.parseInt(p[3]);
		
		plsMatrix = new float[7][24][map.getNumRegions()];
		
		for(int i=4;i<p.length;i++) {
			// 2013-5-23:Sun:13:4018542484
			String[] x = p[i].split(":");
			int day = DM.get(x[1]);
			int h = Integer.parseInt(x[2]);
			String celllac = x[3];
			float[] ai = cache_intersection.get(celllac);
			if(ai == null) {
				ai = computeAreaIntersection(celllac,map);
				cache_intersection.put(celllac, ai);
			}
			
			for(int k=0; k<ai.length;k++) 
				plsMatrix[day][h][k] += ai[k];
		}
		
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<plsMatrix.length;i++)
		for(int j=0; j<plsMatrix[0].length;j++)
	    for(int k=0; k<plsMatrix[0][0].length;k++)
	    	if(plsMatrix[i][j][k] > 0)
	    		sb.append(","+i+":"+j+":"+k+":"+plsMatrix[i][j][k]);
		
		return user_id+","+mnt+","+num_pls+","+num_days+sb.toString();
	}
	
	
	public static float[] computeAreaIntersection(String celllac, RegionMap map) {
		float[] area_intersection = new float[map.getNumRegions()];
		NetworkCell nc = nm.get(Long.parseLong(celllac));
		Polygon circle = GeomUtils.getCircle(nc.getBarycentreLongitude(), nc.getBarycentreLatitude(), nc.getRadius());
		double ca = Math.PI * Math.pow(nc.getRadius(),2);
		int i=0;
		for(Region r: map.getRegions()) {
			boolean overlaps = r.getGeom().overlaps(circle);
			if(overlaps) {
				Geometry a = r.getGeom().intersection(circle);
				area_intersection[i] = (float)(a.getArea()/ca);
			}
			i++;
		}
		return area_intersection;
	}
	
	
	
	public static void main(String[] args) throws Exception {
		String city = "Venezia";
		process(city);
		Logger.logln("Done");
	}
	
	public static void process(String city) throws Exception {
		
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(FileUtils.getFile("RegionMap/"+city+".ser"));
		BufferedReader br = FileUtils.getBR("UserEventCounter/"+city+"_cellacXhour.csv");
		if(br == null) {
			UserEventCounterCellacXHour.process(city);
			br = FileUtils.getBR("UserEventCounter/"+city+"_cellacXhour.csv");
		}
		
		File f = new File(FileUtils.create("TouristData")+"/"+city+".ser");
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
		
		int i=0;
		String line;
		while((line=br.readLine())!=null) {
			out.println(new TouristData(line,rm));
			i++;
			if(i % 10000 == 0) {
				Logger.logln("Processed "+i+" users...");
			}
		}
		br.close();
		out.close();
	}
	
}
