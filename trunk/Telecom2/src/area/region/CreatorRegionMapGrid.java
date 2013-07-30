package area.region;

import java.io.File;

import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;

public class CreatorRegionMapGrid {
	
	public static void main(String[] args) throws Exception {
		
		int size = 20;
		String name = "TorinoGrid"+size;
		double[][] bbox = new double[][]{{7.494789211677311, 44.97591738081519},{7.878659418860384, 45.16510171374535}};
		String output_obj_file=Config.getInstance().base_dir+"/cache/"+name+".ser";
		SpaceGrid sg = new SpaceGrid(bbox[0][0],bbox[0][1],bbox[1][0],bbox[1][1],size,size);
		
		
		RegionMap rm = new RegionMap(name);
		
		for(int i=0; i<size;i++)
		for(int j=0; j<size;j++) {
			rm.add(new Region(i+","+j,sg.getBorderLonLat(i, j)));
		}
		
	
		
		CopyAndSerializationUtils.save(new File(output_obj_file), rm);
		Logger.logln("Done!");
	}
	
}
