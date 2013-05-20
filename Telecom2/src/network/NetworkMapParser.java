package network;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import utils.Config;
import utils.Logger;



public class NetworkMapParser {
	public static void main(String[] args) {
		
		long startTime = System.currentTimeMillis();
			
		HashMap<Long, NetworkCell> map = new HashMap<Long, NetworkCell>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(Config.getInstance().network_map_file));
			String line;
			while((line = in.readLine()) != null){
				String [] splitted = line.split(":");
				NetworkCell cell = new NetworkCell(splitted);
				map.put(cell.getCellac(), cell);
			}
			in.close();
			
			ObjectOutputStream out=new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(new File(Config.getInstance().network_map_bin))));
			out.writeObject(map);
			out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		long endTime = System.currentTimeMillis();
		int mins = (int)((endTime - startTime) / 60000);
		Logger.logln("Completed after "+mins+" mins");
	}
}
