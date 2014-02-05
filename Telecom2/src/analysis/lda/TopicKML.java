package analysis.lda;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import utils.FileUtils;
import visual.kml.KML;
import visual.kml.KMLArrowCurved;

public class TopicKML {
	 public static void main(String[] args) throws Exception {
		 File maind = FileUtils.getFile("Topic");
		for(File d: maind.listFiles()) {
			System.out.println("Processing user "+d.getName()+" ...");
			processUser(d.getName());
		}
	    	
	    System.out.println("Done!");
	 }
	 
	 
	 static final double PROB = 0.2;
	 public static void processUser(String user) throws Exception {
		 
		 PrintWriter out = FileUtils.getPW("Topic/"+user, "topics.kml");
		 KML kml = new KML();
		 kml.printHeaderDocument(out, user);
		 
		
		
		 BufferedReader br = FileUtils.getBR("Topic/"+user+"/p_z_d.txt");
		 String line;
		 
		 //Fri-2013-06-05,0,0,1,0,0,0
		 //Fri-2013-06-12,0,0,0,0,1,0
		 
		 Map<String,Integer> days = new HashMap<String,Integer>();
		 days.put("Mon", 0);
		 days.put("Tue", 1);
		 days.put("Wed", 2);
		 days.put("Thu", 3);
		 days.put("Fri", 4);
		 days.put("Sat", 5);
		 days.put("Sun", 6);
		 
		 Map<String,double[]> map = null;
		 while((line=br.readLine()) != null) {
			 String[] e = line.split(",");
		
			 if(map == null) {
				// init;
				 map = new HashMap<String,double[]>();
				 for(int i=0; i<e.length-1;i++)
					 map.put("Topic_"+i, new double[7]);
			 }
			 
			 int index = days.get(e[0].subSequence(0, 3));
			 for(int i=0; i<e.length-1;i++) {
				  map.get("Topic_"+i)[index] += Double.parseDouble(e[i+1]);
			 } 
		 }
		 br.close();
		 
		 // normalize
		 for(String k: map.keySet()) {
			 double[] v = map.get(k);
			 double sum = 0;
			 for(int i=0; i<v.length; i++)
				 sum += v[i];
			 for(int i=0; i<v.length; i++)
				 v[i] = v[i] / sum;
		 }
		 
		 
		 
		 br = FileUtils.getBR("Topic/"+user+"/p_w_z.txt");
		 // Topic_0,n,7.645,45.0805-a,7.645,45.0805,0.47,n,7.645,45.0805-m,7.726,45.1091,0.18,m,7.726,45.1091-m,7.6044,45.0805,0.18,m,7.6044,45.0805-e,7.645,45.0805,0.18
		 while((line=br.readLine()) != null) {
			 String[] e = line.split(",|-");
			 
			 if(e.length < 7 || Double.parseDouble(e[7]) < PROB) continue;
			 
			 kml.printFolder(out, e[0]);
			 for(int i=1; i<e.length;i=i+7) {
				 
				 String h1 = e[i];
				 double lon1 = Double.parseDouble(e[i+1]);
				 double lat1 = Double.parseDouble(e[i+2]);
				 
				 String h2 = e[i+3]; 
				 double lon2 = Double.parseDouble(e[i+4]);
				 double lat2 = Double.parseDouble(e[i+5]);
				 
				 double prob = Double.parseDouble(e[i+6]);
				 
				 if(prob < PROB) break;
				 
				 out.println(getKml(h1,lon1,lat1,h2,lon2,lat2,prob,map.get(e[0])));
			 }
			 kml.closeFolder(out);
		 }
		 br.close();
		 
		 
		 kml.printFooterDocument(out);
		 out.close();
	 }
	 
	 private static final DecimalFormat DF = new DecimalFormat("0.00",new DecimalFormatSymbols(Locale.US));
	 private static final String[] DAYS = new String[]{"Mon","Tue","Wed","Thu","Fri","Sat","Sun"};
	 private static String getKml(String h1, double lon1, double lat1, String h2, double lon2, double lat2, double prob, double[] dist) {
		 
		 StringBuffer sb = new StringBuffer();
		 
		 sb.append("<description>");
		 for(int i=0; i<dist.length;i++)
		 sb.append(DAYS[i]+"="+DF.format(dist[i])+", ");
		 sb.append("</description>");
		 
		 
		 sb.append("<Style id=\""+h1+prob+"\">");
		 sb.append("<IconStyle>");
		 sb.append("<scale>"+10*prob+"</scale>");
		 sb.append("<Icon><href>http://maps.google.com/mapfiles/kml/paddle/"+h1.toUpperCase()+".png</href></Icon>");
		 sb.append("</IconStyle>");
		 sb.append("</Style>");
		 
		 sb.append("<Style id=\""+h2+prob+"\">");
		 sb.append("<IconStyle>");
		 sb.append("<scale>"+10*prob+"</scale>");
		 sb.append("<Icon><href>http://maps.google.com/mapfiles/kml/paddle/"+h2.toUpperCase()+".png</href></Icon>");
		 sb.append("</IconStyle>");
		 sb.append("</Style>");
		 
		 double jitter = 0.1 - 0.05*Math.random();
		 
		 sb.append("<Placemark>");
		 sb.append("<styleUrl>#"+h1+prob+"</styleUrl>");
		 sb.append("<Point>");
		 sb.append("<coordinates>"+lon1+jitter+","+lat1+jitter+",0</coordinates>");
		 sb.append("</Point>");
		 sb.append("</Placemark>");
		 
		 
		 sb.append("<Placemark>");
		 sb.append("<styleUrl>#"+h2+prob+"</styleUrl>");
		 sb.append("<Point>");
		 sb.append("<coordinates>"+lon2+jitter+","+lat2+jitter+",0</coordinates>");
		 sb.append("</Point>");
		 sb.append("</Placemark>");
		 
		 sb.append(KMLArrowCurved.printArrow(lon1, lat1, lon2, lat2, (int)(50*prob), "ff000000", 1, true));
		 
		 
		 return sb.toString();
	 }
	 
	 
	 
		
	 
}
