package analysis.lda;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import network.NetworkMap;
import network.NetworkMapFactory;

import org.gps.utils.LatLonPoint;

import utils.FileUtils;

public class TestTopicAccuracy {
	
	
	public static final boolean VERBOSE = false;
	
	public static void main(String[] args) throws Exception {
		process("4a783b4759a83ac53e448ca5dd24294bea7e715876c69bcb552e873ae568e");
		System.out.println("Done!");
	}
	
	static NetworkMap nm = NetworkMapFactory.getNetworkMap();
	public static void process(String user) throws Exception  {
		
		// read p_w_z ****************************************************************************************************
		BufferedReader br = FileUtils.getBR("Topic/"+user+"/p_w_z.txt");
		String line;
		// Topic_0,n,7.666,45.0713-n,7.6529,45.055,0.27,n,7.6529,45.055-n,7.666,45.0713,0.2,m,7.6529,45.055-e,7.6587,45.0707,0.13,m,7.666,45.0713-a,7.6529,45.055,0.07
		Map<Integer,List<WordProb>> topics = new TreeMap<Integer,List<WordProb>>();
		while((line = br.readLine()) != null) {
			List<WordProb> topic = new ArrayList<WordProb>();
			String[] e = line.split(",|-");
			for(int i=1; i<e.length;i=i+7) {
				String time1 = e[i];
				double lon1 = Double.parseDouble(e[i+1]);
				double lat1 = Double.parseDouble(e[i+2]);
				String time2 = e[i+3];
				double lon2 = Double.parseDouble(e[i+4]);
				double lat2 = Double.parseDouble(e[i+5]);
				double p = Double.parseDouble(e[i+6]);
				topic.add(new WordProb(time1,lon1,lat1,time2,lon2,lat2,p));
			}
			int index = Integer.parseInt(e[0].substring(e[0].indexOf("_")+1));
			topics.put(index, topic);
		}
		br.close();
		
		if(VERBOSE) {
			for(int t: topics.keySet()) 
				System.out.println(WordProb.print(topics.get(t)));
		}
		
		// read p_z_d *******************************************************************************************************
		Map<String,double[]> pzd = new TreeMap<String,double[]>();
		br = FileUtils.getBR("Topic/"+user+"/p_z_d.txt");
		while((line = br.readLine()) != null) {
			String[] e = line.split(",");
			String day = e[0];
			double[] p = new double[e.length-1];
			for(int i=0; i<p.length;i++)
				p[i] = Double.parseDouble(e[i+1]);
			pzd.put(day, p);
		}
		br.close();
		
		if(VERBOSE) {
			for(String day: pzd.keySet()) {
				System.out.print(day);
				for(double p: pzd.get(day))
					System.out.print(", "+p);
				System.out.println();
			}
		}
		
		// read the user trace ************************************************************************************************
		
		Map<String,LatLonPoint[]> trace = new TreeMap<String,LatLonPoint[]>();
		br = FileUtils.getBR("UserEventCounter/Torino_cellXHour.csv");
		while((line=br.readLine())!=null) {
			if(line.startsWith("//")) 
				continue;
			String[] p = line.split(",");
			if(p[0].equals(user)) {
				
				String[] e = line.split(",");
				for(int i=5;i<e.length;i++) {
					// 2013-5-23:Sun:13:4018542484
					String[] x = e[i].split(":");
					String day = x[0];
					String day_of_week = x[1];
					int h = Integer.parseInt(x[2]);
					LatLonPoint point = nm.get(Long.parseLong(x[3])).getPoint();
					
					LatLonPoint[] t = trace.get(day_of_week+"-"+day);
					if(t == null) {
						t = new LatLonPoint[24];
						trace.put(day_of_week+"-"+day, t);
					}
					t[h] = point;
					break;
				}
			}
		}
		br.close();
		
		if(VERBOSE) {
			System.out.println("\ntrace");
			for(String k: trace.keySet()) {
				System.out.print(k);
				for(LatLonPoint p: trace.get(k)) {
					if(p==null) System.out.print(",(null)");
					else System.out.print(",("+p.getLongitude()+","+p.getLatitude()+")");
				}
				System.out.println();
			}
		}
		
		
		computeAccuracy(topics,pzd,trace);
		
	}
	
	
	
	public static void computeAccuracy(Map<Integer,List<WordProb>> topics,Map<String,double[]> pzd,Map<String,LatLonPoint[]> trace) {
		System.out.println("computing");
	}
}


class WordProb {
	String time1;
	double lon1, lat1;
	String time2;
	double lon2,lat2;
	double p;
	
	public WordProb(String time1, double lon1, double lat1, String time2, double lon2, double lat2, double p) {
		this.time1 = time1;
		this.lon1= lon1;
		this.lat1 = lat1;
		this.time2 = time2;
		this.lon2= lon2;
		this.lat2 = lat2;
		this.p = p;
	}
	
	public String toString() {
		return time1+": ("+lon1+","+lat1+") --> "+time2+": ("+lon2+","+lat2+") p = "+p+" ";
	}
	
	
	public static String print(List<WordProb> topic) {
		StringBuffer sb = new StringBuffer();
		for(WordProb wp : topic) {
			sb.append(wp+" ");
		}
		return sb.toString();
	}
}
