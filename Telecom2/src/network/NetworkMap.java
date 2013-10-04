package network;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.gps.utils.LatLonPoint;

import area.Placemark;

import utils.Config;
import utils.Logger;
import utils.kdtree.GenericPoint;
import utils.kdtree.KDTree;
import utils.kdtree.Point;
import utils.kdtree.RangeSearchTree;
import visual.kml.KML;


public class NetworkMap {
	private HashMap<Long, NetworkCell> hm;
	private KDTree<Double,Point<Double>,NetworkCell> kdtree;
	private RangeSearchTree<Double,Point<Double>,NetworkCell> rangetree;
	 
	
	private static NetworkMap net = null;
	
	
	private NetworkMap() {
		try { 
			ObjectInputStream in_network = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File(Config.getInstance().network_map_bin))));
			hm = (HashMap<Long, NetworkCell>)in_network.readObject();
			in_network.close();
			
			kdtree = new KDTree<Double,Point<Double>,NetworkCell>(2);
			rangetree = (RangeSearchTree<Double,Point<Double>,NetworkCell>)kdtree;
			
			for(NetworkCell cell: hm.values()){
				Point<Double> cellPoint = new GenericPoint<Double>(cell.getBarycentreLatitude(),cell.getBarycentreLongitude());
				kdtree.put(cellPoint, cell);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static NetworkMap getInstance() {
		if(net == null) 
			net = new NetworkMap();
		return net;
	}
	
	
	public NetworkCell get(long cellac) {
		return hm.get(cellac);
	}
	
	
	public Set<NetworkCell> getCellsIn(double[] ll, double[] tr) {
		Set<NetworkCell> cells = new HashSet<NetworkCell>();
		Iterator<Map.Entry<Point<Double>,NetworkCell>> iter = rangetree.iterator(toPoint(ll), toPoint(tr));
		while(iter.hasNext()) 
			cells.add(iter.next().getValue());
		return cells;
	}
	
	
	public static final double earth_radius = 6372.795477598;
	
	public double getAvgCellRadiusAround(LatLonPoint c, double radius){
		double d = Math.toDegrees(radius/1000/earth_radius);
		Point<Double> lower_point = new GenericPoint<Double>(c.getLatitude()-d,c.getLongitude()-d);
		Point<Double> upper_point = new GenericPoint<Double>(c.getLatitude()+d,c.getLongitude()+d);
		Iterator<Map.Entry<Point<Double>,NetworkCell>> iter = rangetree.iterator(lower_point, upper_point);
		double count = 0;
		double dist = 0;
		while(iter.hasNext()) {
			NetworkCell other = iter.next().getValue();
			dist += other.getRadius();
			count++;
		}
		return dist/count;
	}
	
	
	public int getNumCells(LatLonPoint c, double radius) {
		double d = Math.toDegrees(radius/1000/earth_radius);
		Point<Double> lower_point = new GenericPoint<Double>(c.getLatitude()-d,c.getLongitude()-d);
		Point<Double> upper_point = new GenericPoint<Double>(c.getLatitude()+d,c.getLongitude()+d);
		Iterator<Map.Entry<Point<Double>,NetworkCell>> iter = rangetree.iterator(lower_point, upper_point);
		int count = 0;
		while(iter.hasNext()) {
			iter.next();
			count++;
		}
		return count;
	}
	
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
		Iterator<Map.Entry<Point<Double>,NetworkCell>> iter = rangetree.iterator(toPoint(ll), toPoint(tr));
		while(iter.hasNext()) {
			NetworkCell nc = iter.next().getValue();
			out.println(nc.toKml());
		}
		kml.printFooterFolder(out);
		out.close();
	}
	
	private Point<Double> toPoint(double[] x) {
		return new GenericPoint<Double>(x[0],x[1]);	
	}
}
