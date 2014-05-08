package region.network;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import utils.Config;
import utils.FileUtils;
import utils.Logger;



public class NetworkMapParser {
	public static void main(String[] args) throws Exception {
		
		
		FileUtils.createDir("BASE/NetworkMapParser");
		
		
		File dir = new File(Config.getInstance().network_map_dir);
		String[] files = dir.list();
		for(String file: files) {
			BufferedReader in = new BufferedReader(new FileReader(dir+"/"+file));
			HashMap<String, NetworkCell> map = new HashMap<String, NetworkCell>();
			String line;
			while((line = in.readLine()) != null){
				String [] splitted = line.split(":");
				
				
				String description = splitted[0];
				int barycentre = Integer.parseInt(splitted[1]);
				long lac = Long.parseLong(splitted[2]);
				long cell_id = Long.parseLong(splitted[3]);
				//String param5 = splitted[4];
				//String param6 = splitted[5];
				//String param7 = splitted[6];
				//String param8 = splitted[7];
				//String param9 = splitted[8];
				double barycentre_lat = Double.parseDouble(splitted[9]);
				double barycentre_lon = Double.parseDouble(splitted[10]);
				double radius = Double.parseDouble(splitted[11]);
				
				String celllac = String.valueOf(lac*65536+cell_id);
				
				NetworkCell cell = new NetworkCell(celllac,description,barycentre,lac,cell_id,barycentre_lat,barycentre_lon,radius);
				map.put(cell.getCellac(), cell);
			}
			in.close();
			

			ObjectOutputStream out=new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(FileUtils.getFile("BASE/NetworkMapParser")+"/"+file.substring(0,file.length()-4)+".bin")));
			out.writeObject(map);
			out.close();
		}
		
		Logger.logln("Done");
	}
}
