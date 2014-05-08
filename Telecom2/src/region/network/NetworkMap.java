package region.network;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.gps.utils.LatLonPoint;
import org.gps.utils.LatLonUtils;

import region.RegionI;
import visual.kml.KML;


public class NetworkMap {
	private HashMap<String, RegionI> hm;
	
	NetworkMap(String file) {
		try {
			ObjectInputStream in_network = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File(file))));
			hm = (HashMap<String, RegionI>)in_network.readObject();
			in_network.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public RegionI get(String cellac) {
		return hm.get(cellac);
	}
	
	public Collection<RegionI> getAll() {
		return hm.values();
	}
	
	
	public Set<RegionI> getCellsIn(double[] ll, double[] tr) {
		Set<RegionI> cells = new HashSet<RegionI>();
		
		double lon,lat;
		for(RegionI nc : hm.values()) {
			lat = nc.getLatLon()[0];
			lon = nc.getLatLon()[1];
			if(ll[0] < lat && lat < tr[0] && ll[1] < lon && lon < tr[1])
				cells.add(nc);
		}
	
		return cells;
	}
	
	public double getAvgCellRadiusAround(LatLonPoint c, double radius){
		
		double dist = 0;
		double count = 0;
		for(RegionI nc : hm.values()) {
			if(LatLonUtils.getHaversineDistance(nc.getCenterPoint(), c) < radius) {
				dist += nc.getRadius();
				count++;
			}
		}
		return dist/count;
	}
	
	
	public int getNumCells(LatLonPoint c, double radius) {
		int count = 0;
		for(RegionI nc : hm.values()) {
			if(LatLonUtils.getHaversineDistance(nc.getCenterPoint(), c) < radius) {
				count++;
			}
		}
		return count;
	}
	
	
	public static final double earth_radius = 6372.795477598;
	// radius in meters
	public void printKML(String file, double[] center, double radius) throws Exception {
		double d = Math.toDegrees(radius/1000/earth_radius);
		double[] ll = new double[]{center[0]-d,center[1]-d};
		double[] tr = new double[]{center[0]+d,center[1]+d};
		printKML(file,ll,tr);
	}
	
	public void printKML(String file, double[] ll, double[] tr) throws Exception {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		KML kml = new KML();
		String name = file.substring(file.lastIndexOf("/")+1,file.lastIndexOf("."));
		kml.printHeaderFolder(out, name);
		Set<RegionI> cells = getCellsIn(ll,tr);
		for(RegionI nc : cells)
			out.println(nc.toKml(""));
		kml.printFooterFolder(out);
		out.close();
	}
}
