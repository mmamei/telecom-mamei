package area.region;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import utils.CopyAndSerializationUtils;
import utils.FileUtils;
import utils.GeomUtils;
import utils.Logger;
import area.Placemark;

import com.vividsolutions.jts.geom.Geometry;



public class CreatorRegionMapCityGrid {
	
	
	public static void main(String[] args) throws Exception {
		String city = "Venezia";
		process(city);
		RegionMap.process(city);
		Logger.logln("Done!");
	}
	
	
	public static void process(String city) throws Exception {
		
		RegionMap base = new RegionMap(city+"Base");
		
		BufferedReader br = new BufferedReader(new FileReader(new File("C:/DATASET/GEO/"+city+"/areas.txt")));
		String line;
		while((line = br.readLine()) != null) {
			String coordinates = br.readLine();	
			base.add(new Region(line,coordinates));
		}
		br.close();
		
		
		Placemark p = Placemark.getPlacemark(city);
		p.changeRadius(p.getR()+1000);
		int size = 5;
		double[][] bbox = p.getBBox();
		
		SpaceGrid sg = new SpaceGrid(bbox[0][0],bbox[0][1],bbox[1][0],bbox[1][1],size,size);
		
		
		List<Region> rlist = new ArrayList<Region>();
		
		for(int i=0; i<size;i++)
		for(int j=0; j<size;j++) {
			rlist.add(new Region(i+","+j,sg.getBorderLonLat(i, j)));
		}
		
		RegionMap rm = new RegionMap(city);
		
		
		//intersect all the squares with the boundary
		for(Region r: rlist) {
			for(Region b: base.getRegions()) {
				Geometry inter = r.getGeom().intersection(b.getGeom());
				if(inter.getNumPoints() > 0) {
					rm.add(new Region(r.getName(),inter));
				}
			}
		}
		
			
		File f = FileUtils.create("RegionMap");
		CopyAndSerializationUtils.save(new File(f.getAbsolutePath()+"/"+city+".ser"), rm);
	}
}
