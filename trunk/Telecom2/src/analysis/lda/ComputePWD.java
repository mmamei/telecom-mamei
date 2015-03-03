package analysis.lda;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import utils.Config;
import analysis.lda.bow.Bow;
import analysis.lda.bow.OneDocXDay;

public class ComputePWD {
	
	 public static void main(String[] args) {
		 	Bow bow = Bow.getInstance(CreateBagOfWords.BOW_KIND);
			File maind = new File(Config.getInstance().base_folder+"/Topic");
			for(File d: maind.listFiles()) {
				try {
					processUser(d.getName(),bow);
				}catch(Exception e) {
					System.out.println("Problems with user "+d.getName());
					e.printStackTrace();
				}
			}
		    	
		    System.out.println("Done!");
		 }
		 
		 
		 
		 public static void processUser(String user,Bow bow) throws Exception { 
			 File dir = new File(Config.getInstance().base_folder+"/Topic/"+user);
			 
			 Map<String,double[]> pzd = new HashMap<String,double[]>();		 
			 
			 BufferedReader br = new BufferedReader(new FileReader(dir+"/p_z_d.txt"));
			 String line;
			 while((line=br.readLine()) != null) {
				 // Fri-2013-06-21,0.18,0.32,0.32,0.18
				 String[] e = line.split(",");
				 double[] p = new double[e.length-1];
				 for(int i=0; i<p.length;i++)
					 p[i] = Double.parseDouble(e[i+1]);
				 pzd.put(e[0], p);
			 }
			 br.close();
			 
			 
			 Map<Integer,List<Entry<String,Double>>> pwz = new HashMap<Integer,List<Entry<String,Double>>>();
			 br = new BufferedReader(new FileReader(dir+"/p_w_z.txt"));
			 while((line=br.readLine()) != null) {
				 String[] e = line.split(",|-");
				 int topic_index = Integer.parseInt(e[0].split("_")[1]);
				 List<Map.Entry<String,Double>> l = bow.parsePWZ(line);
				 if(l==null) continue;
				 pwz.put(topic_index, l);
			 }
			 br.close();
			 
			 Map<String,Double> pwd = pwd(pzd,pwz);
			 
			 
			 //Map<String,Double> o = Sort.sortHashMapByValuesD(pwd, Collections.reverseOrder());
			 dir = new File(Config.getInstance().base_folder+"/TopicPWD/"+user);
			 dir.mkdirs();
			 PrintWriter out = new PrintWriter(new FileWriter(dir+"/p_w_d.txt"));
			 for(String wd: pwd.keySet())
				 out.println(wd+";"+pwd.get(wd));
			 out.close();			 
		 }
		 
		 public static  Map<String,Double> pwd(Map<String,double[]> pzd, Map<Integer,List<Entry<String,Double>>> pwz) {
			 Map<String,Double> pwd = new TreeMap<String,Double>();
			 
			 for(int topic_index: pwz.keySet()) {
				 List<Map.Entry<String,Double>> l = pwz.get(topic_index);
				 for(Map.Entry<String,Double> wp: l)
					 for(String d: pzd.keySet()) {
						 String wd = d+";"+wp.getKey();
						 Double p = pwd.get(wd);
						 if (p == null) p = 0.0;
						 pwd.put(wd, p + pzd.get(d)[topic_index] * wp.getValue());
					 }
				 }
			 return pwd;
		 }
}
