package analysis.lda;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import utils.Config;
import visual.r.RPlotter;
import analysis.lda.bow.Bow;


	
public class TestTopicAccuracy {
	
	
	public static final boolean VERBOSE = false;
	
	public static void main(String[] args) {
		Bow bow = Bow.getInstance(CreateBagOfWords.BOW_KIND);
		File dir = new File(Config.getInstance().base_folder+"/Topic");
		dir.mkdirs();
	
		DescriptiveStatistics stat1 = new DescriptiveStatistics();
		DescriptiveStatistics stat2 = new DescriptiveStatistics();
		
		int cont = 0;
		File maind = new File(Config.getInstance().base_folder+"/Topic");
		File[] dirs = maind.listFiles();
		for(File d: dirs) {
			try {
				double[] x = process(d.getName(),bow);
				if(VERBOSE) System.exit(0);
				
				stat1.addValue(x[0]);
				stat2.addValue(x[1]);

			} catch(Exception e) {
				e.printStackTrace();
				if(VERBOSE) System.exit(0);
			}
			cont ++;
			if(cont % 100 == 0) System.out.println(cont +" out of "+dirs.length);
		}
		
		
	   double[] p = new double[100];
	   double[] cdf1 = new double[100];
	   double[] cdf2 = new double[100];
	   
	   
	   for(int i=1;i<=100;i++) {
		   p[i-1] = (double)i/100;
		   cdf1[i-1] = stat1.getPercentile(i);
		   cdf2[i-1] = stat2.getPercentile(i);
	   }
	   
	   RPlotter.drawScatter(cdf1, p, "w in trace and topic / w in trace", "cdf", Config.getInstance().base_folder+"/Images/TopicAccuracy.pdf", "geom_line()");	
	   System.out.println("Done!");
	}
	
	
	public static double[] process(String user,Bow bow) throws Exception  {
		
		if(VERBOSE)
			System.out.println(user);
		
		// read p_w_z ****************************************************************************************************
		BufferedReader br = new BufferedReader(new FileReader(new File(Config.getInstance().base_folder+"/Topic/"+user+"/p_w_z.txt")));
		String line;
		// Topic_0,n,7.666,45.0713-n,7.6529,45.055,0.27,n,7.6529,45.055-n,7.666,45.0713,0.2,m,7.6529,45.055-e,7.6587,45.0707,0.13,m,7.666,45.0713-a,7.6529,45.055,0.07
		Map<Integer,List<Entry<String,Double>>> pwz = new TreeMap<Integer,List<Entry<String,Double>>>();
		while((line = br.readLine()) != null) {
			List<Map.Entry<String,Double>> topic = bow.parsePWZ(line);
			String[] e = line.split(",|-");
			int topic_index = Integer.parseInt(e[0].split("_")[1]);
			pwz.put(topic_index, topic);
		}
		br.close();
		
		if(VERBOSE) {
			System.out.println("\npwz");
			for(int k: pwz.keySet()) {
				System.out.print("topic"+k);
				List<Map.Entry<String,Double>> topic = pwz.get(k);
				for(int i=0; i<topic.size() && i<5;i++) {
					Entry<String,Double> wp = topic.get(i);
					System.out.print(" ("+wp.getKey()+","+wp.getValue()+")");
				}
				System.out.println();
			}
		}
	
		
		// read p_z_d *******************************************************************************************************
		Map<String,double[]> pzd = new TreeMap<String,double[]>();
		br = new BufferedReader(new FileReader(new File(Config.getInstance().base_folder+"/Topic/"+user+"/p_z_d.txt")));
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
			System.out.println("\npzd");
			for(String day: pzd.keySet()) {
				System.out.print(day);
				for(double p: pzd.get(day))
					System.out.print(", "+p);
				System.out.println();
			}
		}
		
		// read the user trace ************************************************************************************************
		
		Map<String,Set<String>> trace = new TreeMap<String,Set<String>>();
		br = new BufferedReader(new FileReader(new File(Config.getInstance().base_folder+"/Topic/"+user+"/"+user+".txt")));
		while((line=br.readLine())!=null) {
			String[] e = line.split(" |\\t");
			String day = e[0];
			
			Set<String> t = trace.get(day);
			if(t == null) {
				t = new HashSet<String>();
				trace.put(day, t);
			}
		
			for(int i=2; i<e.length;i++) {
				t.add(e[i].replaceAll("-", ","));
			}
			
		}
		br.close();
		
		if(VERBOSE) {
			System.out.println("\ntrace");
			for(String k: trace.keySet()) {
				System.out.print(k);
				for(String w: trace.get(k)) {
					System.out.print(" "+w);
				}
				System.out.println();
			}
		}
		
		
		return computeAccuracy(pwz,pzd,trace);
		
	}
	
	
	
	public static double[] computeAccuracy(Map<Integer,List<Entry<String,Double>>> pwz,Map<String,double[]> pzd,Map<String,Set<String>> trace) {
		double w_in_trace_and_topic = 0;
		double size1 = 0; int size2 = 0;
		
		for(String day: trace.keySet()) {
			double[] p = pzd.get(day);
			if(p == null) continue;
			
			
			
			
			// hour --> word --> number of repetitions
			Map<String,Map<String,Integer>> mapx = new HashMap<String,Map<String,Integer>>();
			for(String w: trace.get(day)) {
				String h = w.split(",")[0];
				Map<String,Integer> x = mapx.get(h);
				if(x==null) {
					x = new HashMap<String,Integer>();
					mapx.put(h, x);
				}
				Integer cont = x.get(w);
				if(cont == null) cont = 0;
				x.put(w, cont + 1);
			}
			
			/*
			// map associating for each hour the most frequent word in the user trace for that hour.
			Map<String,List<String>> mov = new HashMap<String,List<String>>();
			for(String h: mapx.keySet()) {
				Map<String,Integer> x = mapx.get(h);
				String mfw = null; // most frequent word
				for(String w:  x.keySet())
					if(mfw == null || x.get(mfw) < x.get(w)) mfw = w;
				List<String> words = new ArrayList<String>();
				words.add(mfw);
				mov.put(h, words);
			}
			*/
			
			// map associating for each hour all the words generated by the user in that hour
			Map<String,List<String>> mov = new HashMap<String,List<String>>();
			for(String h: mapx.keySet()) {
				Map<String,Integer> x = mapx.get(h);
				List<String> words = new ArrayList<String>();
				for(String w:  x.keySet())
					words.add(w);
				mov.put(h, words);
			}
			
			
			
			// map associating for each hour the top word in the topics (pwd) for that hour
			Map<String,Entry<String,Double>> topw = new HashMap<String,Entry<String,Double>>();
			Map<String,Double> pwd = ComputePWD.pwd(pzd,pwz);
			for(String word: pwd.keySet()) {
				double prob = pwd.get(word);
				String tword = word.split(";")[1];
				String h = tword.split(",")[0];
				Entry<String,Double> wp = topw.get(h);
				if(wp == null || wp.getValue() < prob) topw.put(h, new SimpleEntry<String,Double>(tword,prob));
			}
			
			
			
			
			
			if(VERBOSE) {
				System.out.print("TRACE = ");
				for(List<String> w: mov.values())
					System.out.print(w+" ");
				System.out.println();
				System.out.print("TOPIC = ");
				for(Entry<String,Double> wp : topw.values())
					System.out.print(wp.getKey()+" ");
				System.out.println();
			}
			
			
			
			for(String h: mov.keySet()) {
				List<String> trace_w = mov.get(h);
				Entry<String,Double> topic_wp = topw.get(h);
				if(topic_wp !=null) {
					String topic_w = topic_wp.getKey();
					if(trace_w.contains(topic_w)) w_in_trace_and_topic++;
				}
			}
			
			
			size1 += mov.size();
			size2 += topw.size();
		}
		
		if(VERBOSE) {
			System.out.println(w_in_trace_and_topic+"/"+size1);
			System.out.println(w_in_trace_and_topic+"/"+size2);
		}
		return new double[]{w_in_trace_and_topic/size1,w_in_trace_and_topic/size2};
	}
}