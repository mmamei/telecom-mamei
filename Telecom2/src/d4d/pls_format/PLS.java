package d4d.pls_format;

public class PLS implements Comparable<PLS>{
	
	public String hash;
	public int imsi;
	public int cellac;
	public long ts;
	
	public PLS(String h, int i, int c, long t){
		this.hash = h;
		this.imsi = i;
		this.cellac = c;
		this.ts = t;
	}
	
	public int compareTo(PLS o) {
		if (ts > o.ts)
			return 1; 
	    if (ts < o.ts) 
	    	return -1;
	    return 0;
	}
	public String toString(){
		return hash+","+imsi+","+cellac+","+ts;
	}

}
