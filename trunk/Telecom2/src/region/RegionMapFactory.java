package region;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import utils.Config;


public class RegionMapFactory {
	
	
	public String[] getAvailableRegions() {
		List<String> files = new ArrayList<String>();
		File dir = new File(Config.getInstance().base_folder+"/RegionMap");
		for(String f: dir.list()) {
			if(f.startsWith("FIX"))
				files.add(f);
		}
		String[] arr = new String[files.size()];
		return files.toArray(arr);
	}
	
	
	public static void main(String[] args) {
		RegionMapFactory rmf = new RegionMapFactory();
		String[] regionMaps = rmf.getAvailableRegions();
		for(String rm: regionMaps)
			System.out.println(rm);
	}
	
}
