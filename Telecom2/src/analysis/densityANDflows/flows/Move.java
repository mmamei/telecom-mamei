package analysis.densityANDflows.flows;

import java.util.HashMap;
import java.util.Map;

import region.Region;
import region.RegionI;

public class Move {
	RegionI s;
	RegionI d;
	boolean directed;
	
	
	public Move(RegionI s, RegionI d) {
		this(s, d, true);
	}
	
	public Move(RegionI s, RegionI d, boolean directed) {
		this.s = s;
		this.d = d;
		this.directed = directed;
	}
	
	public String toString() {
		if(directed)
			return s.getName()+" --> "+d.getName();
		else 
			return s.getName()+" --- "+d.getName();
	}
	
	public String toCoordString() {
		return s.getLatLon()[0]+" "+s.getLatLon()[1]+";"+d.getLatLon()[0]+" "+d.getLatLon()[1];
	}
	
	
	public boolean equals(Object  other) {
		Move o = (Move)other;
		
		if(directed && o.directed)
			return s.equals(o.s) && d.equals(o.d);
		if(!directed && !o.directed)
			return (s.equals(o.s) && d.equals(o.d)) || (s.equals(o.d) && d.equals(o.s)) ;
		else
			return false;
	}
	
	public boolean sameSourceAndDestination() {
		return s.equals(d);
	}
	
	public int hashCode() {
		return s.hashCode()+d.hashCode();
	}
	
	
	// main for simple testing purposes
	public static void main(String[] args) {
		boolean directed = true;
		Region r1 = new Region("a","");
		Region r2 = new Region("b","");
		Region r3 = new Region("a","");
		Move m1 = new Move(r1,r2,directed);
		//Move m2 = new Move(r3,r2,directed);
		//System.out.println(m1.equals(m2));
		Move m3 = new Move(r2,r1,directed);
		System.out.println(m1.equals(m3));
		
		Map<Move,String> hm = new HashMap<Move,String>();
		
		hm.put(m1, "1");
		//hm.put(m2, "2");
		hm.put(m3, "3");
		
	
		
		for(Move m:hm.keySet())
			System.out.println(m+" --> "+hm.get(m));
		
		Map<Move,Double> list_od = new HashMap<Move,Double>();
		list_od.put(m1, 3.0);
		list_od.put(m3, 3.0);
		
		Map<Move,Double> list_od_undirected = new HashMap<Move,Double>();
		// change the list_od so that a --> b and b-->a are merged together
		for(Move m: list_od.keySet()) {
			Move m2 = new Move(m.s,m.d,false);
			Double v2 = list_od_undirected.get(m2);
			if(v2 == null) v2 = 0.0;
			v2 += list_od.get(m);
			list_od_undirected.put(m2, v2);
		}
		list_od = list_od_undirected;
		
		
		for(Move m:list_od.keySet())
			System.out.println(m+" = "+list_od.get(m));
		
	}
	
	
}
