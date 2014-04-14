package pls_parser;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import network.NetworkCell;
import network.NetworkMap;
import network.NetworkMapFactory;
import utils.Colors;
import utils.Config;
import utils.FileUtils;
import utils.GeomUtils;
import utils.Logger;
import visual.kml.KML;
import area.region.Region;
import area.region.RegionMap;



public class AnalyzePLSBBox extends BufferAnalyzer {
	
	SimpleDateFormat f = new SimpleDateFormat("yyyy/MMM/dd",Locale.US);
	String name;
	NetworkMap nm = NetworkMapFactory.getNetworkMap();
	
	
	Map<String,NetworkCell> cells = new HashMap<String,NetworkCell>();

	
	public AnalyzePLSBBox() {
		super();
		Config.getInstance().pls_folder="C:/DATASET/PLS/file_pls/2013/file_pls_ve";
		name =  Config.getInstance().pls_folder.replaceAll("/|:", "_");
		
		AnalyzePLSCoverage apc = new AnalyzePLSCoverage();
		Map<String,String> coverage = apc.compute();
		String first = coverage.keySet().iterator().next();
		first = first.substring(first.indexOf("-")+1);
		
		try {
			Config.getInstance().pls_start_time.setTime(f.parse(first));
			
			Config.getInstance().pls_end_time = Config.getInstance().pls_start_time;
			Config.getInstance().pls_end_time.add(Calendar.DAY_OF_YEAR, 20);
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	

	public void analyze(String line) {
		String celllac = line.split("\t")[2];
		NetworkCell c = nm.get(Long.parseLong(celllac));
		if(c!=null) 
			cells.put(c.getCellName(), c);			
	}
	
	
	public void finish() {
		try{
			System.out.println("N. Cells = "+cells.size());
	
			RegionMap rm = new RegionMap(name);
			for(NetworkCell nc : cells.values()) 
				rm.add(new Region(nc.getCellName(),GeomUtils.getCircle(nc.getBarycentreLongitude(), nc.getBarycentreLatitude(), nc.getRadius())));
			
			
			KML kml = new KML();
			File dir = FileUtils.createDir("BASE/RegionMap");
			PrintWriter out = new PrintWriter(new FileWriter(dir+"/"+name+".kml"));
			
			kml.printHeaderDocument(out, name);
			out.println(rm.toKml(Colors.RANDOM_COLORS[0]));
			kml.printFooterDocument(out);
			
			
			out.close();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	public static void main(String[] args) throws Exception {
		AnalyzePLSBBox ba = new AnalyzePLSBBox();
		ba.run();
		Logger.logln("Done!");
	}	
	
}
