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
import java.util.Map.Entry;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import analysis.lda.bow.Bow;
import utils.Config;
import visual.r.RPlotter;


	
public class TestTopicAccuracy {
	
	
	public static final boolean VERBOSE = false;
	
	public static void main(String[] args) throws Exception  {
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
			}
			cont ++;
			if(cont % 100 == 0) System.out.println(cont +" out of "+dirs.length);
		}
		
		
	   double[] p = new double[100];
	   double[] cdf1 = new double[100];
	   double[] cdf2 = new double[100];
	   
	   List<double[]> ps = new ArrayList<double[]>();
	   ps.add(p);
	   ps.add(p);
	   
	   List<double[]> cdf = new ArrayList<double[]>();
	   cdf.add(cdf1);
	   cdf.add(cdf2);
	   
	   List<String> names = new ArrayList<String>();
	   names.add("cdf1");
	   names.add("cdf2");
	   
	   for(int i=1;i<=100;i++) {
		   p[i-1] = (double)i/100;
		   cdf1[i-1] = stat1.getPercentile(i);
		   cdf2[i-1] = stat2.getPercentile(i);
	   }
	   
	   RPlotter.drawScatter(cdf, ps, names, "cdf", "error", "cdf", Config.getInstance().base_folder+"/Images/TopicAccuracy.pdf", "geom_line()");	
	   
		
		System.out.println("Done!");
	}
	
	
	public static double[] process(String user,Bow bow) throws Exception  {
		
		if(VERBOSE)
			System.out.println(user);
		
		// read p_w_z ****************************************************************************************************
		BufferedReader br = new BufferedReader(new FileReader(new File(Config.getInstance().base_folder+"/Topic/"+user+"/p_w_z.txt")));
		String line;
		// Topic_0,n,7.666,45.0713-n,7.6529,45.055,0.27,n,7.6529,45.055-n,7.666,45.0713,0.2,m,7.6529,45.055-e,7.6587,45.0707,0.13,m,7.666,45.0713-a,7.6529,45.055,0.07
		Map<Integer,List<Entry<String,Double>>> topics = new TreeMap<Integer,List<Entry<String,Double>>>();
		while((line = br.readLine()) != null) {
			List<Map.Entry<String,Double>> topic = bow.parsePWZ(line);
			String[] e = line.split(",|-");
			int topic_index = Integer.parseInt(e[0].split("_")[1]);
			topics.put(topic_index, topic);
		}
		br.close();
		
		if(VERBOSE) {
			System.out.println("\npwz");
			for(int k: topics.keySet()) {
				System.out.print("topic"+k);
				List<Map.Entry<String,Double>> topic = topics.get(k);
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
		
		
		return computeAccuracy(topics,pzd,trace);
		
	}
	
	
	
	public static double[] computeAccuracy(Map<Integer,List<Entry<String,Double>>> topics,Map<String,double[]> pzd,Map<String,Set<String>> trace) {
		double cont1 = 0, cont2 = 0;
		double size1 = 0; int size2 = 0;
		
		for(String day: trace.keySet()) {
			double[] p = pzd.get(day);
			if(p == null) continue;
			
			// get maximum topic
			int maxi = 0;
			for(int i=1; i<p.length;i++)
				if(p[maxi] < p[i]) maxi = i;
			List<Entry<String,Double>> topic = topics.get(maxi);
			
			
			Set<String> mov = trace.get(day);
			
			Set<String> topWords = new HashSet<String>();
			for(Entry<String,Double> wp : topic) {
				if(wp.getValue() > 0.1) topWords.add(wp.getKey());
			}
			
			
			for(String m: mov)
				if(topWords.contains(m)) cont1++;
			for(String m: topWords)
				if(mov.contains(m)) cont2++;
			
			size1 += mov.size();
			size2 += topWords.size();
		}
		
		if(VERBOSE) {
			System.out.println(cont1+"/"+size1);
			System.out.println(cont2+"/"+size2);
		}
		return new double[]{cont1/size1,cont2/size2};
	}
}