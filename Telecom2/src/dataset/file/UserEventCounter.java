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

import utils.Config;
import utils.Logger;

public class UserEventCounter extends BufferAnalyzerConstrained {
	
	private Map<String,Integer> users_events;
	
	UserEventCounter(String placemark_name, String user_list_name) {
		super(placemark_name,user_list_name);
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
	
	public static void extractUsersAboveThreshol(File infile, File outfile, int n) throws Exception {
		PrintWriter out = new PrintWriter(outfile);
		BufferedReader br = new BufferedReader(new FileReader(infile));
		String line;
		while((line=br.readLine())!=null){
			try {
				String[] x = line.split(",");
				String username = x[0];
				int n_events = Integer.parseInt(x[1]);
				if(n_events > n) out.println(username);	
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
		
		PLSCoverageTime apc = new PLSCoverageTime();
		Map<String,List<String>> all =  apc.computeAll();
		String n = f.getName().substring(0,f.getName().indexOf("_count"));
		int ndays = all.get(n).size();
		
		for(int i=1; i<100;i++)
			System.out.println(i+","+stats.getPercentile(i)+" ==> "+stats.getPercentile(i)/ndays);
		System.out.println("TOT DAYS = "+ndays);
	}
	
	
	
	
	
	public static void main(String[] args) throws Exception {
		
		
		
		Config.getInstance().pls_folder = Config.getInstance().pls_root_folder+"/file_pls_lomb";
		Config.getInstance().pls_start_time = new GregorianCalendar(2014,2,1);
		Config.getInstance().pls_end_time = new GregorianCalendar(2014,3,30);
		PLSParser.MIN_HOUR = 1;
		PLSParser.MAX_HOUR = 3;
		UserEventCounter ba = new UserEventCounter(null,null);
		ba.run();
		/*
		percentAnalysis(FileUtils.getFile("BASE/UserEventCounter/file_pls_piem_count_timeframe_1_3.csv"));
		
		extractUsersAboveThreshol(FileUtils.getFile("BASE/UserEventCounter/file_pls_piem_count_timeframe_1_3.csv"),
								  new File(FileUtils.getFile("BASE/UserEventCounter")+"/file_pls_piem_bogus.txt"), 100);
		*/
		Logger.logln("Done!");
	}
	
}
