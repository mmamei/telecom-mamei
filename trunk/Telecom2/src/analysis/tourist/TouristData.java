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
	
	public static final String TIM_MNT = "22201";
	
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
	
	
	String user_id;
	String mnt;
	int num_pls;
	int num_days;
	float[][][] plsMatrix;
	
	/* String events is in the form:
	 * user_id, mnt, num_pls, num,_days, 2013-5-23:Sun:cellac,....
	 * EXAMPLE:
	 * 1b44888ff4f,22201,3,1,2013-5-23:Sun:13:4018542484,2013-5-23:Sun:17:4018542495,2013-5-23:Sun:13:4018542391,
	*/
	

	public TouristData(String events, RegionMap map) {
		
		if(events == null) return; // need for a null construcutor for testing
		
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
	
	
	public boolean roaming() {
		return !mnt.equals(TIM_MNT);
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
	
	
	public String toSVMString(int clazz) {
		StringBuffer sb = new StringBuffer();
		int fcont = 4;
		for(int i=0; i<plsMatrix.length;i++)
		for(int j=0; j<plsMatrix[0].length;j++)
	    for(int k=0; k<plsMatrix[0][0].length;k++) {
	    	if(plsMatrix[i][j][k] > 0)
	    		sb.append(" "+fcont+":"+plsMatrix[i][j][k]);
	    	fcont++;
	    }
		int roaming = roaming()? 0 : 1;
		return clazz+" 1:"+roaming+" 2:"+num_pls+" 3:"+num_days+sb.toString();
	}
	
	
	
	// d_periods = {0,0,0,0,0,1,1}
	// mapping weekdays in 0
	// mapping weekends in 1
	// h_periods = {3,3,3,3,3,3,3,0,0,0,0,0,0,0,1,1,1,1,1,2,2,2,2,3}
	// mapping [7-13] in 0 (morning), [14,18] in 1 (afternoon), [19-22] in 2 (evening), [23-6] in 3 (night)
	
	
	public void compactTime(int[] d_periods, int[] h_periods) {
		if(d_periods!=null) compact(d_periods,0);
		if(h_periods!=null) compact(h_periods,1);
	}
	
	
	/*
	 * This code is rather tricky. To have just one method, I pass the index cindex that has to be reduced.
	 * Then I refer to the matrix by means of an array of indices (or lenghts) so that I can then address the specific index cindex, without if-statements
	 */
	
	
	private void compact(int[] frames, int cindex) {
		
		
		// compute size of the reduced dimenstion
		int flength = 0;
		for(int x: frames) 
			flength = (int)Math.max(flength, x+1);
		
		int[] sizes = new int[3];
		sizes[0] = plsMatrix.length;
		sizes[1] = plsMatrix[0].length;
		sizes[2] = plsMatrix[0][0].length;
		
		sizes[cindex] = flength; // overwrite with the new size
		
		float[][][] compactPlsMatrix = new float[sizes[0]][sizes[1]][sizes[2]];
		
		int[] i = new int[3];
		int[] ci = new int[3];
		for(i[0] = 0; i[0]<plsMatrix.length;i[0]++)
		for(i[1] = 0; i[1]<plsMatrix[0].length;i[1]++)
		for(i[2] = 0; i[2]<plsMatrix[0][0].length;i[2]++) {
			System.arraycopy(i, 0, ci, 0, i.length);
			ci[cindex] = frames[ci[cindex]];
			compactPlsMatrix[ci[0]][ci[1]][ci[2]] += plsMatrix[i[0]][i[1]][i[2]];
		}
		
		plsMatrix = compactPlsMatrix;
	}
	

	
	
	public static float[] computeAreaIntersection(String celllac, RegionMap map) {
		float[] area_intersection = new float[map.getNumRegions()];
		NetworkCell nc = nm.get(Long.parseLong(celllac));
		Polygon circle = GeomUtils.getCircle(nc.getBarycentreLongitude(), nc.getBarycentreLatitude(), nc.getRadius());
		double ca = Math.PI * Math.pow(nc.getRadius(),2);
		int i=0;
		for(Region r: map.getRegions()) {
			Geometry a = r.getGeom().intersection(circle);
			area_intersection[i] = (float)(GeomUtils.geoArea(a)/ca);
			i++;
		}
		
		// normailze to 1
		float sum = 0;
		for(float f: area_intersection)
			sum += f;
		for(i=0; i<area_intersection.length;i++)
			area_intersection[i] = area_intersection[i] / sum;
		
		
		return area_intersection;
	}
	
	
	
	// d_periods = {0,0,0,0,0,1,1}
	// mapping weekdays in 0
	// mapping weekends in 1
	// h_periods = {3,3,3,3,3,3,3,0,0,0,0,0,0,0,1,1,1,1,1,2,2,2,2,3}
	// mapping [7-13] in 0 (morning), [14,18] in 1 (afternoon), [19-22] in 2 (evening), [23-6] in 3 (night)
		
	
	public static void main(String[] args) throws Exception {
		/*
		String city = "Venezia";
		int[] d_periods = null;
		int[] h_periods = null;
		process(city,d_periods,h_periods);
		Logger.logln("Done");
		*/
		
		
		int[] frames = new int[]{0,1,1,2};
		int cindex = 1;
		
		TouristData td = new TouristData(null,null);
		td.plsMatrix = new float[3][4][4];
		init(td.plsMatrix,1);
		
		print(td.plsMatrix);
		td.compact(frames, cindex);
		print(td.plsMatrix);
	}
	
	
	private static void init(float[][][] plsMatrix,float v) {
		for(int i=0; i<plsMatrix.length;i++) 
		for(int j=0; j<plsMatrix[0].length;j++) 
		for(int k=0; k<plsMatrix[0][0].length;k++) 
			plsMatrix[i][j][k] = v;
	}
	
	
	private static void print(float[][][] plsMatrix) {
		for(int i=0; i<plsMatrix.length;i++) {
			System.out.println("--------- "+i+":");
			for(int j=0; j<plsMatrix[0].length;j++) {
			for(int k=0; k<plsMatrix[0][0].length;k++) 
				System.out.print((int)plsMatrix[i][j][k]+"\t");
			System.out.println();
			}
		} 
	}
	
	
	public static void process(String city, int[] d_periods, int[] h_periods) throws Exception {
		
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(FileUtils.getFile("RegionMap/"+city+".ser"));
		BufferedReader br = FileUtils.getBR("UserEventCounter/"+city+"_cellacXhour.csv");
		if(br == null) {
			UserEventCounterCellacXHour.process(city);
			br = FileUtils.getBR("UserEventCounter/"+city+"_cellacXhour.csv");
		}
		
		File f = new File(FileUtils.create("TouristData")+"/"+city+".txt");
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
		
		int i=0;
		String line;
		TouristData td;
		while((line=br.readLine())!=null) {
			td = new TouristData(line,rm);
			td.compactTime(d_periods, h_periods);
			out.println(td);
			i++;
			if(i % 10000 == 0) {
				Logger.logln("Processed "+i+" users...");
			}
		}
		br.close();
		out.close();
	}
	
}
