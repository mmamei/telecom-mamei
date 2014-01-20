package analysis.tourist.extractGT;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import area.Placemark;
import network.NetworkCell;
import network.NetworkMap;
import network.NetworkMapFactory;
import utils.FileUtils;
import utils.Logger;

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
		Profile resident = new Resident(placemark);
		Profile tourist = new Tourist(placemark);
		Profile commuter = new Commuter(placemark);
		Profile transit = new Transit(placemark);
		
		int n_resident = 0;
		int n_tourist = 0;
		int n_commuter = 0;
		int n_transit = 0;
		int n_total = 0;
		
		// read header
		line = br.readLine();
		int tot_days = Integer.parseInt(line.substring(line.indexOf("=")+1).trim());
		
		while((line = br.readLine())!=null) {		
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
			
			boolean isResident = resident.check(user_id,mnt,num_pls,num_days,days_interval,list,tot_days);
			boolean isTourist = tourist.check(user_id,mnt,num_pls,num_days,days_interval,list,tot_days);
			boolean isCommuter = commuter.check(user_id,mnt,num_pls,num_days,days_interval,list,tot_days);
			boolean isTransit = transit.check(user_id,mnt,num_pls,num_days,days_interval,list,tot_days);
			
			if(isResident) n_resident ++;
			else if(isTourist) n_tourist++;
			else if(isCommuter) n_commuter++;
			else if(isTransit) n_transit++;
			
			n_total ++;
		}
		
		
		Logger.logln("N. RESIDENTS = "+n_resident);
		Logger.logln("N. TOURISTS = "+n_tourist);
		Logger.logln("N. COMMUTERS = "+n_commuter);
		Logger.logln("N. IN TRANSIT = "+n_transit);	
		Logger.logln("N. TOTAL = "+n_total);	
	}
	
	
}
