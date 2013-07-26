package analysis.densityANDflows;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.Config;
import utils.Logger;
import area.SpaceGrid;

public class PopulationDensityGrid {
	public static void main(String[] args) throws Exception {
		
		String kop = "SATURDAY_NIGHT";
		String nokop = "HOME";
		int size = 20;
		double[][] bbox = new double[][]{{7.494789211677311, 44.97591738081519},{7.878659418860384, 45.16510171374535}};
		SpaceGrid sg = new SpaceGrid(bbox[0][0],bbox[0][1],bbox[1][0],bbox[1][1],size,size);
		double[][] density = process(sg,kop,nokop);	
		
		
		String dir = Config.getInstance().base_dir+"/PopulationDensityGrid";
		File d = new File(dir);
		if(!d.exists()) d.mkdirs();
		
		String file = dir+"/"+ kop+"-"+nokop+"-"+size+".kml";
		
		sg.draw(file, kop+"-"+nokop, density);
	}
	
	public static double[][] process(SpaceGrid sg, String kop, String nokop) throws Exception {
		
		int[] size = sg.size();
		double[][] density = new double[size[0]][size[1]];
		
		BufferedReader br = new BufferedReader(new FileReader(Config.getInstance().base_dir+"/PlaceRecognizer/file_pls_piem_users_above_2000/results.csv"));
		String line;
		String[] elements;
		Map<String,UserPlaces> up = new HashMap<String,UserPlaces>();
		while((line = br.readLine())!=null) {
			elements = line.split(",");
			String username = elements[0];
			UserPlaces places = up.get(username);
			if(places==null) {
				places = new UserPlaces(username);
				up.put(username, places);
			}
			String kind = elements[1];
			for(int i=2;i<elements.length;i++){
				String c = elements[i];
				double lon = Double.parseDouble(c.substring(0,c.indexOf(" ")));
				double lat = Double.parseDouble(c.substring(c.indexOf(" ")+1));
				places.add(kind, lon, lat);
			} 
		}
		br.close();
		for(UserPlaces p: up.values()) {
			List<double[]> lkop = p.places.get(kop);
			List<double[]> lnokop = p.places.get(nokop);
			List<double[]> r = exclude_nopkop(sg,lkop,lnokop);
			if(r != null)
			for(double[] ll: r) {
				int[] ij = sg.getGridCoord(ll[0], ll[1]);
				int i = ij[0];
				int j = ij[1];
				if(i<0 || i>size[0] || j<0 || j>size[1]) 
					Logger.logln(ll[0]+","+ll[1]+" is outside the grid");
				else 
					density[i][j] += 1;
			}
		}
		/*
		while((line = br.readLine())!=null) {
			if(line.contains(","+kop+",")) {
				line = line.substring(line.indexOf(kop)+kop.length()+1, line.length());
				elements = line.split(",");
				for(String c: elements) {
					double lon = Double.parseDouble(c.substring(0,c.indexOf(" ")));
					double lat = Double.parseDouble(c.substring(c.indexOf(" ")+1));
					int[] ij = sg.getGridCoord(lon, lat);
					int i = ij[0];
					int j = ij[1];
					if(i<0 || i>size[0] || j<0 || j>size[1]) 
						Logger.logln(lon+","+lat+" is outside the grid");
					else 
						density[i][j] += (1.0 / elements.length);
				}
			}
		}
		*/
		
		return density;
	}
	
	public static List<double[]> exclude_nopkop(SpaceGrid sg, List<double[]> kop, List<double[]> nokop) {
		if(kop==null) return null;
		if(nokop==null) return kop;
		List<double[]> r = new ArrayList<double[]>();
		for(double[] p1: kop) {
			boolean found = false;
			int[] ij1 = sg.getGridCoord(p1[0],p1[1]);
			for(double[] p2 : nokop) {
				int[] ij2 = sg.getGridCoord(p2[0],p2[1]);
				if(ij1[0] == ij2[0] && ij1[1] == ij2[1]) found = true;
			}
			if(!found) r.add(p1);
		}
		return r;
	}
}


class UserPlaces {
	String username;
	Map<String,List<double[]>> places;
	
	UserPlaces(String username) {
		this.username = username;
		places = new HashMap<String,List<double[]>>();
	}
	
	void add(String kop, double lon, double lat) {
		List<double[]> p = places.get(kop);
		if(p==null) {
			p = new ArrayList<double[]>();
			places.put(kop, p);
		}
		p.add(new double[]{lon,lat});
	}
}