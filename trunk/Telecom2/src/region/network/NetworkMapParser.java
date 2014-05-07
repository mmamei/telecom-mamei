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
			HashMap<Long, NetworkCell> map = new HashMap<Long, NetworkCell>();
			String line;
			while((line = in.readLine()) != null){
				String [] splitted = line.split(":");
				NetworkCell cell = new NetworkCell(splitted);
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
