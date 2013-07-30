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
import visual.HeatMapGoogleMaps;
import area.SpaceGrid;

public class PopulationDensityGrid {
	public static void main(String[] args) throws Exception {
		
		String kop = "SATURDAY_NIGHT";
		String exclude_kop = "HOME";
		int size = 20;
		double[][] bbox = new double[][]{{7.494789211677311, 44.97591738081519},{7.878659418860384, 45.16510171374535}};
		SpaceGrid sg = new SpaceGrid(bbox[0][0],bbox[0][1],bbox[1][0],bbox[1][1],size,size);
		Map<String,UserPlaces> up = UserPlaces.readUserPlaces(Config.getInstance().base_dir+"/PlaceRecognizer/file_pls_piem_users_above_2000/results.csv");
		double[][] density = process(sg,up,kop,exclude_kop);	
		
		
		
		String dir = Config.getInstance().base_dir+"/PopulationDensityGrid";
		File d = new File(dir);
		if(!d.exists()) d.mkdirs();
		
		//String file = dir+"/"+ kop+"-"+nokop+"-"+size+".kml";
		//sg.draw(file, kop+"-"+nokop, density);
		
		String title = kop+"-"+exclude_kop+"-"+size;
		String file = dir+"/"+title+".html";

		List<double[]> points = new ArrayList<double[]>();
		List<Double> weights = new ArrayList<Double>();
		
		for(int i=0; i<size;i++)
		for(int j=0; j<size;j++) {
			if(density[i][j] > 1) {
				points.add(sg.grid2LatLon(i, j));
				weights.add(density[i][j]);
			}
		}
		
		HeatMapGoogleMaps.draw(file, title, points, weights);
	}
	
	
	
	
	
	public static double[][] process(SpaceGrid sg, Map<String,UserPlaces> up, String kop, String exclude_kop) {
		
		int[] size = sg.size();
		double[][] density = new double[size[0]][size[1]];
			
		for(UserPlaces p: up.values()) {
			List<double[]> lkop = p.places.get(kop);
			List<double[]> lnokop = p.places.get(exclude_kop);
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