package analysis.densityANDflows.flows;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import region.RegionI;
import region.RegionMap;
import utils.CopyAndSerializationUtils;
import utils.Logger;
import dataset.file.DataFactory;

public class ODMatrixTime {
	public static void main(String[] args) throws Exception {
		
		String region = "Stadio Silvio Piola (NO)";//"TorinoGrid20";
		File input_obj_file = new File("BASE/cache/"+region+".ser");
		if(!input_obj_file.exists()) {
			System.out.println(input_obj_file+" does not exist... run the region parser first!");
			System.exit(0);
		}
		
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(input_obj_file); 
		
		rm.printKML();
		
		Map<Move,Double> list_od = new HashMap<Move,Double>();
		
		String file = "Stadio_Silvio_Piola_(NO)-11_03_2012_18_00-12_03_2012_00_00.txt";//"Torino-11_03_2012_17_00-11_03_2012_19_00.txt";
		RegionMap nm = DataFactory.getNetworkMapFactory().getNetworkMap(DataFactory.getNetworkMapFactory().getCalendar("11/03/2012"));
		
		BufferedReader br = new BufferedReader(new FileReader("BASE/LocationsXUserAroundAnEvent/"+file));
		String line;
		while((line=br.readLine())!=null) {
			line = line.substring(line.indexOf(",")+1).trim();
			String[] cells = line.split(" ");
			for(int i=1;i<cells.length;i++) {
				RegionI nc1 = nm.getRegion(cells[i-1]);
				RegionI nc2 = nm.getRegion(cells[i]);
				
				//System.out.print(nc1.getBarycentreLongitude()+","+nc1.getBarycentreLatitude()+" ---> ");
				//System.out.println(nc2.getBarycentreLongitude()+","+nc2.getBarycentreLatitude());
				
				RegionI r1 = rm.get(nc1.getLatLon()[1], nc1.getLatLon()[0]);
				RegionI r2 = rm.get(nc2.getLatLon()[1], nc2.getLatLon()[0]);
				
				//if(!r1.getName().equals(r2.getName()))
				//System.out.println("Movement from: "+r1.getName()+" to "+r2.getName());
				if(r1!=null && r2!=null) {
					Move m = new Move(r1,r2);
					Double c = list_od.get(m);
					c = c == null ? 1 : c+1;
					list_od.put(m, c);
					//System.err.println(m);
				}	
			}
		}
		br.close();
		
		/*
		for(Move m: list_od.keySet()) {
			if(!m.sameSourceAndDestination())
				System.out.println(m.toCoordString());
		}
		*/
		
		ODMatrixVisual.draw("ODMatrixTime_"+file, list_od,false);
		
		Logger.logln("Done");
	}
}
