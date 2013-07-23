package area;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;

import org.gps.utils.LatLonPoint;
import org.gps.utils.LatLonUtils;

import utils.Colors;
import utils.Config;
import utils.Logger;
import visual.KMLSquare;
import visual.Kml;


public class SpaceGrid {
	
	// PRECOMPUTED
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
	
	
	public double[] grid2geo(int i, int j) {
		
		double cell_lon = min_lon + j * d_lon;
		double cell_lat = min_lat + i * d_lat;
		
		return new double[]{cell_lon,cell_lat};
	}
	
	
	/*
	public List<Integer>[][] divideInRegions(TweetDataset data) throws Exception {
		
		
		//System.out.println("d_lat = "+d_lat+", d_lon = "+d_lon);
		
		List<Integer>[][] grid = new List[N_CELL_LAT][N_CELL_LON];
		for(int i=0; i<N_CELL_LAT;i++)
		for(int j=0; j<N_CELL_LON;j++)
			grid[i][j] = new ArrayList<Integer>();
		
		int i=0;
		for (Iterator<Tweet> it = data; it.hasNext(); ) {
			Tweet t = it.next();
			int[] ij = getGridCoord(t);
			grid[ij[0]][ij[1]].add(i);
			i++;
 		}
		
		return grid;
	}
	*/
	
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
	
	private static final DecimalFormat DF = new DecimalFormat("##.###");
	public void draw(String title) throws Exception {
		String dir = Config.getInstance().base_dir+"/SpaceGrid";
		File d = new File(dir);
		if(!d.exists()) d.mkdirs();
		
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(dir+"/"+title+".kml")));
		Kml kml = new Kml();
		KMLSquare kmlsq = new KMLSquare();
		kml.printHeaderDocument(out, title);
		
		for(int i=0; i<n_cell_lon;i++)
		for(int j=0; j<n_cell_lat;j++) {
			double[][] ll = new double[5][2];
			ll[0] = grid2geo(i, j);
			ll[1] = grid2geo(i+1, j);
			ll[2] = grid2geo(i+1, j+1);
			ll[3] = grid2geo(i, j+1);
			ll[4] = grid2geo(i, j);
			String desc = "("+DF.format(ll[0][0])+";"+DF.format(ll[0][1])+")<br>" +
						  "("+DF.format(ll[1][0])+";"+DF.format(ll[1][1])+")<br>" +
						  "("+DF.format(ll[2][0])+";"+DF.format(ll[2][1])+")<br>" +
						  "("+DF.format(ll[3][0])+";"+DF.format(ll[3][1])+")<br>";
			out.println(kmlsq.draw(ll, i+","+j, "000000ff", "ffffffff", desc));
		}
		kml.printFooterDocument(out);
		out.close();
	}
	
	public void draw(String title,double[][] vals) throws Exception {
		
		
		// normalize vals
		double max  = 0;
		for(int i=0;i<vals.length;i++)
		for(int j=0;j<vals[i].length;j++)
			max = Math.max(max, vals[i][j]);
		
		
		
		double[][] norm = new double[vals.length][vals[0].length];
		for(int i=0;i<vals.length;i++)
		for(int j=0;j<vals[i].length;j++)
			norm[i][j] = vals[i][j] / max;
		
		String dir = Config.getInstance().base_dir+"/SpaceGrid";
		File d = new File(dir);
		if(!d.exists()) d.mkdirs();
		
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(dir+"/"+title+".kml")));
		Kml kml = new Kml();
		KMLSquare kmlsq = new KMLSquare();
		kml.printHeaderDocument(out, title);
		
		for(int i=0; i<n_cell_lon;i++)
		for(int j=0; j<n_cell_lat;j++) {
			double[][] ll = new double[5][2];
			ll[0] = grid2geo(i, j);
			ll[1] = grid2geo(i+1, j);
			ll[2] = grid2geo(i+1, j+1);
			ll[3] = grid2geo(i, j+1);
			ll[4] = grid2geo(i, j);
			out.println(kmlsq.draw(ll, "vals["+i+"]["+j+"] = "+(int)vals[i][j], Colors.val01_to_color(norm[i][j]), "44aaaaaa", "..."));
		}
		kml.printFooterDocument(out);
		out.close();
	}
	
	
	public static void main(String[] args) throws Exception {
		double[][] bbox = new double[][]{{7.494789211677311, 44.97591738081519},{7.878659418860384, 45.16510171374535}};
		SpaceGrid sg = new SpaceGrid(bbox[0][0],bbox[0][1],bbox[1][0],bbox[1][1],20,20);
		sg.draw("Torino");
		Logger.logln("Done!");
	}
	
	
	
	
}
