package visual;

import java.io.FileWriter;
import java.io.PrintWriter;

import utils.Config;
import utils.Logger;

public class HeatMapGoogleMaps {
	
	static double radius = 30;
	static double opacity = 0.8;
	
	public static void draw(String file, String title) throws Exception {
		PrintWriter out = new PrintWriter(new FileWriter(file));
		
		out.println("<html>");
		out.println("<head>");
		out.println("<style type=\"text/css\">");
		out.println("	  html { height: 100% }");
		out.println("      body { height: 100%; margin: 0; padding: 0 }");
		out.println("      #map-canvas { height: 100% }");
		out.println("	  p { margin: 0; padding: 0; background-color: #000000; color: #ffffff");
		out.println("}");
		out.println("</style>");
		out.println("<script src=\"https://maps.googleapis.com/maps/api/js?v=3.exp&sensor=false&libraries=visualization\"></script>");
		out.println("<script type=\"text/javascript\">");
		out.println("var map;");
		out.println("var heatMapData = [");
		out.println("{location: new google.maps.LatLng(37.785, -122.437), weight: 2},");
		out.println("{location: new google.maps.LatLng(37.785, -122.435), weight: 3}");
		out.println("];");

		out.println("var cp = new google.maps.LatLng(37.774546, -122.433523);");

		out.println("function initialize() {");
		
		out.println("map = new google.maps.Map(document.getElementById('map-canvas'), {");
		out.println("center: cp,");
		out.println("zoom: 13,");
		out.println("mapTypeId: google.maps.MapTypeId.SATELLITE");
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
		draw(Config.getInstance().base_dir+"/heatmap.html","Heat Map Example");
		Logger.log("Done!");
	}
	
}
