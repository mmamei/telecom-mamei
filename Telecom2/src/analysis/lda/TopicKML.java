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
				 if(Double.parseDouble(e[i+1]) > 0.4)
				  map.get("Topic_"+i)[index] ++ ;
			 } 
		 }
		 br.close();
		 
		  
		 br = FileUtils.getBR("Topic/"+user+"/p_w_z.txt");
		 // Topic_0,n,7.645,45.0805-a,7.645,45.0805,0.47,n,7.645,45.0805-m,7.726,45.1091,0.18,m,7.726,45.1091-m,7.6044,45.0805,0.18,m,7.6044,45.0805-e,7.645,45.0805,0.18
		 while((line=br.readLine()) != null) {
			 String[] e = line.split(",|-");
			 
			 if(e.length < 7 || Double.parseDouble(e[7]) < PROB) continue;
			 
			 kml.printFolder(out, e[0]);
			 
			 out.println("<description>");
			 out.println(getGraph(e[0],map.get(e[0])));
			 out.println("</description>");
			 
			 for(int i=1; i<e.length;i=i+7) {
				 
				 String h1 = e[i];
				 double lon1 = Double.parseDouble(e[i+1]);
				 double lat1 = Double.parseDouble(e[i+2]);
				 
				 String h2 = e[i+3]; 
				 double lon2 = Double.parseDouble(e[i+4]);
				 double lat2 = Double.parseDouble(e[i+5]);
				 
				 double prob = Double.parseDouble(e[i+6]);
				 
				 if(prob < PROB) break;
				 
				 out.println(getKml(h1,lon1,lat1,h2,lon2,lat2,prob));
			 }
			 kml.closeFolder(out);
		 }
		 br.close();
		 
		 
		 kml.printFooterDocument(out);
		 out.close();
	 }
	 
	 private static String getKml(String h1, double lon1, double lat1, String h2, double lon2, double lat2, double prob) {
		 
		 StringBuffer sb = new StringBuffer();
		 
		  
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
		 
		 double jitter = 0.001 - 0.0005 * Math.random();
		 
		 lon1 = lon1 + jitter;
		 lat1 = lat1 + jitter;
		 lon2 = lon2 + jitter;
		 lat2 = lat2 + jitter;
		 
		 sb.append("<Placemark>");
		 sb.append("<styleUrl>#"+h1+prob+"</styleUrl>");
		 sb.append("<Point>");
		 sb.append("<coordinates>"+lon1+","+lat1+",0</coordinates>");
		 sb.append("</Point>");
		 sb.append("</Placemark>");
		 
		 
		 sb.append("<Placemark>");
		 sb.append("<styleUrl>#"+h2+prob+"</styleUrl>");
		 sb.append("<Point>");
		 sb.append("<coordinates>"+lon2+","+lat2+",0</coordinates>");
		 sb.append("</Point>");
		 sb.append("</Placemark>");
		 
		 sb.append(KMLArrowCurved.printArrow(lon1, lat1, lon2, lat2, (int)(50*prob), "ff000000", 0.5, true));
		 
		 
		 return sb.toString();
	 }
	 
	 
	 private static final DecimalFormat DF = new DecimalFormat("0.00",new DecimalFormatSymbols(Locale.US));
	 public static String getGraph(String topic, double[] dist) {
		 StringBuffer sb = new StringBuffer();

		 sb.append("<html>");
		 sb.append("<head>");
		 sb.append("<script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>");
		 sb.append("<script type=\"text/javascript\">");
		 sb.append("google.load('visualization', '1.0', {'packages':['corechart']});");
		 sb.append("google.setOnLoadCallback(drawChart);");
		 sb.append("function drawChart() {");
		 sb.append("var data = new google.visualization.DataTable();");
		 sb.append("data.addColumn('string', 'Days');");
		 sb.append("data.addColumn('number', 'Topic presence');");
		 sb.append("data.addRows([");
	
		 sb.append("['Mon', "+DF.format(dist[0])+"],");
		 sb.append("['Tue', "+DF.format(dist[1])+"],");
		 sb.append("['Wed', "+DF.format(dist[2])+"],");
		 sb.append("['Thu', "+DF.format(dist[3])+"],");
		 sb.append("['Fri', "+DF.format(dist[4])+"],");
		 sb.append("['Sat', "+DF.format(dist[5])+"],");
		 sb.append("['Sun', "+DF.format(dist[6])+"]");
		 
		 sb.append("]);");
		 sb.append("var options = {'title':'"+topic+"',");
		 sb.append("'width':400,");
		 sb.append("'height':300,");
		 sb.append("'legend':{position: 'none'}};");
		 sb.append("var chart = new google.visualization.ColumnChart(document.getElementById('chart_div'));");
		 sb.append("chart.draw(data, options);");
		 sb.append(" }");
		 sb.append("</script>");
		 sb.append("</head>");
		 sb.append("<body>");
		 sb.append("<div id=\"chart_div\"></div>");
		 sb.append("</body>");
		 sb.append("</html>");
		 
		 return sb.toString();
	 }
	 
	 
	 
		
	 
}
