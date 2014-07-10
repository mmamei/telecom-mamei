package dataset.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import region.Placemark;
import utils.Config;
import utils.Logger;
import utils.Mail;

public class UserEventCounter extends BufferAnalyzerConstrained {
	
	private Map<String,Integer> users_events;
	
	UserEventCounter(Placemark placemark, String user_list_name) {
		super(placemark,user_list_name);
		users_events = new HashMap<String,Integer>();
	}

	void analyze(String username, String imsi, String celllac, long timestamp, Calendar cal, String header) {
		Integer n = users_events.get(username);
		users_events.put(username, n == null ? 1 : n+1);
	}
	
	protected void finish() {
		try{
			System.out.println(users_events.size());
			File d = new File(Config.getInstance().base_folder+"/UserEventCounter");
			d.mkdirs();
			PrintWriter out = new PrintWriter(new FileWriter(d+"/"+this.getString()+"_count_timeframe_"+PLSParser.MIN_HOUR+"_"+PLSParser.MAX_HOUR+".csv"));
			for(String user: users_events.keySet())
				out.println(user+","+users_events.get(user));
			out.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void extractUsersAboveThreshold(File infile, File outfile, int threshold, int max_n_users) throws Exception {
		System.out.println("extract Users Above Threshold "+threshold);
		PrintWriter out = new PrintWriter(outfile);
		BufferedReader br = new BufferedReader(new FileReader(infile));
		String line;
		int count = 0;
		while((line=br.readLine())!=null){
			try {
				String[] x = line.split(",");
				String username = x[0];
				int n_events = Integer.parseInt(x[1]);
				if(n_events > threshold) {
					out.println(username);	
					count ++;
				}
				if(max_n_users > 0 && count > max_n_users)
					break;
					
			} catch(Exception e) {
				System.out.println("BAD LINE = "+line);
			}
		}
		br.close();
		out.close();
	}
	
	
	public static void percentAnalysis(File f) throws Exception {
		
		DescriptiveStatistics stats = new DescriptiveStatistics();
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line; 
		while((line = br.readLine())!=null) {
			try {
				double v = Double.parseDouble(line.substring(line.indexOf(",")+1));
				stats.addValue(v);
			} catch(Exception e) {
				System.out.println("BAD LINE = "+line);
			}
		}
		br.close();
		
		int ndays = (int)((Config.getInstance().pls_end_time.getTimeInMillis() - Config.getInstance().pls_start_time.getTimeInMillis()) / (1000 * 3600 * 24)); 
		
		
		for(int i=1; i<100;i++)
			System.out.println(i+","+stats.getPercentile(i)+" ==> "+stats.getPercentile(i)/ndays);
		System.out.println("TOT DAYS = "+ndays);
		System.out.println("TOT USERS = "+stats.getN());
	}
	
	
	public static void main2(String[] args) throws Exception {
		String region = "file_pls_lomb";
		Config.getInstance().pls_folder = Config.getInstance().pls_root_folder+"/"+region;
		Config.getInstance().pls_start_time = new GregorianCalendar(2014,Calendar.MARCH,1);
		Config.getInstance().pls_end_time = new GregorianCalendar(2014,Calendar.MARCH,30);
		percentAnalysis(new File(Config.getInstance().base_folder+"/UserEventCounter/"+region+"_count_timeframe_"+PLSParser.MIN_HOUR+"_"+PLSParser.MAX_HOUR+".csv"));
	}
	
	public static void getBogus() throws Exception {
		String region = "file_pls_fi";
		Config.getInstance().pls_folder = Config.getInstance().pls_root_folder+"/"+region;
		Config.getInstance().pls_start_time = new GregorianCalendar(2013,Calendar.JULY,1,0,0,0);
		Config.getInstance().pls_end_time = new GregorianCalendar(2013,Calendar.JULY,10,23,59,59);
		PLSParser.MIN_HOUR = 1;
		PLSParser.MAX_HOUR = 3;
		//new UserEventCounter(null,null).run();
		
		percentAnalysis(new File(Config.getInstance().base_folder+"/UserEventCounter/"+region+"_count_timeframe_"+PLSParser.MIN_HOUR+"_"+PLSParser.MAX_HOUR+".csv"));
		int threshold = 20;
		int max_users_retrieved = -1;
		String filename = region+"_bogus.txt";
		extractUsersAboveThreshold(new File(Config.getInstance().base_folder+"/UserEventCounter/"+region+"_count_timeframe_"+PLSParser.MIN_HOUR+"_"+PLSParser.MAX_HOUR+".csv"),new File(Config.getInstance().base_folder+"/UserEventCounter"+"/"+filename), threshold,max_users_retrieved);
	
		Logger.logln("Done!");
		Mail.send("UserEventCounter completed!");
	}

	
	public static void main5(String[] args) throws Exception {
		
		Config.getInstance().changeDataset("ivory-set3");
		String region = "file_pls_ivory";
		Config.getInstance().pls_folder = Config.getInstance().pls_root_folder+"/"+region;
		//new UserEventCounter(null,null).run();
		
		
		//percentAnalysis(new File(Config.getInstance().base_folder+"/UserEventCounter/"+region+"_count_timeframe_"+PLSParser.MIN_HOUR+"_"+PLSParser.MAX_HOUR+".csv"));
		int threshold = 2000;
		int max_users_retrieved = 10000;
		//String filename = region+"_bogus.txt";
		String filename = region+"_users_"+threshold+"_"+max_users_retrieved+".txt";
		extractUsersAboveThreshold(new File(Config.getInstance().base_folder+"/UserEventCounter/"+region+"_count_timeframe_"+PLSParser.MIN_HOUR+"_"+PLSParser.MAX_HOUR+".csv"),new File(Config.getInstance().base_folder+"/UserEventCounter"+"/"+filename), threshold,max_users_retrieved);
	
		Logger.logln("Done!");
		Mail.send("UserEventCounter completed!");
	}
	
	public static void main(String[] args) throws Exception{
		getBogus();
	}
	
	
}
