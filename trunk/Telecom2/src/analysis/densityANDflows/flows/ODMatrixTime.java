package analysis.densityANDflows.flows;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import network.NetworkCell;
import network.NetworkMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;
import area.region.Region;
import area.region.RegionMap;

public class ODMatrixTime {
	public static void main(String[] args) throws Exception {
		
		String region = "Piemonte";//"TorinoGrid20";
		File input_obj_file = new File(Config.getInstance().base_dir+"/cache/"+region+".ser");
		if(!input_obj_file.exists()) {
			System.out.println(input_obj_file+" does not exist... run the region parser first!");
			System.exit(0);
		}
		
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(input_obj_file); 
		Map<Move,Double> list_od = new HashMap<Move,Double>();
		
		String file = "Torino-11_03_2012_17_00-11_03_2012_19_00.txt";
		BufferedReader br = new BufferedReader(new FileReader(new File(Config.getInstance().base_dir+"/LocationsXUserAroundAnEvent/"+file)));
		String line;
		NetworkMap nm = NetworkMap.getInstance();
		
		while((line=br.readLine())!=null) {
			line = line.substring(line.indexOf(",")+1).trim();
			String[] cells = line.split(" ");
			for(int i=1;i<cells.length;i++) {
				NetworkCell nc1 = nm.get(Long.parseLong(cells[i-1]));
				NetworkCell nc2 = nm.get(Long.parseLong(cells[i]));
				Region r1 = rm.get(nc1.getBarycentreLongitude(), nc1.getBarycentreLatitude());
				Region r2 = rm.get(nc2.getBarycentreLongitude(), nc2.getBarycentreLatitude());
				
				//if(!r1.getName().equals(r2.getName()))
				//System.out.println("Movement from: "+r1.getName()+" to "+r2.getName());
				
				Move m = new Move(r1,r2);
				Double c = list_od.get(m);
				c = c == null ? 1 : c+1;
				list_od.put(m, c);
				
			}
		}
		br.close();
		
		
		ODMatrixVisual.draw(file, list_od);
		
		Logger.logln("Done");
	}
}
