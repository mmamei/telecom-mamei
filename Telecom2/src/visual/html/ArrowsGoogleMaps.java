package visual.html;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import utils.Config;
import utils.Logger;

public class ArrowsGoogleMaps {
	
	public static int zoom = 12;
	
	public static void draw(String file,String title,List<double[][]> points,List<Double> w, List<String> colors, boolean directed) throws Exception {
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
		
		
		// compute center point
		double[] cp = new double[2];
		for(double[][] p: points) {
			cp[0] += p[0][0];
			cp[1] += p[0][1];
			
			cp[0] += p[1][0];
			cp[1] += p[1][1];
		}
		cp[0] = cp[0] / (2*points.size());
		cp[1] = cp[1] / (2*points.size());
		
		
		out.println("function initialize() {");
		
		out.println("var mapOptions = {");
		out.println("zoom: "+zoom+",");
		out.println("center: new google.maps.LatLng("+cp[0]+","+cp[1]+"),");
		out.println(" mapTypeId: google.maps.MapTypeId.TERRAIN");
		out.println("};");

		out.println("var map = new google.maps.Map(document.getElementById('map-canvas'), mapOptions);");

		out.println("var lineSymbol = {");
		out.println("path: google.maps.SymbolPath.FORWARD_CLOSED_ARROW");
		out.println("};");

		for(int i=0;i<points.size();i++) {
			drawArrow(out,points.get(i),w.get(i),colors.get(i),directed);
		}

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
	
	
	public static void drawArrow(PrintWriter out, double[][] p, double w, String color, boolean directed){
		out.println("var line = new google.maps.Polyline({");
		out.println("path: [");
		
		
		double jitter = color.equals("#0000ff") ? 0.0001 * w : 0;
		
		for(int i=0; i<p.length-1;i++)
			out.println("new google.maps.LatLng("+(p[i][0]+jitter)+","+(p[i][1]+jitter)+"),");
		out.println("new google.maps.LatLng("+(p[p.length-1][0]+jitter)+","+(p[p.length-1][1]+jitter)+")");
		out.println("],");
		
		out.println("strokeColor: '"+color+"',");
		//out.println("strokeOpacity: 0.8,");
		out.println("strokeWeight: "+w+",");
		
		if(directed) {
			out.println("icons: [{");
			out.println("icon: lineSymbol,");
			out.println("offset: '100%'");
			out.println("}],");
		}
		out.println("map: map");
		out.println("});");
	}
	
	
	public static void main(String[] args) throws Exception {
		
		List<double[][]> points = new ArrayList<double[][]>();
		List<Double> w = new ArrayList<Double>();
		List<String> colors = new ArrayList<String>();
		
		points.add(new double[][]{{22.291, 153.027},{18.291, 153.027}});
		w.add(2.0);
		colors.add("#ff0000");
		
		points.add(new double[][]{{22.291, 153.027},{18.491, 153.427}});
		w.add(3.0);
		colors.add("#00ff");
		
		draw("BASE/arrows.html","Arrow Map Example",points,w,colors,false);
		Logger.log("Done!");
	}
	
}
