package analysis.densityANDflows;

import java.io.BufferedReader;
import java.io.FileReader;

import utils.Config;
import utils.Logger;
import area.SpaceGrid;

public class PopulationDensityGrid {
	public static void main(String[] args) throws Exception {
		
		String kind_of_place = "SUNDAY";
		double[][] bbox = new double[][]{{7.494789211677311, 44.97591738081519},{7.878659418860384, 45.16510171374535}};
		SpaceGrid sg = new SpaceGrid(bbox[0][0],bbox[0][1],bbox[1][0],bbox[1][1],40,40);
		double[][] density = process(sg,kind_of_place);		
		sg.draw(kind_of_place, density);
	}
	
	public static double[][] process(SpaceGrid sg, String kind_of_place) throws Exception {
		
		int[] size = sg.size();
		double[][] density = new double[size[0]][size[1]];
		
		BufferedReader br = new BufferedReader(new FileReader(Config.getInstance().base_dir+"/PlaceRecognizer/file_pls_piem_users_above_2000/results.csv"));
		String line;
		String[] elements;
		while((line = br.readLine())!=null) {
			if(line.contains(","+kind_of_place+",")) {
				line = line.substring(line.indexOf(kind_of_place)+kind_of_place.length()+1, line.length());
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
		br.close();
		return density;
	}
}
