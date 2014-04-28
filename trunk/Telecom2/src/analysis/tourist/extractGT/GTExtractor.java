package analysis.tourist.extractGT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import network.NetworkCell;
import network.NetworkMap;
import network.NetworkMapFactory;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.FileUtils;
import utils.Logger;
import area.region.Placemark;

public class GTExtractor {
	
	
	public static final String CLASSES = "Resident,Tourist,Commuter,Transit";
	
	//static final String FILE = "UserEventCounter/file_pls_fi_Firenze_cellXHour_cellXHour.csv";
	//static final String PLACEMARK = "Firenze";
	
	
	static final String FILE = "BASE/UserEventCounter/file_pls_ve_Venezia_cellXHour.csv";
	static final String PLACEMARK = "Venezia";
	
	public static void main(String[] args) throws Exception {
		
		String line;
		
		File f = FileUtils.getFile(FILE);
		if(f == null) {
			Logger.logln("Run UserEventCounterCellacXHour first!");
			System.exit(0);
		}
		
		BufferedReader br = new BufferedReader(new FileReader(f));
		
		//NetworkMap nm = NetworkMapFactory.getNetworkMap(Config.getInstance().pls_start_time);
		String user_id,mnt;
		int num_pls,num_days,days_interval;
		List<CalCell> list;
		
		Placemark placemark = Placemark.getPlacemark(PLACEMARK);
		
		Map<String,Profile> mp = new HashMap<String,Profile>(); // map profiles
		mp.put("Resident", new Resident(placemark));
		mp.put("Tourist", new Tourist(placemark));
		mp.put("Commuter", new Commuter(placemark));
		mp.put("Transit", new Transit(placemark));
		
		
		Map<String,Integer> mcont = new HashMap<String,Integer>(); // map profiles
		for(String p : mp.keySet())
			mcont.put(p, 0);
		
		Map<String,String> mu = new HashMap<String,String>(); // user profile
			
		int n_total = 0;
		
		// read header
		line = br.readLine();
		int tot_days = Integer.parseInt(line.substring(line.indexOf("=")+1).trim());
		
		while((line = br.readLine())!=null) {	
			try {
				String[] p = line.split(",");
				user_id = p[0];
				mnt = p[1];
				num_pls = Integer.parseInt(p[2]);
				num_days = Integer.parseInt(p[3]);
				days_interval = Integer.parseInt(p[4]);
				list = new ArrayList<CalCell>();
				// 2013-5-23:Sun:13:4018542484
				for(int i=5;i<p.length;i++) {
					String[] x = p[i].split(":|-");
					int y = Integer.parseInt(x[0]);
					int m = Integer.parseInt(x[1]);
					int d = Integer.parseInt(x[2]);
					int h = Integer.parseInt(x[4]);
					NetworkMap nm = NetworkMapFactory.getNetworkMap(new GregorianCalendar(y,m,d));
					NetworkCell nc = nm.get(Long.parseLong(x[5]));
					list.add(new CalCell(new GregorianCalendar(y,m,d,h,0),nc));
				}
				
				
				for(String prof: mp.keySet()) {
					if(mp.get(prof).check(user_id,mnt,num_pls,num_days,days_interval,list,tot_days)) {
						mu.put(user_id, prof);
						mcont.put(prof,mcont.get(prof)+1);
					}
				}
								
				n_total ++;
			} catch(Exception e) {
				System.err.println(line);
			}
		}
		br.close();
		
		for(String prof: mcont.keySet())
			Logger.logln(prof+" = "+mcont.get(prof)+"/"+n_total);
		
		
		File dir = FileUtils.createDir("BASE/Tourist");
		
		PrintWriter pw = new PrintWriter(new FileWriter(dir+"/"+PLACEMARK+"_gt_profiles.csv"));
		for(String user: mu.keySet())
			pw.println(user+","+mu.get(user));
		pw.close();
		
		CopyAndSerializationUtils.save(FileUtils.getFile("BASE/Tourist/"+PLACEMARK+"_gt_profiles.ser"), mu);
		
		Logger.logln("Done!");
	}	
}
