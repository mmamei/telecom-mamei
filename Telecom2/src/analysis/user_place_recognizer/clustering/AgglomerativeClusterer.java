package analysis.user_place_recognizer.clustering;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.gps.utils.LatLonPoint;
import org.gps.utils.LatLonUtils;

import utils.CopyAndSerializationUtils;
import utils.Sort;
import analysis.PLSEvent;
import analysis.user_place_recognizer.Cluster;
import analysis.user_place_recognizer.weight_functions.WeightOnTime;


public class AgglomerativeClusterer implements Clusterer {
	
	
	private int minPts;
	private double[][] weights;
	private double max_dist;
	private boolean generic = true;
	public AgglomerativeClusterer(int minPts, double[][] weights, double max_dist) {
		this.minPts = minPts;
		this.weights = weights;
		this.max_dist = max_dist;
		
		double w = weights[0][0];
		for(int i=0; i<weights.length;i++)
		for(int j=0; j<weights[i].length;j++)
			if(w != weights[i][j]) {
				generic = false;
				break;
			}
	}
	
	
	private HashMap<Integer, Cluster> clusters;
	public HashMap<Integer, Cluster> buildCluster(List<PLSEvent> events) {
		
		clusters = new HashMap<Integer, Cluster>();
		
		List<PLSEvent> sorted = sortByWeight(events);
		
		for(int i=0; i<sorted.size(); i++) {
			Cluster c = new Cluster();
			c.add(sorted.get(i));
			clusters.put(i, c);
		}
		

		Integer[] keys = new Integer[clusters.keySet().size()];
		keys = clusters.keySet().toArray(keys);
		
		for(int i=0; i<keys.length;i++) {
			if(clusters.get(keys[i]) == null) continue;
					
			for(int j=0;j<keys.length;j++) {
				if(keys[i] == keys[j] || clusters.get(keys[i]) == null || clusters.get(keys[j]) == null) continue;	
							
				if(dist(clusters.get(keys[i]),clusters.get(keys[j])) < max_dist && 
				   clusters.get(keys[i]).getAgglomerateWidth(clusters.get(keys[j])) < max_dist) {
				    
					//System.out.println(keys[i]+":"+clusters.get(keys[i]).size()+":"+w(clusters.get(keys[i]))+" <--- "+keys[j]+":"+clusters.get(keys[j]).size()+":"+w(clusters.get(keys[j])));	
					//System.out.print(s(getClusterCenter(clusters.get(keys[i])))+" + "+s(getClusterCenter(clusters.get(keys[j]))));
					
					clusters.get(keys[i]).addAll(clusters.get(keys[j]));
					clusters.remove(keys[j]);
					
					
					//System.out.println(" = "+s(getClusterCenter(clusters.get(keys[i]))));
				}
			}
		}
		
		keys = new Integer[clusters.keySet().size()];
		keys = clusters.keySet().toArray(keys);
		
		for(int k=0; k<keys.length;k++) {
			if(clusters.get(keys[k]).size() < minPts)
				clusters.remove(keys[k]);
		}
		
		
		return clusters;
	}	
	
	
	public String s(LatLonPoint p) {
		return "("+p.getLatitude()+","+p.getLongitude()+")";
	}
	
	
	public void print(Integer[] keys) {
		for(int k: keys)
			System.out.print(k+", ");
		System.out.println();
	}
	
	
	
	public Integer[] sort(Integer[] keys, int ki) {
		
		HashMap<Integer, Double> temp = new HashMap<Integer, Double>();
		LatLonPoint pi = clusters.get(ki).getCenter(weights);
		for(int k: keys) {
			if(clusters.get(k)!=null) {
				LatLonPoint pk = clusters.get(k).getCenter(weights);
				temp.put(k, LatLonUtils.getHaversineDistance(pi,pk));
			}
		}
		
		LinkedHashMap<Integer, Double> ordered = Sort.sortHashMapByValuesD(temp,null);
		Integer[] skeys = new Integer[ordered.keySet().size()];
		return ordered.keySet().toArray(skeys);	
	}
	
	public double dist(Cluster c1, Cluster c2) {
		
		// spatial distance
		
		LatLonPoint p1 = c1.getCenter(weights);
		double r1 = c1.getAvgCellRadius(weights);
		
		LatLonPoint p2 = c2.getCenter(weights);
		double r2 = c2.getAvgCellRadius(weights);
	
		double sdistance = LatLonUtils.getHaversineDistance(p2, p1) - (r1+r2);
		if(sdistance < 0) sdistance = 0;
		
		// temporal distance
		
		double tdistance = 0;
		
		double t1 = c1.w(weights) / c1.size();
		double t2 = c2.w(weights) / c2.size();
		tdistance = Math.abs(t2-t1);
		if((t2 >= 0 && t1 < 0) || (t2 < 0 && t1 >= 0)) tdistance = 1;
		else tdistance = 0;
		
		return sdistance + tdistance * (max_dist);	
	}
	
	
	public List<PLSEvent> sortByWeight(List<PLSEvent> events) {
		Collections.sort(events, new EventByWeightComparator(weights));
		return events;
	}
}


class EventByWeightComparator implements Comparator<PLSEvent> {
	private double[][] weights;
	
	public EventByWeightComparator(double[][] weights) {
		this.weights = weights;
	}
	
	public int compare(PLSEvent e1, PLSEvent e2) {
		if(w(e1) > w(e2))
			return 1;
		if(w(e1) < w(e2))
			return -1;
		return 0;
	}
	
	private double w(PLSEvent e) {
		Calendar c = new GregorianCalendar();
		c.setTimeInMillis(e.getTimeStamp());
		int day = c.get(Calendar.DAY_OF_WEEK) - 1;
		int h = c.get(Calendar.HOUR_OF_DAY);
		return weights[day][h];
	}
}
