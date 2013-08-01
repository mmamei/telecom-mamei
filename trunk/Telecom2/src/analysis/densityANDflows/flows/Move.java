package analysis.densityANDflows.flows;

import area.region.Region;

public class Move {
	Region s;
	Region d;
	
	public Move(Region s, Region d) {
		this.s = s;
		this.d = d;
	}
	
	public String toString() {
		return s.getName()+" --> "+d.getName();
	}
	
	public boolean equals(Object  other) {
		Move o = (Move)other;
		return s.equals(o.s) && d.equals(o.d);
	}
	
	public boolean sameSourceAndDestination() {
		return s.equals(d);
	}
	
	public int hashCode() {
		return s.hashCode()+d.hashCode();
	}
	
}
