package analysis.densityANDflows.flows;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;
import java.util.TreeMap;

import network.NetworkCell;
import network.NetworkMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;
import area.CityEvent;
import area.Placemark;
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
		
		
		Placemark p = new Placemark("Torino",new double[]{45.073036,7.679733},5000);
		CityEvent ce = new CityEvent(p,"11/03/2012 17:00","11/03/2012 19:00",-1);
		
		BufferedReader br = new BufferedReader(new FileReader(new File(Config.getInstance().base_dir+"/LocationsXUserAroundAnEvent/"+ce.toFileName())));
		String line;
		
		NetworkMap nm = NetworkMap.getInstance();
		
		Map<Move,Double> list_od = new TreeMap<Move,Double>();
		
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
		
		
		ODMatrix.draw(ce.toFileName(), list_od);
		
		Logger.logln("Done");
	}
}
