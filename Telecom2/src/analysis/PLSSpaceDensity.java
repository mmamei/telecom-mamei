package analysis;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import region.Placemark;
import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;
import analysis.tourist.GTExtractor;


/*
 * This class stores the following information associated to each user
 * user_id, mnt, num_pls, num,_days, 
 * {7*24*N matrix with the number of pls produced in a given area of the city in a given day and hour - N are the areas of the city obtained from Voronoi}
 */

public class PLSSpaceDensity implements Serializable {
	

	// d_periods = {0,0,0,0,0,1,1}
	// mapping weekdays in 0
	// mapping weekends in 1
	// h_periods = {3,3,3,3,3,3,3,0,0,0,0,0,0,0,1,1,1,1,1,2,2,2,2,3}
	
	public static transient final String[] DP = new String[]{"W","W","W","W","W","WE","WE"};
	
	public static transient final String[] HP1 = new String[]{"N","N","N","N","N","N","N","M", 
															  "M","M","M","M","M","M","A","A", 
															  "A","A","A","E","E","E","E","N"};
	
	public static transient final String[] HP2 = new String[]{"H","H","H","H","H","H","H","W", 
															  "W","W","W","W","W","W","W","W", 
															  "W","W","W","W","W","H","H","H"};
	
	public static transient final String[] HP3 = new String[]{"H","H","H","H","H","H","H","H", 
		  													  "H","H","H","H","H","H","H","H", 
		  													  "H","H","H","H","H","H","H","H"};
	public static transient final String[] HP = HP2;
	
	// these will be overwritten in case of a compact operation
	public static transient String[] DP_LABELS = new String[]{"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
	public static transient String[] HP_LABELS = new String[]{"0","1","2","3","4","5","6","7",
		 													  "8","9","10","11","12","13","14","15",
		 													  "16","17","18","19","20","21","22","23"};
	
	
	public static transient int[] DP_INT;
	public static transient int[] HP_INT;
	
	
	public static transient boolean COMPACT_SPACE = false;
	
	static {
		if(DP != null) { DP_LABELS = changePeriodLables(DP); DP_INT = toNum(DP);}
		if(HP != null) { HP_LABELS = changePeriodLables(HP); HP_INT = toNum(HP);}	
	}
	
	private static String[] changePeriodLables(String[] p) {
		List<String> l = new ArrayList<String>();
		for(String x: p)
			if(!l.contains(x)) l.add(x);
		String[] labels = new String[l.size()];
		return l.toArray(labels);
	}
	
	private static int[] toNum(String[] l) {
		int[] x = new int[l.length];
		
		Map<String,Integer> map = new HashMap<String,Integer>();
		int cont = 0;
		for(int i=0; i<x.length;i++) {
			Integer n = map.get(l[i]);
			if(n==null) { 
				n = cont;
				map.put(l[i], n);
				cont++;
			}
			x[i] = n;
		}
		return x;
	}
	
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
	
	public static transient String[] MAP_LABELS = null;	
	
	public String user_id;
	
	// feature vector
	public String mnt;
	public int num_pls;
	public int num_days;
	public int days_interval;
	
	public int num_days_in_area;
	public int num_nights_in_area;
	public int num_weekends_in_area;
	public int max_h_interval;
	
	
	public float[][][] plsMatrix;
	

	/* String events is in the form:
	 * user_id, mnt, num_pls, num,_days, 2013-5-23:Sun:cellac,....
	 * EXAMPLE:
	 * 1b44888ff4f,22201,3,1,2013-5-23:Sun:13:4018542484,2013-5-23:Sun:17:4018542495,2013-5-23:Sun:13:4018542391,
	*/
	
	
	
	private static final SimpleDateFormat F = new SimpleDateFormat("yyyy-MM-dd");
	public PLSSpaceDensity(String events, RegionMap map) throws Exception {
		
		//if(events == null) return; // need for a null construcutor for testing
		
		if(MAP_LABELS == null) {
			MAP_LABELS = new String[map.getNumRegions()];
			int c = 0;
			for(RegionI r: map.getRegions()) {
				MAP_LABELS[c] = r.getName().replaceAll(",", "_");
				c++;
			}
		}
		
		String[] p = events.split(",");
		user_id = p[0];
		mnt = p[1];
		num_pls = Integer.parseInt(p[2]);
		num_days = Integer.parseInt(p[3]);
		days_interval = Integer.parseInt(p[4]);
		
		plsMatrix = new float[7][24][map.getNumRegions()];
		Set<String> days_in_area = new HashSet<String>();
		Set<String> nights_in_area = new HashSet<String>();
		float[] ai;
		
		
		// the following three variables are used to compute the maximum time interval in which the user is inside the city 
		// this is useful to classify user in transit (their maximum time interval must be small)
		boolean inarea = false; // boolean variable to see if the user is currently in the city
		long prevTime = -1; // the last time the user entered the city
		int max_dh = -1; // the maximum time interval (in hours) in which the user has been in the city
		
		
		for(int i=5;i<p.length;i++) {
			try {
				// 2013-5-23:Sun:13:4018542484
				String[] x = p[i].split(":");
				
				//System.out.println("---> "+x[0]+" --> "+F.parse(x[0]));
				
				int day = DM.get(x[1]);
				int h = Integer.parseInt(x[2]);
				long celllac =Long.parseLong(x[3].trim());
				ai = map.computeAreaIntersection(celllac,F.parse(x[0]).getTime());
				
				Calendar cal = new GregorianCalendar();
				cal.setTime(F.parse(x[0]));
				cal.set(Calendar.HOUR_OF_DAY, h);
				long time = cal.getTimeInMillis();
				
				if(placemark.contains(celllac)) {
				//if(!Double.isNaN(ai[0])) {
					// event inside the city area
					for(int k=0; k<ai.length;k++) 
						plsMatrix[day][h][k] += ai[k];
					
					days_in_area.add(x[0]+":"+x[1]);
					
					if(h >= 21 || h <= 6) nights_in_area.add(x[0]+":"+x[1]);
						

					
					if(!inarea) { // before I was out
						prevTime = time;		
					}
					else { // before I was in
						int dh = (int)((time - prevTime)/(3600 * 1000));
						if(max_dh == -1 || dh > max_dh) max_dh = dh;
					}
					inarea = true;
				}
				else {
					
					
					
					// event outside the city area
					inarea = false;
					prevTime = time;
				}
			} catch(Exception e) {
				System.out.println("Problems with "+p[i]);
				e.printStackTrace();
			}
		}
		max_h_interval = max_dh;
		num_days_in_area = days_in_area.size();
		num_nights_in_area = nights_in_area.size();
		num_weekends_in_area  = 0;
		for(String d: days_in_area)
			if(d.endsWith("Sat") || d.endsWith("Sun"))
				num_weekends_in_area ++;
		
		compactTime();
		if(COMPACT_SPACE) compactSpace();
		normalize();
	}
	
	
	
	
	public boolean isItalian() {
		return mnt.startsWith("222");
	}
	
	
	/*
	 * @RELATION iris
	 * @ATTRIBUTE sepallength  NUMERIC
	 * @ATTRIBUTE sepalwidth   NUMERIC
	 * @ATTRIBUTE class        {Iris-setosa,Iris-versicolor,Iris-virginica}
	 */
	private final int NUM_FEATURES_BEFORE_PLS_MATRIX = 9;
	public String wekaHeader(String title) {
		StringBuffer sb = new StringBuffer();
		sb.append("@RELATION "+title+"\n");
		
		//sb.append("@ATTRIBUTE roaming {TIM,ROAMING}\n");
		sb.append("@ATTRIBUTE user STRING\n");
		sb.append("@ATTRIBUTE mnt NUMERIC\n");
		
		sb.append("@ATTRIBUTE num_pls NUMERIC\n");
		sb.append("@ATTRIBUTE num_days NUMERIC\n");
		sb.append("@ATTRIBUTE days_interval NUMERIC\n");
		
		sb.append("@ATTRIBUTE num_days_in_area NUMERIC\n");
		sb.append("@ATTRIBUTE num_nights_in_area NUMERIC\n");
		sb.append("@ATTRIBUTE num_weekends_in_area NUMERIC\n");
		
		sb.append("@ATTRIBUTE max_h_interval NUMERIC\n");
		
		for(int i=0; i<plsMatrix.length;i++)
		for(int j=0; j<plsMatrix[0].length;j++)
		if(COMPACT_SPACE)
			sb.append("@ATTRIBUTE "+DP_LABELS[i]+"_"+HP_LABELS[j]+" NUMERIC\n");
		else {
			for(int k=0; k<plsMatrix[0][0].length;k++) {
				sb.append("@ATTRIBUTE "+DP_LABELS[i]+"_"+HP_LABELS[j]+"_"+MAP_LABELS[k]+" NUMERIC\n");
			}
		}
		sb.append("@ATTRIBUTE class {"+GTExtractor.CLASSES+"}\n");
		sb.append("@DATA\n");
		return sb.toString();
	}
	
	
	/*
	 *     @data
	 *     {1 X, 3 Y, 4 "class A"}
	 *     {2 W, 4 "class B"}
	 *     Each instance is surrounded by curly braces, and the format for each entry is: <index> <space> <value> where index is the attribute index (starting from 0).
	 */
	public String toWEKAString(String clazz) {
		StringBuffer sb = new StringBuffer();
		int fcont = NUM_FEATURES_BEFORE_PLS_MATRIX;
		for(int i=0; i<plsMatrix.length;i++)
		for(int j=0; j<plsMatrix[0].length;j++)
	    for(int k=0; k<plsMatrix[0][0].length;k++) {
	    	if(plsMatrix[i][j][k] > 0)
	    		sb.append(","+fcont+" "+plsMatrix[i][j][k]);
	    	fcont++;
	    }
		String roaming = mnt.substring(0,3);//isItalian()? "TIM" : "ROAMING";
		String class_attribute = clazz == null ? ", "+fcont+" ?"  : ", "+fcont+" "+clazz;
		
		
		//if(max_h_interval > 10 && max_h_interval!=1000 && num_pls < 10  && clazz!=null && clazz.equals("Transit")) System.out.println(user_id);
		
		String max_h_attribute = max_h_interval > 0 ? String.valueOf(max_h_interval) : "?";
		
		
		return "{0 "+user_id+", 1 "+roaming+", 2 "+num_pls+", 3 "+num_days+", 4 "+days_interval+", 5 "+num_days_in_area+", 6 "+num_nights_in_area+", "
				+ "7 "+num_weekends_in_area+", 8 "+max_h_attribute+sb.toString()+class_attribute+"}";
	}
	
	
	
	// d_periods = {0,0,0,0,0,1,1}
	// mapping weekdays in 0
	// mapping weekends in 1
	// h_periods = {3,3,3,3,3,3,3,0,0,0,0,0,0,0,1,1,1,1,1,2,2,2,2,3}
	// mapping [7-13] in 0 (morning), [14,18] in 1 (afternoon), [19-22] in 2 (evening), [23-6] in 3 (night)
	
	
	private void compactTime() {
		if(DP!=null) compact(DP_INT,0);
		if(DP!=null) compact(HP_INT,1);
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
	
	
	private void compactSpace() {
		float[][][] cplsMatrix = new float[plsMatrix.length][plsMatrix[0].length][1];
		for(int i=0; i<plsMatrix.length;i++)
		for(int j=0; j<plsMatrix[0].length;j++)
		for(int k=0; k<plsMatrix[0][0].length;k++) 
			cplsMatrix[i][j][0] +=  plsMatrix[i][j][k];
		
		for(int i=0; i<plsMatrix.length;i++)
		for(int j=0; j<plsMatrix[0].length;j++)
			cplsMatrix[i][j][0] = Math.round(cplsMatrix[i][j][0]);
		plsMatrix = cplsMatrix;
	}
	
	
	private void normalize() {
		for(int i=0; i<plsMatrix.length;i++)
		for(int j=0; j<plsMatrix[0].length;j++)
		for(int k=0; k<plsMatrix[0][0].length;k++) 
			plsMatrix[i][j][k] = plsMatrix[i][j][k] / num_pls;
	}

	// d_periods = {0,0,0,0,0,1,1}
	// mapping weekdays in 0
	// mapping weekends in 1
	// h_periods = {3,3,3,3,3,3,3,0,0,0,0,0,0,0,1,1,1,1,1,2,2,2,2,3}
	// mapping [7-13] in 0 (morning), [14,18] in 1 (afternoon), [19-22] in 2 (evening), [23-6] in 3 (night)
	
	private static Placemark placemark;
	
	static String POST = "_July2013";
	static {
		if(POST.equals("_July2013")) {
			Config.getInstance().pls_start_time = new GregorianCalendar(2013,Calendar.JULY,1,0,0,0);
			Config.getInstance().pls_end_time = new GregorianCalendar(2013,Calendar.JULY,31,23,59,59);
		}
		else if(POST.equals("_March2014")) {
			Config.getInstance().pls_start_time = new GregorianCalendar(2014,Calendar.MARCH,1,0,0,0);
			Config.getInstance().pls_end_time = new GregorianCalendar(2014,Calendar.MARCH,31,23,59,59);
		}
		else {
			System.out.println("ERROR IN SETTING pls_start_time AND pls_end_time");
			System.exit(0);
		}
	}
	
	
	public static void main(String[] args) throws Exception {
	
		/*
		String city = "Torino";
		String cellXHourFile = Config.getInstance().base_folder+"/UserEventCounter/Torino_cellXHour.csv";
		String gt_ser_file = "Firenze_gt_profiles.ser";
		*/
		
		String pre = "file_pls_ve_";
		String city = "Venezia";
		String mapfile = "venezia_area.ser"; 
		placemark = Placemark.getPlacemark(city);
		String cellXHourFile =Config.getInstance().base_folder+"/UserEventCounter/"+pre+city+"_cellXHour"+POST+".csv";
		String gt_ser_file = Config.getInstance().base_folder+"/Tourist/"+city+"_gt_profiles"+POST+".ser";
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/"+mapfile));
		process(rm,cellXHourFile,gt_ser_file,null);
		
		
		
		
		Logger.logln("Done");
	}
	

	
	public static void process(RegionMap rm, String cellXHourFile, String gt_ser_file, Integer max) throws Exception {
	
		BufferedReader br = new BufferedReader(new FileReader(new File(cellXHourFile)));
		
		//String placemark_name = cellXHourFile.substring(cellXHourFile.lastIndexOf("/")+1,cellXHourFile.lastIndexOf("_cellXHour.csv"));
		
		
		Map<String,String> user_gt_prof = null;
		if(gt_ser_file != null)
			user_gt_prof = (Map<String,String>)CopyAndSerializationUtils.restore(new File(gt_ser_file));
		
		String s = max == null ? "" : "_"+max;
	
		File dir = new File(Config.getInstance().base_folder+"/PLSSpaceDensity");
		dir.mkdirs();
		PrintWriter weka_out = new PrintWriter(new BufferedWriter(new FileWriter(new File(Config.getInstance().base_folder+"/Tourist")+"/"+rm.getName()+s+POST+".arff")));
		
		int i=0;
		String line;
		PLSSpaceDensity td;
		while((line=br.readLine())!=null) {
			if(line.startsWith("//")) continue;
			if(max != null && i > max) break;
			
			try {
				td = new PLSSpaceDensity(line,rm);
			} catch(Exception e) {
				System.err.println(line);
				continue;
			}
			
			if(i==0) weka_out.println(td.wekaHeader(rm.getName()+POST));
			
			String clazz = user_gt_prof == null ? null : user_gt_prof.get(td.user_id);
			weka_out.println(td.toWEKAString(clazz));
			
			/*
			if(line.startsWith("60c8e54db1761e8617deaa4d5515c56bb97bf5699e8395c0f3f1437f59eea")) {
				System.out.println(line);
				System.out.println(td.wekaHeader(rm.getName()));
				System.out.println(td.toWEKAString(clazz));
				return;
			}
			*/
			
			
			i++;
			if(i % 10000 == 0) {
				Logger.logln("Processed "+i+"th users...");
			}
		}
		br.close();
		weka_out.close();
	}
	
}
