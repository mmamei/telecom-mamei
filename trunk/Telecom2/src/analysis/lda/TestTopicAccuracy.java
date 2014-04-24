package analysis.lda;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import network.NetworkMap;
import network.NetworkMapFactory;
import utils.Config;
import utils.FileUtils;

public class TestTopicAccuracy {
	
	
	public static final boolean VERBOSE = false;
	static final DecimalFormat F = new DecimalFormat("#.##",new DecimalFormatSymbols(Locale.US));
	static NetworkMap nm = NetworkMapFactory.getNetworkMap(Config.getInstance().pls_folder);
	
	public static void main(String[] args) throws Exception  {
		
		File dir = FileUtils.createDir("BASE/Topic");
		PrintWriter pw = new PrintWriter(new FileWriter(dir+"/accuracy.csv"));
		
		int cont = 0;
		File maind = FileUtils.getFile("Topic");
		File[] dirs = maind.listFiles();
		for(File d: dirs) {
			try {
				double[] x = process(d.getName());
				pw.println(F.format(x[0])+","+F.format(x[1]));
			} catch(Exception e) {
			}
			cont ++;
			if(cont % 100 == 0) System.out.println(cont +" out of "+dirs.length);
		}
		pw.close();
		System.out.println("Done!");
	}
	
	
	public static double[] process(String user) throws Exception  {
		
		// read p_w_z ****************************************************************************************************
		BufferedReader br = new BufferedReader(new FileReader(FileUtils.getFile("BASE/Topic/"+user+"/p_w_z.txt")));
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
		br = new BufferedReader(new FileReader(FileUtils.getFile("BASE/Topic/"+user+"/p_z_d.txt")));
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
		
		Map<String,Set<String>> trace = new TreeMap<String,Set<String>>();
		br = new BufferedReader(new FileReader(FileUtils.getFile("BASE/Topic/"+user+"/"+user+".txt")));
		while((line=br.readLine())!=null) {
			String[] e = line.split(" |\\t");
			String day = e[0];
			
			Set<String> t = trace.get(day);
			if(t == null) {
				t = new HashSet<String>();
				trace.put(day, t);
			}
		
			for(int i=2; i<e.length;i++) {
				t.add(e[i].toLowerCase());
			}
			
		}
		br.close();
		
		if(VERBOSE) {
			System.out.println("\ntrace");
			for(String k: trace.keySet()) {
				System.out.print(k);
				for(String w: trace.get(k)) {
					System.out.print(","+w);
				}
				System.out.println();
			}
		}
		
		
		return computeAccuracy(topics,pzd,trace);
		
	}
	
	
	
	public static double[] computeAccuracy(Map<Integer,List<WordProb>> topics,Map<String,double[]> pzd,Map<String,Set<String>> trace) {
		double cont1 = 0, cont2 = 0;
		double size1 = 0; int size2 = 0;
		
		for(String day: trace.keySet()) {
			double[] p = pzd.get(day);
			if(p == null) continue;
			
			// get maximum topic
			int maxi = 0;
			for(int i=1; i<p.length;i++)
				if(p[maxi] < p[i]) maxi = i;
			List<WordProb> topic = topics.get(maxi);
			
			
			Set<String> mov = trace.get(day);
			
			Set<String> topWords = new HashSet<String>();
			for(WordProb wp : topic) {
				if(wp.p > 0.1) topWords.add(wp.toString());
			}
			
			
			for(String m: mov)
				if(topWords.contains(m)) cont1++;
			for(String m: topWords)
				if(mov.contains(m)) cont2++;
			
			size1 += mov.size();
			size2 += topWords.size();
		}
		
		//System.out.println(cont1+"/"+size1);
		//System.out.println(cont2+"/"+size2);
		return new double[]{cont1/size1,cont2/size2};
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
		return time1+","+lon1+","+lat1+"-"+time2+","+lon2+","+lat2;
	}
	
	
	public static String print(List<WordProb> topic) {
		StringBuffer sb = new StringBuffer();
		for(WordProb wp : topic) {
			sb.append(wp+" ");
		}
		return sb.toString();
	}
}
