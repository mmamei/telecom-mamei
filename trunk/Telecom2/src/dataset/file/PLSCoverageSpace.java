package dataset.file;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import region.Region;
import region.RegionI;
import region.RegionMap;
import utils.Colors;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.GeomUtils;
import utils.Logger;
import visual.kml.KML;
import dataset.PLSCoverageSpaceI;



 class PLSCoverageSpace extends BufferAnalyzer implements PLSCoverageSpaceI {
	
	SimpleDateFormat f = new SimpleDateFormat("yyyy/MMM/dd",Locale.US);
	String name;
	RegionMap nm = null;
	RegionMap rm;  
	
	Map<String,RegionI> cells = new HashMap<String,RegionI>();

	
	PLSCoverageSpace() {
		
	}
	
	PLSCoverageSpace(String plsf) {
		super();
		System.out.println("======================> "+plsf);
		plsf = plsf.replaceAll("\\\\", "/");
		Config.getInstance().pls_folder = plsf;
		name =  plsf.substring(plsf.lastIndexOf("/")+1);
		rm = new RegionMap(plsf);
		PLSCoverageTime apc = new PLSCoverageTime();
		List<String> coverage = apc.compute();
		
		String first = coverage.get(0);
		try {
			Config.getInstance().pls_start_time.setTime(f.parse(first));
			Config.getInstance().pls_end_time = (Calendar)Config.getInstance().pls_start_time.clone();
			Config.getInstance().pls_end_time.add(Calendar.DAY_OF_YEAR, 5);
			nm = DataFactory.getNetworkMapFactory().getNetworkMap(Config.getInstance().pls_start_time);
			System.out.println(nm.getName());
			System.out.println(plsf+" FROM: "+Config.getInstance().pls_start_time.getTime()+" TO: "+Config.getInstance().pls_end_time.getTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	

	protected void analyze(String line) {
		String celllac = line.split("\t")[2];
		RegionI c = nm.getRegion(celllac);
		if(c!=null) 
			cells.put(c.getName(), c);			
	}
	
	
	protected void finish() {
		try{
			System.out.println("N. Cells = "+cells.size());
			for(RegionI nc : cells.values()) {
				String name = nc.getLatLon()[0]+","+nc.getLatLon()[1]+","+nc.getRadius();
				rm.add(new Region(name,GeomUtils.getCircle(nc.getLatLon()[1], nc.getLatLon()[0], nc.getRadius())));
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
		File odir = new File(Config.getInstance().base_folder+"/RegionMap");
		odir.mkdirs();
		File f = new File(odir+"/plsCoverageSpace.ser");
		if(f.exists()) {
			map = (Map<String,RegionMap>)CopyAndSerializationUtils.restore(f);
		}
		else {
			File basedir = new File(Config.getInstance().pls_root_folder);
			map = new HashMap<String,RegionMap>();
			
				for(File dir: basedir.listFiles()) {
					if(!map.containsKey(dir.getName())) {
						PLSCoverageSpace ba = new PLSCoverageSpace(dir.getAbsolutePath());
						ba.run();
						map.put(dir.getName(),ba.rm);
					}
				
			}
			CopyAndSerializationUtils.save(f, map);
		}
		return map;
	}
	
	
	public void printKml(Map<String,RegionMap> map) throws Exception {
		KML kml = new KML();
		File dir = new File(Config.getInstance().base_folder+"/RegionMap");
		dir.mkdirs();
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
		
		
		sb.append("var heatmaps = new Array();\n");
	
		
		int i = 0;
		for(String name: map.keySet()) {
			RegionMap rm = map.get(name);
			if(rm.getNumRegions() > 0) {
				
				sb.append("data_"+name+" = [\n");
				           
				
				for(RegionI r: rm.getRegions()) {
					double lat = r.getLatLon()[0];
					double lon = r.getLatLon()[1];
					sb.append("new google.maps.LatLng("+lat+", "+lon+"),\n");
				}
				sb.append("];\n");
				
				String color = Colors.RANDOM_COLORS_RGBA[i%Colors.RANDOM_COLORS_RGBA.length];
				
				sb.append("heatmaps["+i+"] = new google.maps.visualization.HeatmapLayer({data: new google.maps.MVCArray(data_"+name+"),radius:30,gradient: ['rgba(255, 255, 255, 0)','"+color+"']});\n");
				i++;
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
				lat += r.getLatLon()[0];
				lng += r.getLatLon()[1];
				cont ++;
			}
		}
		return (lat/cont)+","+(lng/cont);
	}
	
	
	
	public static void main(String[] args) throws Exception {
		
		//Config.getInstance().changeDataset("ivory-set3");
		
		PLSCoverageSpace ba = new PLSCoverageSpace();
		
	
		Map<String,RegionMap> map = ba.getPlsCoverage();
		ba.printKml(map);
		ba.getJSMap(map);
		System.out.println(ba.getJSMapCenterLatLng(map));

	
		Logger.logln("Done!");
	}	
	
}
