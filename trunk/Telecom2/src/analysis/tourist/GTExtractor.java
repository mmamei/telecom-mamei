package analysis.tourist;

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

import region.Placemark;
import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;
import analysis.PLSEvent;
import dataset.file.DataFactory;

public class GTExtractor {
	
	
	public static final String CLASSES = "Resident,Tourist,Commuter,Transit,Excursionist";
	
	
	static final String PLACEMARK = "Venezia";
	static final String FILE = Config.getInstance().base_folder+"/UserEventCounter/file_pls_ve_"+PLACEMARK+"_cellXHour.csv";
	static {
		Config.getInstance().pls_start_time = new GregorianCalendar(2013,Calendar.JULY,1,0,0,0);
		Config.getInstance().pls_end_time = new GregorianCalendar(2013,Calendar.JULY,31,23,59,59);
	}
	
	public static void main(String[] args) throws Exception {
		
		String line;
		
		File f = new File(FILE);
		if(f == null) {
			Logger.logln("Run UserEventCounterCellacXHour first!");
			System.exit(0);
		}
		
		BufferedReader br = new BufferedReader(new FileReader(f));
		
		//NetworkMap nm = NetworkMapFactory.getNetworkMap(Config.getInstance().pls_start_time);
		String user_id,mnt;
		int num_pls,num_days,days_interval;
		List<PLSEvent> list;
		
		Placemark placemark = Placemark.getPlacemark(PLACEMARK);
		
		Map<String,Profile> mp = new HashMap<String,Profile>(); // map profiles
		mp.put("Resident", new Resident(placemark));
		mp.put("Tourist", new Tourist(placemark));
		mp.put("Commuter", new Commuter(placemark));
		mp.put("Transit", new Transit(placemark));
		mp.put("Excursionist", new Excursionist(placemark));
		
		Map<String,Integer> mcont = new HashMap<String,Integer>(); // map profiles
		for(String p : mp.keySet())
			mcont.put(p, 0);
		
		Map<String,String> mu = new HashMap<String,String>(); // user profile
		
		int[][] gtConfMatrix = new int[mp.size()][mp.size()];
		int users_with_multiple_classes = 0;
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
				list = PLSEvent.getDataFormUserEventCounterCellacXHourLine(line);
				/*
				list = new ArrayList<PLSEvent>();
				// 2013-5-23:Sun:13:4018542484
				for(int i=5;i<p.length;i++) {
					String[] x = p[i].split(":|-");
					int y = Integer.parseInt(x[0]);
					int m = Integer.parseInt(x[1]) - 1; // beware! important calendar correction
					int d = Integer.parseInt(x[2]);
					int h = Integer.parseInt(x[4]);
					String time = String.valueOf(new GregorianCalendar(y,m,d,h,0).getTimeInMillis());
					list.add(new PLSEvent(user_id,mnt,x[5],time));
				}
				*/
				List<Integer> uprofiles = new ArrayList<Integer>();
				int how_many_classes = 0;
				int i = 0;
				for(String prof: mp.keySet()) {
					if(mp.get(prof).check(user_id,mnt,num_pls,num_days,days_interval,list,tot_days)) {
						how_many_classes++;
						mu.put(user_id, prof);
						mcont.put(prof,mcont.get(prof)+1);
						uprofiles.add(i);
					}
					i++;
				}
				
				if(how_many_classes > 1) {
					users_with_multiple_classes++;
					for(int ii: uprofiles) {
					for(int jj: uprofiles)
						if(ii!=jj) gtConfMatrix[ii][jj]++;
					}
				}	
				n_total ++;
				
				if(n_total % 10000 == 0) {
					System.out.println("Processed "+n_total+" users..."); 
					Logger.logln("USERS WITH MULTIPLE CLASSES "+users_with_multiple_classes+"/"+n_total);
				}
				
			} catch(Exception e) {
				System.err.println(line);
			}
		}
		br.close();
		
		for(String prof: mcont.keySet())
			Logger.logln(prof+" = "+mcont.get(prof)+"/"+n_total);
		Logger.logln("USERS WITH MULTIPLE CLASSES "+users_with_multiple_classes+"/"+n_total);
		Logger.logln("GT CONFUSION MATRIX");
		for(int i=0; i< gtConfMatrix.length;i++) {
			for(int j=0; j<gtConfMatrix.length;j++)
				System.out.print(gtConfMatrix[i][j]+"\t");
			System.out.println();
		}
		
		File dir = new File(Config.getInstance().base_folder+"/Tourist");
		dir.mkdirs();
		PrintWriter pw = new PrintWriter(new FileWriter(dir+"/"+PLACEMARK+"_gt_profiles.csv"));
		for(String user: mu.keySet())
			pw.println(user+","+mu.get(user));
		pw.close();
		
		CopyAndSerializationUtils.save(new File(Config.getInstance().base_folder+"/Tourist/"+PLACEMARK+"_gt_profiles.ser"), mu);
		
		Logger.logln("Done!");
	}	
}
