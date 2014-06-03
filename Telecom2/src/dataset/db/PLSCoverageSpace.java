package dataset.db;

import java.io.File;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import region.NetworkCell;
import region.RegionI;
import region.RegionMap;
import utils.Colors;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;
import dataset.PLSCoverageSpaceI;
import dataset.db.insert.DBConnection;
import dataset.db.insert.NetworkTable;



 class PLSCoverageSpace implements PLSCoverageSpaceI {
	
	public static final SimpleDateFormat F = new SimpleDateFormat("yyyyMMdd");
	private int MAX_TABLES_PER_REGION = 3; // thus days
	
	
	
	public Map<String,RegionMap> getPlsCoverage() {
		Map<String,RegionMap> map = new HashMap<String,RegionMap>();
		
		File odir = new File(Config.getInstance().base_folder+"/RegionMap");
		odir.mkdirs();
		File f = new File(odir+"/plsCoverageSpace.ser");
		if(f.exists()) {
			return (Map<String,RegionMap>)CopyAndSerializationUtils.restore(f);
		}
		
		
		Map<String,Integer> numTablesProcessedByRegion = new HashMap<String,Integer>();
		Statement s = DBConnection.getStatement();
		try {
			ResultSet r = s.executeQuery("select table_name from information_schema.tables");
			while(r.next()) {
				String t = r.getString("table_name"); 
				if(t.startsWith("pls_")) {					
					String region = t.substring(0,t.lastIndexOf("_"));
					Integer num = numTablesProcessedByRegion.get(region);
					if(num == null) num = 0;
					if(num < MAX_TABLES_PER_REGION) {
						
						System.out.println("Processing "+t);
						RegionMap rm = map.get(region);
						if(rm == null) {
							rm = new RegionMap(region);
							map.put(region, rm);
						}
						
						Calendar cal = Calendar.getInstance();
						cal.setTime(F.parse(t.substring(t.lastIndexOf("_")+1)));
						String nt = NetworkTable.findClosestNetworkTable(cal);
						
						Statement s2 = DBConnection.getStatement();
						long opstart = System.currentTimeMillis();
						ResultSet r2 = s2.executeQuery("select "+nt+".celllac, "+nt+".description,"+nt+".lac,"+nt+".cell_id,"+nt+".radius,AsText("+nt+".center) from "+t+","+nt+" where "+t+".celllac = "+nt+".celllac");
						long opend = System.currentTimeMillis();
						//System.out.println("Query time = "+(opend-opstart)/(1000));
						while(r2.next()) {
							String celllac = r2.getString("celllac");
					
							if(rm.getRegion(celllac) != null) continue;
							
							String description = r2.getString("description");
							long lac = r2.getInt("lac");
							long cell_id = r2.getInt("cell_id");
							double radius = r2.getFloat("radius");
							String lonlat = r2.getString("AsText("+nt+".center)");
							double lon = Double.parseDouble(lonlat.substring(lonlat.indexOf("(")+1,lonlat.indexOf(" ")));
							double lat = Double.parseDouble(lonlat.substring(lonlat.indexOf(" ")+1,lonlat.indexOf(")")));
							rm.add(new NetworkCell(celllac,description,lac,cell_id,lat,lon,radius));
						}
						r2.close();
						s2.close();
						
						numTablesProcessedByRegion.put(region,num+1);
					}
				}
			}
			r.close();
			s.close();
			DBConnection.closeConnection();
		}catch(Exception e) {
			e.printStackTrace();
		}
		CopyAndSerializationUtils.save(f, map);
		return map;
	}
	
	public String getJSMap(Map<String,RegionMap> map) {
		StringBuffer sb = new StringBuffer();
		
		
		sb.append("var heatmaps = new Array();\n");
	
		
		int i = 0;
		for(String name: map.keySet()) {
			RegionMap rm = map.get(name);
			if(rm.getNumRegions() > 0) {
				
				sb.append("data_"+name+" = [\n");
				           
				int j = 0;
				for(RegionI r: rm.getRegions()) {
					double lat = r.getLatLon()[0];
					double lon = r.getLatLon()[1];
					sb.append("new google.maps.LatLng("+lat+", "+lon+"),\n");
					if(j > 500) break;
					j++;
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
		
		PLSCoverageSpace ba = new PLSCoverageSpace();
		
		Map<String,RegionMap> map = ba.getPlsCoverage();
		
		for(String k: map.keySet())
			System.out.println(k);
		
		Logger.logln("Done!");
	}	
	
}
