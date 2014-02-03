package area.region;

import java.io.File;

import org.gps.utils.LatLonPoint;
import org.gps.utils.LatLonUtils;

import area.CityEvent;
import area.Placemark;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.FileUtils;
import utils.Logger;

public class CreatorRegionMapGrid {
	
	public static void main(String[] args) throws Exception {
	
		
		Placemark p = Placemark.getPlacemark("Torino");
		p.changeRadius(p.getR()+1000);
		run(p.name,p.getBBox(),10);
		
		/*
		CityEvent ce = CityEvent.getEvent("Juventus Stadium (TO),20/03/2012");
		ce = CityEvent.expand(ce, 1, 10000);
		String name = ce.spot.name;
		double[][] bbox = ce.spot.getBBox();
		run(name,bbox,5);
		*/
	}
		
		
	public static void run(String name, double[][] bbox, int size) throws Exception {
		
		String output_obj_file=FileUtils.create("RegionMap").getAbsolutePath()+"/"+name+".ser";
		
		SpaceGrid sg = new SpaceGrid(bbox[0][0],bbox[0][1],bbox[1][0],bbox[1][1],size,size);
		
		
		RegionMap rm = new RegionMap(name);
		
		for(int i=0; i<size;i++)
		for(int j=0; j<size;j++) {
			rm.add(new Region(i+","+j,sg.getBorderLonLat(i, j)));
		}
		
		rm.printKML();
		
		CopyAndSerializationUtils.save(new File(output_obj_file), rm);
		Logger.logln("Done!");
	}
	
}


class SpaceGrid {
	
	private double min_lon;
	private double min_lat;
	private double max_lon;
	private double max_lat;
	 
	
	private int n_cell_lat;
	private int n_cell_lon;
	private double d_lat;
	private double d_lon;
	
	
	public SpaceGrid(double min_lon, double min_lat, double max_lon, double max_lat, int n_cell_lon, int n_cell_lat) {
		this.min_lon = min_lon;
		this.min_lat = min_lat;
		this.max_lon = max_lon;
		this.max_lat = max_lat;
		
		this.n_cell_lon = n_cell_lon;
		this.n_cell_lat = n_cell_lat;
		
		d_lon = (max_lon - min_lon) / n_cell_lon;
		d_lat = (max_lat - min_lat) / n_cell_lat;
	}
	
	public void printSize() {
		LatLonPoint p0 = new LatLonPoint(min_lat,min_lon);
		LatLonPoint p1 = new LatLonPoint(min_lat,max_lon);
		LatLonPoint p2 = new LatLonPoint(max_lat,min_lon);
		
		double d_lon = LatLonUtils.getHaversineDistance(p0, p1);
		double d_lat = LatLonUtils.getHaversineDistance(p0, p2);
		
		d_lon = d_lon / n_cell_lon;
		d_lat = d_lat / n_cell_lat;
		
		System.out.println("cell size: "+d_lon+", "+d_lat+" meters");
	}
	
	public int[] size() {
		return new int[]{n_cell_lon,n_cell_lat};
	}
	
	
	public double[] grid2LonLat(int i, int j) {
		
		double cell_lon = min_lon + j * d_lon;
		double cell_lat = min_lat + i * d_lat;
		
		return new double[]{cell_lon,cell_lat};
	}

	public double[] grid2LatLon(int i, int j) {
		
		double[] x = grid2LonLat(i,j);
		return new double[]{x[1],x[0]};
	}
	
	public int[] getGridCoord(double lon, double lat) {
		double d_lat = (max_lat - min_lat) / n_cell_lat;
		double d_lon = (max_lon - min_lon) / n_cell_lon;
		
		int grid_i = (int)Math.floor((lat - min_lat) / d_lat);
		int grid_j = (int)Math.floor((lon - min_lon) / d_lon);
		
		// deal with the maximum lat and maximum lon
		if(grid_i == n_cell_lat) grid_i = n_cell_lat - 1;
		if(grid_j == n_cell_lon) grid_j = n_cell_lon - 1;
		return new int[]{grid_i,grid_j};
	}
	

	public double[][] getBorderLonLat(int i, int j) {
		double[][] ll = new double[5][2];
		ll[0] = grid2LonLat(i, j);
		ll[1] = grid2LonLat(i+1, j);
		ll[2] = grid2LonLat(i+1, j+1);
		ll[3] = grid2LonLat(i, j+1);
		ll[4] = grid2LonLat(i, j);
		return ll;
	}
}

