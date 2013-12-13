package analysis.densityANDflows.flows;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import network.NetworkCell;
import network.NetworkMap;
import network.NetworkMapFactory;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;
import analysis.presence_at_event.PlacemarkRadiusExtractor;
import area.CityEvent;
import area.region.Region;
import area.region.RegionMap;

public class ODMatrixEventPresence {
	public static void main(String[] args) throws Exception {
		process("Juventus Stadium (TO),20/03/2012",2000);
	}
	
	public static void process(String event, double expand) throws Exception {
		
		CityEvent ce = CityEvent.getEvent(event);
		Map<String,Double> bestR = PlacemarkRadiusExtractor.readBestR(true,true);
		ce.spot.changeRadius(bestR.get(ce.spot.name));
		
		CityEvent ce_expand = CityEvent.expand(ce, 1, expand);
		
		File input_obj_file = new File(Config.getInstance().base_dir+"/cache/"+ce.spot.name+".ser");
		if(!input_obj_file.exists()) {
			System.out.println(input_obj_file+" does not exist... run the region parser first!");
			System.exit(0);
		}
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(input_obj_file); 
		rm.printKML();
		
		
		
		// READ INFORMATION ABOUT EVENT ATTENDANCE
		Map<String,Double> user_pres_prob = new HashMap<String,Double>();
		BufferedReader br1 = new BufferedReader(new FileReader(new File(Config.getInstance().base_dir+"/PresenceCounterProbability/ProbScores/"+ce.toFileName())));
		String line;
		//c1a4c67ec172ae1554e9856db5358fcb1664fcbff4b0222a1d86c093a32736b;0.8324594444444444
		while((line=br1.readLine())!=null) {
			String[] e = line.split(";");
			user_pres_prob.put(e[0], Double.parseDouble(e[1]));
		}
		br1.close();
		
		
		// READ INFORMATION ABOUT MOVEMENT
		Map<Move,Double> incoming_od = new HashMap<Move,Double>();
		Map<Move,Double> outgoing_od = new HashMap<Move,Double>();
		
		BufferedReader br2 = new BufferedReader(new FileReader(new File(Config.getInstance().base_dir+"/LocationsXUserAroundAnEvent/"+ce_expand.toFileName())));
		
		NetworkMap nm = NetworkMapFactory.getNetworkMap(event.substring(event.indexOf(",")+1));
		
		while((line=br2.readLine())!=null) {
			String user = line.substring(0, line.indexOf(","));
			Double upp = user_pres_prob.get(user); // user presence probability
			if(upp == null || upp == 0) continue;
			line = line.substring(line.indexOf(",")+1).trim();
			String[] cells = line.split(" ");
			
			boolean before = true;
			for(int i=1;i<cells.length;i++) {
				NetworkCell nc1 = nm.get(Long.parseLong(cells[i-1]));
				NetworkCell nc2 = nm.get(Long.parseLong(cells[i]));
				
				if(ce.spot.contains(nc1.getCellac()) && !ce.spot.contains(nc2.getCellac())) before = false;
				
				Region r1 = rm.get(nc1.getBarycentreLongitude(), nc1.getBarycentreLatitude());
				Region r2 = rm.get(nc2.getBarycentreLongitude(), nc2.getBarycentreLatitude());
				
				Map<Move,Double> list_od = before ? incoming_od : outgoing_od;
				
				if(r1!=null && r2!=null) {
					Move m = new Move(r1,r2);
					Double c = list_od.get(m);
					c = c == null ? upp : c+upp;
					list_od.put(m, c);
				}	
			}
		}
		br2.close();
		
		
		ODMatrixVisual.draw("ODMatrixEventPresence_"+ce.toFileName(), incoming_od, outgoing_od, true);
		
		Logger.logln("Done");
	}
}
