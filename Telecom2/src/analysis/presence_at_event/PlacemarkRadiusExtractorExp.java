package analysis.presence_at_event;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import network.NetworkCell;
import network.NetworkMap;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.gps.utils.LatLonUtils;

import pls_parser.PLSEventsAroundAPlacemark;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;
import visual.java.GraphPlotter;
import analysis.PLSBehaviorInAnArea;
import analysis.PLSMap;
import area.CityEvent;
import area.Placemark;

public class PlacemarkRadiusExtractorExp {
	
	
	public static final int MAX_R = 1500;
	public static final int MIN_R = -500;
	public static final int STEP = 100;
	public static double[] RS = new double[]{-500,-400,-300,-200,-100,0,100,200,300,400,500,600,700,800,900,1000,1100,1200,1300,1400,1500};
	
	public static final String[] pnames = new String[]{
		"Juventus Stadium (TO)",
		"Stadio Olimpico (TO)",
		"Stadio Silvio Piola (NO)",
		"Stadio San Siro (MI)","Stadio Atleti Azzurri d'Italia (BG)","Stadio Mario Rigamonti (BS)","Stadio Franco Ossola (VA)",
		"Piazza San Carlo (TO)",
		"Piazza Castello (TO)",
		"Piazza Vittorio (TO)",
		"Parco Dora (TO)"
	};
	
	
	public static void main(String[] args) throws Exception { 
		
		String odir = Config.getInstance().base_dir+"/PlacemarkRadiusExtractor";
		new File(odir).mkdirs();
		PrintWriter out = new PrintWriter(new FileWriter(new File(odir+"/result_exp.csv")));
		
		NetworkMap nm = NetworkMap.getInstance();
		
		for(String pn : pnames) {
			Placemark p = Placemark.getPlacemark(pn);
			
			double nc = nm.getNumCells(p.center_point, 1000);
			double bestr = PlacemarkRadiusExtractor.round(-35 * nc + 1750);
			
			System.out.println(pn+" --> "+bestr);
		    out.println(pn+","+bestr);
		} 
		
		out.close();
		
		Logger.logln("Done");
	}
	
	
	public static Map<String,Double> readBestR() throws Exception {
		Map<String,Double> best = new HashMap<String,Double>();
		BufferedReader br = new BufferedReader(new FileReader(new File(Config.getInstance().base_dir+"/PlacemarkRadiusExtractor/result.csv")));
		String line;
		while((line = br.readLine())!=null) {
			String[] e = line.split(",");
			best.put(e[0], Double.parseDouble(e[1]));
		}
		br.close();
		return best;
	}
}
