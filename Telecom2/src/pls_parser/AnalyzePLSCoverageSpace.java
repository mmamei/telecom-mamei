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

import region.Region;
import region.RegionI;
import region.RegionMap;
import region.network.NetworkCell;
import region.network.NetworkMap;
import region.network.NetworkMapFactory;
import utils.Colors;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.FileUtils;
import utils.GeomUtils;
import utils.Logger;
import visual.kml.KML;



public class AnalyzePLSCoverageSpace extends BufferAnalyzer {
	
	SimpleDateFormat f = new SimpleDateFormat("yyyy/MMM/dd",Locale.US);
	String name;
	NetworkMap nm = null;
	RegionMap rm;  
	
	Map<String,NetworkCell> cells = new HashMap<String,NetworkCell>();

	
	public AnalyzePLSCoverageSpace() {
		
	}
	
	public AnalyzePLSCoverageSpace(String plsf) {
		super();
		System.out.println("======================> "+plsf);
		plsf = plsf.replaceAll("\\\\", "/");
		Config.getInstance().pls_folder = plsf;
		name =  plsf.substring(plsf.lastIndexOf("/")+1);
		rm = new RegionMap(plsf);
		AnalyzePLSCoverageTime apc = new AnalyzePLSCoverageTime();
		Map<String,String> coverage = apc.compute();
		
		String first = coverage.keySet().iterator().next();
		first = first.substring(first.indexOf("-")+1);
		try {
			Config.getInstance().pls_start_time.setTime(f.parse(first));
			Config.getInstance().pls_end_time = (Calendar)Config.getInstance().pls_start_time.clone();
			Config.getInstance().pls_end_time.add(Calendar.DAY_OF_YEAR, 10);
			nm = NetworkMapFactory.getNetworkMap(Config.getInstance().pls_start_time);
			System.out.println(plsf+" FROM: "+Config.getInstance().pls_start_time.getTime()+" TO: "+Config.getInstance().pls_end_time.getTime());
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	

	public void analyze(String line) {
		String celllac = line.split("\t")[2];
		NetworkCell c = nm.get(celllac);
		if(c!=null) 
			cells.put(c.getDescription(), c);			
	}
	
	
	public void finish() {
		try{
			System.out.println("N. Cells = "+cells.size());
			for(NetworkCell nc : cells.values()) {
				String name = nc.getBarycentreLatitude()+","+nc.getBarycentreLongitude()+","+nc.getRadius();
				rm.add(new Region(name,GeomUtils.getCircle(nc.getBarycentreLongitude(), nc.getBarycentreLatitude(), nc.getRadius())));
			}
			/*
			KML kml = new KML();
			File dir = FileUtils.createDir("BASE/RegionMap");
			PrintWriter out = new PrintWriter(new FileWriter(dir+"/"+name+".kml"));
			
			kml.printHeaderDocument(out, name);
			out.println(rm.toKml(Colors.RANDOM_COLORS[0]));
			kml.printFooterDocument(out);
			out.close();
			*/
			nm = null; // clean up the network map
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public Map<String,RegionMap> getPlsCoverage() {
		Map<String,RegionMap> map = null;
		File odir = FileUtils.createDir("BASE/RegionMap");
		File f = new File(odir+"/plsCoverageSpace.ser");
		if(f.exists()) {
			map = (Map<String,RegionMap>)CopyAndSerializationUtils.restore(f);
		}
		else {
			File[] basedirs = FileUtils.getFiles("DATASET/PLS/file_pls");
			map = new HashMap<String,RegionMap>();
			for(File basedir: basedirs) {
				for(File dir: basedir.listFiles()) {
					if(!map.containsKey(dir.getName())) {
						
						AnalyzePLSCoverageSpace ba = new AnalyzePLSCoverageSpace(dir.getAbsolutePath());
						ba.run();
						map.put(dir.getName(),ba.rm);
					}
				}
			}
			CopyAndSerializationUtils.save(f, map);
		}
		return map;
	}
	
	
	public void printKml(Map<String,RegionMap> map) throws Exception {
		KML kml = new KML();
		File dir = FileUtils.createDir("BASE/RegionMap");
		PrintWriter out = new PrintWriter(new FileWriter(dir+"/"+"plsCoverage.kml"));
		
		kml.printHeaderDocument(out, "plsCoverage");
		int index = 0;
		for(String name: map.keySet()) {
			RegionMap rm = map.get(name);
			if(rm.getNumRegions() > 0) {
				System.out.println(rm.getName()+" ==> "+rm.getNumRegions());
				kml.printFolder(out, name);
				out.println(rm.toKml(Colors.RANDOM_COLORS[index]));
				index ++;
				kml.closeFolder(out);
			}
		}
		kml.printFooterDocument(out); 
		
		out.close();
	}
	
	
	public String getJSMap(Map<String,RegionMap> map) {
		StringBuffer sb = new StringBuffer();
		sb.append("var citymap = {};\n");
		
		for(String name: map.keySet()) {
			RegionMap rm = map.get(name);
			if(rm.getNumRegions() > 0) {
				sb.append("citymap['"+name+"'] = new Array();\n");
				System.out.println(rm.getName());
				//nm = NetworkMapFactory.getNetworkMap(rm.getName());
				int i = 0;
				for(RegionI r: rm.getRegions()) {
					String[] x = r.getName().split(",");
					double lat = Double.parseDouble(x[0]);
					double lon = Double.parseDouble(x[1]);
					double radius = Double.parseDouble(x[2]);
					sb.append("citymap['"+name+"']["+i+"] = {center: new google.maps.LatLng("+lat+", "+lon+"),radius: "+radius+"};\n");
					i++;
					if(i >= 100) break;
				}
			}
		}
		return sb.toString();
	}
	
	
	public String getJSMapCenterLatLng(Map<String,RegionMap> map) {
		double lat = 0;
		double lng = 0;
		double cont = 0;
		
		for(String name: map.keySet()) {
			RegionMap rm = map.get(name);
			for(RegionI r: rm.getRegions()) {
				String[] x = r.getName().split(",");
				lat += Double.parseDouble(x[0]);
				lng += Double.parseDouble(x[1]);
				cont ++;
			}
		}
		return (lat/cont)+","+(lng/cont);
	}
	
	
	
	public static void main(String[] args) throws Exception {
		
		AnalyzePLSCoverageSpace ba = new AnalyzePLSCoverageSpace();
		
	
		Map<String,RegionMap> map = ba.getPlsCoverage();
		ba.printKml(map);
		ba.getJSMap(map);
		System.out.println(ba.getJSMapCenterLatLng(map));

	
		Logger.logln("Done!");
	}	
	
}
