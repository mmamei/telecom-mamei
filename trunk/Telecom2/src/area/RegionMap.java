package area;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import utils.Colors;
import utils.Config;
import visual.Kml;

public class RegionMap implements Serializable {
		
	private String name;
	private List<Region> rm;
	
	public RegionMap(String name) {
		this.name = name;
		rm = new ArrayList<Region>();
	}
	
	public void add(Region r) {
		rm.add(r);
	}
	
	public void printKML() throws Exception  {
		String dir = Config.getInstance().base_dir+"/RegionMap";
		File d = new File(dir);
		if(!d.exists()) d.mkdirs();
		
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(dir+"/"+name+".kml")));
		Kml kml = new Kml();
		kml.printHeaderFolder(out, name);
		int index = 0;
		for(Region r: rm) {
			out.println(r.toKml(Colors.RANDOM_COLORS[index]));
			index++;
			if(index >= Colors.RANDOM_COLORS.length) index = 0;
		}
		kml.printFooterFolder(out);
		out.close();
	}
}
