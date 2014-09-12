package d4d.pls_format;

public class NetworkCell {

	public String cellname;
    public int bari;
    public int lacid;
    public int cellid;
    public double lat_ant;
    public double lon_ant;
    public int sette;
    public double otto;
    public int nove;
    public double lat_bari;
    public double lon_bari;
    public int raggio;
	
	public NetworkCell(String c, int b, int lac, int ceid,double la_a, double lo_a, int sett,
			double ott, int nov, double la_b, double lo_b, int r){
		
		this.cellname = c;
		this.bari = b;
		this.lacid = lac;
		this.cellid = ceid;
		this.lat_ant = la_a;
		this.lon_ant = lo_a;
		this.sette = sett;
		this.otto = ott;
		this.nove = nov;
		this.lat_bari = la_b;
		this.lon_bari = lo_b;
		this.raggio = r;
	}
}
