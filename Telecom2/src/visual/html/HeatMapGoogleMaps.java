package visual.html;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import utils.Config;
import utils.Logger;

public class HeatMapGoogleMaps {
	
	static double radius = 100;
	static double opacity = 0.6;
	static int zoom = 13;
	
	public static void draw(String file, String title, List<double[]> points, List<Double> weights) throws Exception {
		
		
		// compute center point
		double[] cp = new double[2];
		for(double[] p: points) {
			cp[0] += p[0];
			cp[1] += p[1];
		}
		cp[0] = cp[0] / points.size();
		cp[1] = cp[1] / points.size();
		
		
		PrintWriter out = new PrintWriter(new FileWriter(file));
		
		out.println("<html>");
		out.println("<head>");
		
		out.println("<style type=\"text/css\">");
		out.println("html { height: 100% }");
		out.println("body { height: 100%; margin: 0; padding: 0 }");
		out.println("#map-canvas { height: 100% }");
		out.println("p { margin: 0; padding: 0; background-color: #000000; color: #ffffff");
		out.println("}");
		out.println("</style>");
		
		out.println("<script src=\"https://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false&libraries=visualization\"></script>");
		out.println("<script type=\"text/javascript\">");
		out.println("var map;");
		out.println("var heatMapData = [");
		
		for(int i=0; i<points.size();i++) {
			out.print("{location: new google.maps.LatLng("+points.get(i)[0]+", "+points.get(i)[1]+"), weight: "+weights.get(i)+"}");
			if(i < points.size()-1) out.println(",");
			else out.println();
		}
		out.println("];");

		out.println("var cp = new google.maps.LatLng("+cp[0]+", "+cp[1]+");");

		out.println("function initialize() {");
		
		out.println("map = new google.maps.Map(document.getElementById('map-canvas'), {");
		out.println("center: cp,");
		out.println("zoom: "+zoom+",");
		out.println("mapTypeId: google.maps.MapTypeId.NORMAL");
		out.println("});");

		out.println("var heatmap = new google.maps.visualization.HeatmapLayer({");
		out.println("data: heatMapData,");
		out.println("radius: "+radius+",");
		out.println("opacity: "+opacity);
		out.println("});");
		out.println("heatmap.setMap(map);");
		
		out.println("}");
		
		out.println("google.maps.event.addDomListener(window, 'load', initialize);");
		out.println("</script>");
		out.println("<body>");
		out.println("<p>"+title+"</p>");
		out.println("<div id=\"map-canvas\"/>");
		out.println("</body>");
		out.println("</html>");
	
		out.close();
	}
	
	
	public static void main(String[] args) throws Exception {
		
		List<double[]> points = new ArrayList<double[]>();
		List<Double> weights = new ArrayList<Double>();
		
		points.add(new double[]{37.785, -122.437});
		weights.add(2.0);
		
		points.add(new double[]{37.785, -122.435});
		weights.add(4.0);
		
		draw(Config.getInstance().base_dir+"/heatmap.html","Heat Map Example",points,weights);
		Logger.log("Done!");
	}
	
}
