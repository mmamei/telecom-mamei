package analysis.place_recognizer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import network.NetworkCell;
import network.NetworkMap;
import network.NetworkMapFactory;

import org.gps.utils.LatLonPoint;
import org.gps.utils.LatLonUtils;

import analysis.PlsEvent;


public class Cluster implements Serializable {
	private List<PlsEvent> events;
	private Map<String,Double> weights;
	
	private static NetworkMap NM = null;
	
	public Cluster() {
		events = new ArrayList<PlsEvent>();
		weights = new HashMap<String,Double>();
	}
	
	
	public int size() {
		return events.size();
	}
	
	public void addAll(Cluster c) {
		events.addAll(c.getEvents());
		if(NM == null) NM = NetworkMapFactory.getNetworkMap(NetworkMapFactory.getCalendar(c.getEvents().get(0).getTime()));
	}
	
	public void add(PlsEvent e) {
		events.add(e);
		if(NM == null) NM = NetworkMapFactory.getNetworkMap(NetworkMapFactory.getCalendar(e.getTime()));
	} 
	
	public List<PlsEvent> getEvents() {
		return events;
	}
	
	public void addWeight(String label, double w) {
		weights.put(label, w);
	}
	
	public double getWeight(String label) {
		return weights.get(label);
	}
	
	public double totWeight() {
		double weight = 0;
		for(double w: weights.values())
			weight += w;
		return weight;
	}
	
	public double w(PlsEvent e, double[][] weights) {
		Calendar c = e.getCalendar();
		int day = c.get(Calendar.DAY_OF_WEEK) - 1;
		int h = c.get(Calendar.HOUR_OF_DAY);
		return weights[day][h];
	}
	
	public double w(double[][] weights) {
		double w = 0;
		for(PlsEvent e: events)
			w += w(e,weights);
		return w;
	}
	
	public double getAvgCellRadius(double[][] weights) {
		double tot_w = w(weights);
		double r = 0;
		for(PlsEvent e: events) {
			NetworkCell x = NM.get(e.getCellac());
			double f = tot_w > 0 ? w(e,weights) : 1;
			r += x.getRadius() * f;
		}
		double den = tot_w > 0 ? tot_w : size();
		return r / den;
	}
	
	public LatLonPoint getCenter(double[][] weights) {
		double aLat = 0;
		double aLon = 0;
		
		double tot_w = w(weights);
		for(PlsEvent e: events) {
			NetworkCell x = NM.get(e.getCellac());
			double f = tot_w > 0 ? w(e,weights) : 1;
			aLat += x.getBarycentreLatitude() * f;
			aLon += x.getBarycentreLongitude() * f;
		}		
		
		double den = tot_w > 0 ? tot_w : size();
		
		aLat = aLat / den;
		aLon = aLon / den;
		
		return new LatLonPoint(aLat, aLon);
	}
	
	public double calcWidth() {
		double maxLat=0, maxLon=0;
		double minLat = Double.MAX_VALUE, minLon = Double.MAX_VALUE;
		for(PlsEvent e: events){
			NetworkCell cell = NM.get(e.getCellac());
			maxLat = Math.max(maxLat, cell.getBarycentreLatitude());
			maxLon = Math.max(maxLon, cell.getBarycentreLongitude());
			minLat = Math.min(minLat, cell.getBarycentreLatitude());
			minLon = Math.min(minLon, cell.getBarycentreLongitude());
		}
		LatLonPoint p1 = new LatLonPoint(maxLat, maxLon);
		LatLonPoint p2 = new LatLonPoint(minLat, minLon);
		
		return LatLonUtils.getHaversineDistance(p1, p2);
	}
	
	public double getAgglomerateWidth(Cluster c2) {
		Cluster temp = new Cluster();
		temp.addAll(this);
		temp.addAll(c2);
		return temp.calcWidth();
	}
	
	
}
