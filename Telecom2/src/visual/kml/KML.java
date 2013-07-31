package visual.kml;

import java.io.PrintWriter;


public class KML {
	
	public static String MRTYP_LOGO = "http://pervasive2.morselli.unimo.it/~laura/images/logo_mrtyp.png";
	
	public void closeFolder(PrintWriter out){
		try{
			out.println("</Folder>");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/*
	 * print the header of a kml file
	 */
	public void printHeaderDocument(PrintWriter out, String name) {
		try{
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			out.println("<kml xmlns=\"http://earth.google.com/kml/2.2\">");
			out.println("<Document>");
			out.println("<name>"+name+"</name>");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public void printHeaderFolder(PrintWriter out, String name) {
		try{
			out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			out.println("<kml xmlns=\"http://earth.google.com/kml/2.2\">");
			out.println("<Folder>");
			out.println("<name>"+name+"</name>");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void printFolder(PrintWriter out, String name){
		try{
			out.println("<Folder>");
			out.println("<name>"+name+"</name>");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/*
	 * print the footer of a given kml file
	 */
	public void printFooterDocument(PrintWriter out) {
		try{
			out.println("</Document>");
			out.println("</kml>");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void printFooterFolder(PrintWriter out) {
		try{
			out.println("</Folder>");
			out.println("</kml>");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/*
	 * print mr.typ logo
	 */
	public void printMrTyp(PrintWriter out) {
		
		printScreenOverlay(out, MRTYP_LOGO, "MR.TYP");
		
	}
	
	/*
	 * print ScreenOverlay on the lowerleft
	 */
	public void printScreenOverlay(PrintWriter out, String image, String name) {
		try{
			out.write("<ScreenOverlay>\n");
			out.write("<name>"+name+"</name>\n");
			out.write("<Icon>\n");
			out.write("<href>"+image+"</href>\n");
			out.write("</Icon>\n");
			out.write("<overlayXY x='0.02' y='0.050' xunits='fraction' yunits='fraction'/>\n");
			out.write("<screenXY x='0.02' y='0.050' xunits='fraction' yunits='fraction'/>\n");
			out.write("<rotationXY x='0.0' y='0.0' xunits='fraction' yunits='fraction'/>\n");
			out.write("<size x='0.27' y='0.0' xunits='fraction' yunits='fraction'/>\n");
			out.write("</ScreenOverlay>\n");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/*
	 * print ScreenOverlay on the lowerleft
	 */
	public void printScreenOverlayLowerRight(PrintWriter out, String image, String name) {
		try{
			out.write("<ScreenOverlay>\n");
			out.write("<name>"+name+"</name>\n");
			out.write("<Icon>\n");
			out.write("<href>"+image+"</href>\n");
			out.write("</Icon>\n");
			out.write("<overlayXY x='0.980' y='0.050' xunits='fraction' yunits='fraction'/>\n");
			out.write("<screenXY x='0.980' y='0.050' xunits='fraction' yunits='fraction'/>\n");
			out.write("<rotationXY x='0.0' y='0.0' xunits='fraction' yunits='fraction'/>\n");
			out.write("<size x='0.02' y='0.0' xunits='fraction' yunits='fraction'/>\n");
			out.write("</ScreenOverlay>\n");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public void printStyle(PrintWriter out, String styleName, String styleUrl){
		out.write("<Style id=\""+styleName+"\">");
		out.write("<IconStyle>");
		out.write("<scale>1.1</scale>");
		out.write("<Icon>");
		out.write("<href>"+styleUrl+"</href>");
		out.write("</Icon>");
		out.write("<hotSpot x=\"32\" y=\"1\" xunits=\"pixels\" yunits=\"pixels\"/>");
		out.write("</IconStyle>");
		out.write("</Style>");
	}
}
