package analysis.lda;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.gps.utils.LatLonPoint;
import org.gps.utils.LatLonUtils;

import utils.Config;
import utils.Sort;
import visual.r.RPlotter;


/*
 * Main routine computed by Andrea Sassi.
 * Basically it detects the two most visited places and assume the user is moving back and forth
 */

public class GroundtruthInfo {
	public static List<Info> run(String file) {
		List<Info> gt = new ArrayList<Info>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			// 046edba7d83687797b6a19d6b8b9e05194d0662fc1e25953e4bd84e486f2,POINT (45.0586 7.59479),POINT (45.07903 7.6322),28654132,81186961
			while((line=br.readLine())!=null) {
				String[] e = line.split(",");
				String user = e[0];
				String[] x = e[1].split(" ");
				LatLonPoint p1 = new LatLonPoint(Double.parseDouble(x[1].substring(1).trim()),Double.parseDouble(x[2].substring(0,x[2].length()-1).trim()));
				x = e[2].split(" ");
				LatLonPoint p2 = new LatLonPoint(Double.parseDouble(x[1].substring(1).trim()),Double.parseDouble(x[2].substring(0,x[2].length()-1).trim()));
				long time1 = Long.parseLong(e[3]);
				long time2 = Long.parseLong(e[4]);
				gt.add(new Info(user,p1,p2,time1,time2));
			}
			br.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return gt;
	}
	
	public static void userSetCreator(List<Info> gt, String file) {
		try {
			PrintWriter out = new PrintWriter(new FileWriter(file));
			for(Info i: gt)
				out.println(i.user);
			out.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	static double PROB = 0.0;
	public static void check(List<Info> gt, String bdir) {
		int cont = 0;
		
		DescriptiveStatistics stat = new DescriptiveStatistics();
		
		for(Info info: gt) {
			File dir = new File(bdir+"/"+info.user);
			String line = null;
			String[] e = null;
			int i = 0;
			if(dir.exists()) {
				try {
				BufferedReader br = new BufferedReader(new FileReader(dir+"/p_w_z.txt"));
				
				double mindh = Integer.MAX_VALUE;
				double mind = Double.MAX_VALUE;
				while((line=br.readLine()) != null) {
					 e = line.split(",|-");
					 if(e.length < 7 || Double.parseDouble(e[7]) < PROB) continue;
					 
					 double lastProb = 0;
			
					 for(i=1; i<e.length;i=i+7) {
						
						 int h1 = Integer.parseInt(e[i]);
						 int h2 = Integer.parseInt(e[i+3]); 
						 				 
						 LatLonPoint p1 = null;
						 LatLonPoint p2 = null;
						 try {
							 p1 = new LatLonPoint( Double.parseDouble(e[i+2]), Double.parseDouble(e[i+1]));
							 p2 = new LatLonPoint( Double.parseDouble(e[i+5]),Double.parseDouble(e[i+4]));
						 }catch(Exception f) {
						 }
						 
						 if(p1 == null || p2 == null) continue;
						 
						 double prob = Double.parseDouble(e[i+6]);
						 
						 if(prob < lastProb) break; // with this condition only the most probable topics are considered
						 
						 double d1 = (LatLonUtils.getHaversineDistance(p1, info.p1) + LatLonUtils.getHaversineDistance(p2, info.p2)) / 2; 
						 double d2 = (LatLonUtils.getHaversineDistance(p1, info.p2) + LatLonUtils.getHaversineDistance(p2, info.p1)) / 2; 
						 if(d1 < mind) mind = d1;
						 if(d2 < mind) mind = d2;
					 }
				}
				br.close();
				stat.addValue(mind);
				cont ++;
				}catch(Exception exc) {
					System.err.println("Problems with user = "+info.user+" *********************************************************************************************");
					System.err.println(line); 
					System.err.println(e[i-1]+","+e[i]+","+e[i+1]+"-"+e[i+2]+","+e[i+3]+","+e[i+4]+","+e[i+5]);
					System.err.println(e.length);
					exc.printStackTrace();
				}
			}
		}
		System.out.println(cont+"/"+gt.size()+" found!");
		
		
		double[] prob = new double[100];
		double[] val = new double[prob.length];
		for(int i=1; i<100;i++) {
			prob[i] = (double)i/100;
			val[i] = stat.getPercentile(i);
		}
		RPlotter.drawLine(val, prob, "distance (m)", "CDF", Config.getInstance().base_folder+"/Images/gt.pdf","scale_x_log10(limits = c(10,10000))");
	}
	
	
	public static void checkPWD(List<Info> gt, String bdir) {
		int cont = 0;
		
		DescriptiveStatistics stat = new DescriptiveStatistics();
		
		for(Info info: gt) {
			File dir = new File(bdir+"/"+info.user);
			String line = null;
			String[] e = null;
			int i = 0;
			if(dir.exists()) {
				try {
					BufferedReader br = new BufferedReader(new FileReader(dir+"/p_w_d.txt"));
				
					
				
					//Fri-2013-06-21;0,7.6296,45.0941;0.005200000000000001
					Map<String,Double> lp = new HashMap<String,Double>();
					while((line=br.readLine()) != null) {
						String[] elements = line.split(";");
						if(elements[0].startsWith("Sat") || elements[0].startsWith("Sun")) continue;
						double p = Double.parseDouble(elements[2]);
						String[] f = elements[1].split(",");
						String key = f[2]+","+f[1];
						Double prob = lp.get(key);
						if(prob == null) prob = 0.0;
						lp.put(key, prob+p);
					}
					br.close();
					
					
					double homedist = 0;
					double workdist = 0;
					LatLonPoint homep = null;
					LatLonPoint workp = null;
					
					Map<String,Double> olp = Sort.sortHashMapByValuesD(lp, Collections.reverseOrder());
					int num = 0;
					for(String k: olp.keySet()) {
						String[] latlon = k.split(",");
						LatLonPoint x = new LatLonPoint(Double.parseDouble(latlon[0]), Double.parseDouble(latlon[1]));
						if(homep == null || LatLonUtils.getHaversineDistance(x, info.p1) < homedist) {
							homep = x;
							homedist = LatLonUtils.getHaversineDistance(x, info.p1);
						}
						else if(workp == null || LatLonUtils.getHaversineDistance(x, info.p2) < workdist)  {
							workp = x;
							workdist = LatLonUtils.getHaversineDistance(x, info.p2);
						}
						if(num>10) break;
					}
					stat.addValue(homedist+workdist);
					cont ++;
				}catch(Exception exc) {
					System.err.println("Problems with user = "+info.user+" *********************************************************************************************");
					System.err.println(line); 
					System.err.println(e[i-1]+","+e[i]+","+e[i+1]+"-"+e[i+2]+","+e[i+3]+","+e[i+4]+","+e[i+5]);
					System.err.println(e.length);
					exc.printStackTrace();
				}
			}
		}
		System.out.println(cont+"/"+gt.size()+" found!");
		
		
		double[] prob = new double[100];
		double[] val = new double[prob.length];
		for(int i=1; i<100;i++) {
			prob[i] = (double)i/100;
			val[i] = stat.getPercentile(i);
		}
		RPlotter.drawLine(val, prob, "distance (m)", "CDF", Config.getInstance().base_folder+"/Images/gt.pdf","scale_x_log10(limits = c(10,10000))");
	}
	
	public static void main(String[] args) {
		List<Info> gt = run("C:/DATASET/lda-groundtruth/dailyroutes.csv");
		//userSetCreator(gt,Config.getInstance().base_folder+"/UserSetCreator/LDAPOP.csv");
		//check(gt,Config.getInstance().base_folder+"/Topic");
		checkPWD(gt,Config.getInstance().base_folder+"/TopicPWD-multi");
	}
}


class Info {
	String user;
	LatLonPoint p1;
	LatLonPoint p2;
	long time1;
	long time2;
	
	public Info(String user,LatLonPoint p1,LatLonPoint p2,long time1,long time2) {
		this.user = user;
		this.p1=p1;
		this.p2=p2;
		this.time1 = time1;
		this.time2 = time2;
	}
	
	public String toString() {
		return user+" ("+p1.getLatitude()+","+p1.getLongitude()+") <-> ("+p2.getLatitude()+","+p2.getLongitude()+") between: "+time1+" and "+time2;
	}
}
