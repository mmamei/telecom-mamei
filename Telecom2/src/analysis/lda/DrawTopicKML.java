package analysis.lda;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import utils.Config;
import visual.kml.KML;
import analysis.lda.bow.Bow;
import analysis.lda.bow.OneDocXDay;

public class DrawTopicKML {
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
	 
	 
	 static final double PROB = 0.1;
	 public static void processUser(String user,Bow bow) throws Exception {
		 
		 File dir = new File(Config.getInstance().base_folder+"/Topic/"+user);
		 
		 PrintWriter out = new PrintWriter(new FileWriter(dir+"/topics.kml"));
		 KML kml = new KML();
		 kml.printHeaderDocument(out, user);
		 
		 BufferedReader br = new BufferedReader(new FileReader(dir+"/p_z_d.txt"));
		 
	
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
		 
		 br = new BufferedReader(new FileReader(dir+"/p_w_z.txt"));
		 // Topic_0,n,7.645,45.0805-a,7.645,45.0805,0.47,n,7.645,45.0805-m,7.726,45.1091,0.18,m,7.726,45.1091-m,7.6044,45.0805,0.18,m,7.6044,45.0805-e,7.645,45.0805,0.18
		 while((line=br.readLine()) != null) {
			 String[] e = line.split(",|-");
			 
			 kml.printFolder(out, e[0]);
			 
			 out.println("<description>");
			 out.println(getGraph(e[0],map.get(e[0])));
			 out.println("</description>");
			 
			 
			 List<Entry<String,Double>> l = bow.parsePWZ(line);
			 for(Entry<String,Double> wp: l)
				 out.println(bow.word2KML(wp));
			 
			 kml.closeFolder(out);
		 }
		 br.close(); 
		 kml.printFooterDocument(out);
		 out.flush();
		 out.close();
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
