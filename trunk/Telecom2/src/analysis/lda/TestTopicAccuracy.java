package analysis.lda;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import utils.FileUtils;

public class TestTopicAccuracy {
	public static void main(String[] args) throws Exception {
		process("4a783b4759a83ac53e448ca5dd24294bea7e715876c69bcb552e873ae568e");
		System.out.println("Done!");
	}
	
	
	public static void process(String user) throws Exception  {
		
		// read p_w_z ****************************************************************************************************
		BufferedReader br = FileUtils.getBR("Topics/"+user+"/p_w_z.txt");
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
		
		for(int t: topics.keySet()) 
			System.out.println(WordProb.print(topics.get(t)));
		
		
		// read p_z_t
		Map<String,double[]> pzd = new TreeMap<String,double[]>();
		br = FileUtils.getBR("Topics/"+user+"/p_z_d.txt");
		while((line = br.readLine()) != null) {
			String[] e = line.split(",");
			String day = e[0];
			double[] p = new double[e.length-1];
			for(int i=0; i<p.length;i++)
				p[i] = Double.parseDouble(e[i+1]);
			pzd.put(day, p);
		}
		br.close();
		
		for(String day: pzd.keySet()) {
			System.out.print(day);
			for(double p: pzd.get(day))
				System.out.print(", "+p);
			System.out.println();
		}
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
