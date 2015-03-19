package region;


import java.io.File;
import java.io.FileReader;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;

/*
 * This parser parses the kml files describing the italian comuni taken from:
 * http://localmapping.wordpress.com/2008/11/20/i-confini-amministrativi-degli-8101-comuni-ditalia-al-2001/
 */

public class CreatorRegionMapComuni {
	
	public static final String xpath_name = "kml/Document/Folder[1]/Placemark/name";
	public static final String xpath_coor = "kml/Document/Folder/Placemark/Polygon/outerBoundaryIs/LinearRing/coordinates";
	
	
	public static void main(String[] args) throws Exception {
		String region = "Veneto";
		String input_kml_file="C:/DATASET/GEO/"+region+".kml";
		String output_obj_file=Config.getInstance().base_folder+"/RegionMap/FIX_"+region+".ser";
		process(region, input_kml_file,output_obj_file);
		Logger.logln("Done!");
	}
	
	
	public static RegionMap process(String name, String input_kml_file, String output_obj_file) throws Exception {
		
		RegionMap rm = new RegionMap(name);
		
		InputSource docIS = new InputSource(new FileReader(new File(input_kml_file)));
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(docIS);
		NodeList n = process(doc,xpath_name);
		NodeList c = process(doc,xpath_coor);
		
		for(int i=0; i<n.getLength(); i++) {
			
			String comune_name = n.item(i).getTextContent();
			comune_name = comune_name.replaceAll("'", "\\\\'");
			
			String coordinates = c.item(i).getTextContent();
			
			rm.add(new Region(comune_name,coordinates));
		}
		
		CopyAndSerializationUtils.save(new File(output_obj_file), rm);
		return rm;
	}
	
	
	private static NodeList process(Document doc, String xpath) {
		XPathFactory factory = XPathFactory.newInstance();
		NodeList result = null;
		
		XPath xp = factory.newXPath();
		try {
			result = (NodeList)xp.evaluate(xpath, doc, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return result;
	}
}
