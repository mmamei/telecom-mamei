package d4d.pls_format.ivory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.gps.utils.LatLonPoint;
import org.gps.utils.LatLonUtils;

import d4d.pls_format.NetworkCell;

// sia il file di rete del set 2 che il file di rete (subprefetture) del set 3

public class MainNetworkFile {
	
	public static void main (String [] args) throws Exception{
	
		String file = "C:\\Users\\Alket\\junocode\\D4D\\dati\\SUBPREF_POS_LONLAT.TSV";
		//String file = "C:\\DATA\\d4d\\data\\SET2\\POS_SAMPLE_0.TSV";
		String line;
		BufferedReader  br = new BufferedReader(new FileReader(new File(file)));
		List<NetworkCell>rlist = new ArrayList<NetworkCell>();
		//SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss",Locale.ENGLISH);
		
		System.out.println(file);
		while((line = br.readLine())!= null){
			String [] r = line.split("\t");
			String cellname = "cellid_"+r[0];
			int bari = 15000;
			int lacid = 0;
			int cellid = Integer.parseInt(r[0]);
			double lat_ant = Double.parseDouble(r[1]);
			double lon_ant = Double.parseDouble(r[2]);
			int set = 1;
			double ott = 2.0;
			int nov = 0;
			double lat_bari = Double.parseDouble(r[1]);
			double lon_bari = Double.parseDouble(r[2]);
			int raggio = 15000;
			
			
			rlist.add(new NetworkCell(cellname,bari,lacid, cellid, lon_ant,lat_ant, set, ott, nov, lon_bari,lat_bari,  raggio));
		
		}br.close();
		print(rlist, "dfl_network_20120428.txt");
	}
	
	
public static void print(List<NetworkCell>p, String file) throws Exception{
		
		PrintWriter out = new PrintWriter(new FileWriter(new File(file)));
		
		for (int i = 0; i < p.size(); i++) {
			out.print(p.get(i).cellname+":");
			out.print(p.get(i).bari+":");
			out.print(p.get(i).lacid+":");
			out.print(p.get(i).cellid+":");
			out.print(p.get(i).lat_ant+":");
			out.print(p.get(i).lon_ant+":");
			out.print(p.get(i).sette+":");
			out.print(p.get(i).otto+":");
			out.print(p.get(i).nove+":");
			out.print(p.get(i).lat_bari+":");
			out.print(p.get(i).lon_bari+":");
			out.println(p.get(i).raggio);
		}
		out.close();
	}

}
