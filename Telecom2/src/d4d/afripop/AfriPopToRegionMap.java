package d4d.afripop;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import visual.kml.KMLHeatMap;

public class AfriPopToRegionMap {
	
	public static void main(String[] args) throws Exception {
		Config.getInstance().changeDataset("ivory-set3");
		
		String region = "IvoryCoast";
		double west = -8.5998017426885;
		double south = 4.3570288111761;
		double east = -2.4942126426885;
		double north = 10.736773611176101;

		RegionMap map = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/FIX_"+region+".ser"));
		AfriPopMap afripop = AfriPopParser.run(region,south,west,north,east);
		
		Map<String,Double> census = process(map,afripop);	
		
		KMLHeatMap.drawHeatMap(Config.getInstance().base_folder+"/RegionMap/"+region+"_Afripop.kml", census, map, "", true);
		
		
		File out_dir = new File(Config.getInstance().base_folder+"/CENSUS");
		out_dir.mkdirs();
		PrintWriter out = new PrintWriter(new FileWriter(out_dir+"/"+region+".csv"));
		
		for(String subpref: census.keySet()) 
			out.println(subpref.toUpperCase()+","+census.get(subpref));
		out.close();
		
		System.out.println("Done");
	}
	
	public static Map<String,Double> process(RegionMap map, AfriPopMap afripop) throws Exception {
		
	
		Map<String,Double> census = new HashMap<String,Double>();
		int cont = 0;
		for(double j=0; j<afripop.height;j++){
			for(double i=0; i<afripop.width;i++){
				if(afripop.data[cont] > 1) { 
					// convert i and j to latitude and longitude
					//double lon = west + j * MalariaAtlas2KML.cellsize;
					//double lat = north - i * MalariaAtlas2KML.cellsize;
					
					double lon = afripop.west + i * (afripop.east - afripop.west)/((double)afripop.width-1);
					double lat = afripop.north - j * (afripop.north - afripop.south)/((double)afripop.height-1);
					
					
					RegionI r = map.get(lon, lat);
					if(r!=null) {
						Double pop = census.get(r.getName());
						if(pop == null) pop = 0.0;
						census.put(r.getName(), pop+afripop.data[cont]);		
					}
					else System.out.println(lat+","+lon+" is outside of the region map!");
				}
				cont ++;
			}
			if((j+1)%10 == 0) System.out.println((j+1)+" out of "+afripop.width+" done");
		}
		
		return census;
	}
	
}
