package analysis.tourist.extractGT;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import network.NetworkCell;
import network.NetworkMap;
import network.NetworkMapFactory;
import utils.FileUtils;
import utils.Logger;

public class Extractor {
	
	
	static final String FILE = "";
	
	public static void main(String[] args) throws Exception {
		
		
		String line;
		BufferedReader br = FileUtils.getBR(FileUtils.getFileS("UserEventCounter")+"/"+FILE);
		if(br == null) {
			Logger.logln("Run UserEventCounterCellacXHour first!");
			System.exit(0);
		}
		
		NetworkMap nm = NetworkMapFactory.getNetworkMap();
		String user_id,mnt;
		int num_pls,num_days,days_interval;
		List<CalCell> list;
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
			
			boolean isResident = Resident.check(user_id,mnt,num_pls,num_days,days_interval,list);
			boolean isTourist = Tourist.check(user_id,mnt,num_pls,num_days,days_interval,list);
			boolean isCommuter = Commuter.check(user_id,mnt,num_pls,num_days,days_interval,list);
			boolean isTransit = Transit.check(user_id,mnt,num_pls,num_days,days_interval,list);
		}
		
	}
	
	
}
