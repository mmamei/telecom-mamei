package analysis.tourist.extractGT;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import network.NetworkCell;
import network.NetworkMap;
import network.NetworkMapFactory;
import utils.FileUtils;
import utils.Logger;
import area.Placemark;

public class Extractor {
	
	
	//static final String FILE = "UserEventCounter/file_pls_fi_Firenze_cellXHour_cellXHour.csv";
	//static final String PLACEMARK = "Firenze";
	
	
	static final String FILE = "UserEventCounter/file_pls_ve_Venezia_cellXHour.csv";
	static final String PLACEMARK = "Venezia";
	
	public static void main(String[] args) throws Exception {
		
		
		String line;
		BufferedReader br = FileUtils.getBR(FILE);
		if(br == null) {
			Logger.logln("Run UserEventCounterCellacXHour first!");
			System.exit(0);
		}
		
		NetworkMap nm = NetworkMapFactory.getNetworkMap();
		String user_id,mnt;
		int num_pls,num_days,days_interval;
		List<CalCell> list;
		
		Placemark placemark = Placemark.getPlacemark(PLACEMARK);
		Profile residentP = new Resident(placemark);
		Profile touristP = new Tourist(placemark);
		Profile commuterP = new Commuter(placemark);
		Profile transitP = new Transit(placemark);
		
		Set<String> residents = new HashSet<String>();
		Set<String> tourists = new HashSet<String>();
		Set<String> commuters = new HashSet<String>();
		Set<String> transits = new HashSet<String>();
		
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
					NetworkCell nc = nm.get(Long.parseLong(x[5]));
					list.add(new CalCell(new GregorianCalendar(y,m,d,h,0),nc));
				}
				
				boolean isResident = residentP.check(user_id,mnt,num_pls,num_days,days_interval,list,tot_days);
				boolean isTourist = touristP.check(user_id,mnt,num_pls,num_days,days_interval,list,tot_days);
				boolean isCommuter = commuterP.check(user_id,mnt,num_pls,num_days,days_interval,list,tot_days);
				boolean isTransit = transitP.check(user_id,mnt,num_pls,num_days,days_interval,list,tot_days);
				
				if(isResident) residents.add(user_id);
				else if(isTourist) tourists.add(user_id);
				else if(isCommuter) commuters.add(user_id);
				else if(isTransit)  transits.add(user_id);
				
				n_total ++;
			} catch(Exception e) {
				System.err.println(line);
			}
		}
		
		save(PLACEMARK+"_Residents.csv",residents);
		save(PLACEMARK+"_Tourists.csv",tourists);
		save(PLACEMARK+"_Commuters.csv",commuters);
		save(PLACEMARK+"_Transits.csv",transits);
		
		Logger.logln("N. RESIDENTS = "+residents.size());
		Logger.logln("N. TOURISTS = "+tourists.size());
		Logger.logln("N. COMMUTERS = "+commuters.size());
		Logger.logln("N. IN TRANSIT = "+transits.size());	
		Logger.logln("N. TOTAL = "+n_total);	
		
		
		
	}
	
	
	public static void save(String name, Set<String> set) {
		PrintWriter pw = FileUtils.getPW("TouristData/GT", name);
		for(String u: set)
			pw.println(u);
		pw.close();
	}
	
	
}
