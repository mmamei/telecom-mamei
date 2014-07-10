package dataset.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import region.Placemark;
import utils.Config;
import utils.Logger;
import utils.Mail;
import analysis.UserTrace;

public class UserEventCounterCellacXHour extends BufferAnalyzerConstrained {
	
	
	private Map<String,UserTrace> users_info;

	
	UserEventCounterCellacXHour(Placemark placemark, String user_list_name) {
		super(placemark,user_list_name);
		users_info = new HashMap<String,UserTrace>();
	}
	
	
	UserTrace info;
	String day,dayw;
	void analyze(String username, String imsi, String celllac, long timestamp, Calendar cal,String header) {
		info = users_info.get(username);
		if(info == null) {
			info = new UserTrace(username,true);
			users_info.put(username, info);
		}
		
		info.addEvent(imsi,celllac,""+timestamp);
	}
	
	
	
	Set<String> days;
	private int getTotDays() {
		days = new HashSet<String>();
		for(UserTrace ui:users_info.values()) {
			days.addAll(ui.getDays());
		}
		return days.size();
	}
	
	//private static final SimpleDateFormat F = new SimpleDateFormat("dd/MM/yyyy");
	
	protected void finish() {
		try {
			File dir = new File(Config.getInstance().base_folder+"/UserEventCounter");
			dir.mkdirs();
			PrintWriter out = new PrintWriter(new FileWriter(dir+"/"+this.getString()+"_cellXHour.csv"));
			out.println("// TOT. DAYS = "+getTotDays());
			for(String user: users_info.keySet())
				//if(users_info.get(user).getNumDays() >= 14)
					out.println(user+","+users_info.get(user).getInfoCellXHour());
			out.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	

	public static void process(Placemark placemark) {
		BufferAnalyzerConstrained ba = new UserEventCounterCellacXHour(placemark,null);
		ba.run();
	}
	
	public static void process(String userListF) {
		BufferAnalyzerConstrained ba = new UserEventCounterCellacXHour(null,userListF);
		ba.run();
	}
	
	
	public static void main(String[] args) throws Exception {
		
		Config.getInstance().pls_folder = Config.getInstance().pls_root_folder+"/file_pls_fi"; 
		Config.getInstance().pls_start_time = new GregorianCalendar(2013,Calendar.JULY,1,0,0,0);
		Config.getInstance().pls_end_time = new GregorianCalendar(2013,Calendar.JULY,31,23,59,59);
		
		
		/*
		 * Questo mi serve per le operazioni density and flows perchè vedo le tracce degli utenti e quindi la loro density/flow
		 * all'interno della città.
		 */
	
		//process(Placemark.getPlacemark("Firenze"));
		
		/*
		 * Questo serve per l'analisi dei turisti. In questo modo trovo i dati di tutti i turisti che sono passati almeno una volta per la città.
		 * Ma estraggo anche i dati relativi a quando erano fuori dalla città. In questo modo, ad esempio, riesco ad estrarre anche informazioni sugli utenti in
		 * transito perchè veod dov'erano prima e dopo la città e quindi quanto tempo ci sono stati.
		 */
		
		process(Config.getInstance().base_folder+"/UserSetCreator/Firenze.csv");
		Mail.send("UserEventCounterCellacXHour completed!");
		Logger.logln("Done!");
	}	
	
	
	/*
	 * This main is places here for convenience. It just read the file and remove all the users producing few events
	 */
	public static void trim(Placemark p, int min_size) throws Exception {
		File dir = new File(Config.getInstance().base_folder+"/UserEventCounter");
		dir.mkdirs();
		BufferedReader br = new BufferedReader(new FileReader(dir+"/"+p.getName()+"_cellacXhour.csv"));
		PrintWriter out = new PrintWriter(new FileWriter(dir+"/"+p.getName()+"_cellacXhour_trim"+min_size+".csv"));
		String line;
		while((line = br.readLine()) != null) {
			int num_pls = Integer.parseInt(line.split(",")[2]);
			if(num_pls >= min_size)
				out.println(line);
		}
		br.close();
		out.close();
		Logger.logln("Done!");
	}
}