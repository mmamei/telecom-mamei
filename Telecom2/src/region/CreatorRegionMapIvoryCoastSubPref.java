package region;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.vividsolutions.jts.geom.Geometry;

import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;

public class CreatorRegionMapIvoryCoastSubPref {
	
	public static void main(String[] args) throws Exception {
		Config.getInstance().changeDataset("ivory-set3");
		String region = "IvoryCoast";
		String input_kml_file="C:/DATASET/GEO/"+region+".kml";
		String output_obj_file=Config.getInstance().base_folder+"/RegionMap/FIX_"+region+".ser";
		process(region, input_kml_file,output_obj_file);
		Logger.logln("Done!");
	}
	
	public static RegionMap process(String rname, String input_kml_file, String output_obj_file) throws Exception {
		
		RegionMap rm = new RegionMap(rname);
			
		BufferedReader br = new BufferedReader(new FileReader(new File(input_kml_file)));
		String line;
		boolean inPlacemark = false;
		boolean inRegion = false;
		
		String name = null;
		int id = -1;
		double lat = -1;
		double lon = -1;
		String kmlcoord = null;
		
		while((line = br.readLine())!=null) {
			line = line.trim();
			
			if(line.equals("<Placemark id=\"\">")) {
				name = null;
				id = -1;
				lat = -1;
				lon = -1;
				inPlacemark = true;
			}
			
			
			if(inPlacemark) {
				if(line.startsWith("<Snippet maxLines=\"1\">")) {
					name = line.substring(line.indexOf(">")+1, line.indexOf("</"));
					//System.out.println(name);
				}
				if(line.startsWith("<name>")) {
					String sid = line.substring(line.indexOf(">")+1, line.indexOf("</"));
					id = Integer.parseInt(sid);
					//System.out.println(id);
				}
				if(line.startsWith("<coordinates>")) {
					String coord = line.substring(line.indexOf(">")+1, line.indexOf(",0 </"));
					String[] sc = coord.split(",");
					lat = Double.parseDouble(sc[0]);
					lon = Double.parseDouble(sc[1]);
					//System.out.println(lat+","+lon);
				}
			}
			
			if(inPlacemark && line.equals("</Placemark>")) { 
				
				Region sp = new Region(""+id,(Geometry)null);
				sp.setDescription(name);
				rm.add(sp);
				inPlacemark = false;
			}
			
			
			if(!line.equals("<Placemark id=\"\">") && line.startsWith("<Placemark id=")) {
				inRegion = true;
			}
			
			
			if(inRegion) {
				if(line.startsWith("<name>")) {
					String sid = line.substring(line.indexOf(">")+1, line.indexOf("</"));
					id = Integer.parseInt(sid);
					//System.out.println(id);
				}
				if(line.startsWith("<coordinates>")) {
					kmlcoord = line.substring(line.indexOf(">")+1, line.indexOf(" </"));
				}
			}
			
			if(inRegion && line.equals("</Placemark>")) {
				
				Region r = new Region(""+id,kmlcoord);
				r.setDescription(rm.getRegion(""+id).getDescription());
				rm.add(r);
				inRegion = false;
			}		
			
		}
		
		br.close();
		CopyAndSerializationUtils.save(new File(output_obj_file), rm);
		return rm;
	}
}
