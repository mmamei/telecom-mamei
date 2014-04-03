package network;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import utils.Config;
import utils.FileUtils;
import utils.Logger;

public class NetworkMapFactory {
	private static final SimpleDateFormat F = new SimpleDateFormat("dd/MM/yyyy");
	
	private static Map<String,NetworkMap> mapnet = new HashMap<String,NetworkMap>();
	
	
	private static String getCalString(String f) {
		//dfl_network_20130516.bin
		String yyyy = f.substring(12,16);
		String MM = f.substring(16,18);
		String dd = f.substring(18,20);
		return dd+"/"+MM+"/"+yyyy;
	}

	
	private static Calendar getCalendar(String f) throws Exception{
		Calendar c = Calendar.getInstance();
		c.setTime(F.parse(getCalString(f)));
		return c;
	}
	
	
	private static String findClosestNetworkFile(String target_cal_string) {
		
		target_cal_string = target_cal_string.split(" ")[0];
		
		String best_file = null;
		
		File dir = FileUtils.getFile("BASE/NetworkMapParser");
		
		try {
			Calendar target_cal = Calendar.getInstance();
			target_cal.setTime(F.parse(target_cal_string));
			
			
			String[] files = dir.list();
			
			best_file = files[0];
			
			for(int i=1; i<files.length;i++) {
				Calendar cal = getCalendar(files[i]);
				long dt = Math.abs(cal.getTimeInMillis() - target_cal.getTimeInMillis());
				long best_dt = Math.abs(getCalendar(best_file).getTimeInMillis() - target_cal.getTimeInMillis());
				if(dt < best_dt) 
					best_file = files[i];
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return dir.getAbsolutePath()+"/"+best_file;
	}

	
	private static long first = Long.MAX_VALUE;;
	private static long last = 0;
	private static Calendar getBestTime(String dir) {
		File[] files = new File(dir).listFiles();
		for(File f: files) {
			if(f.isDirectory()) return getBestTime(f.getAbsolutePath());
			else {
				long time = Long.parseLong(f.getName().substring(f.getName().lastIndexOf("_")+1, f.getName().indexOf(".zip")));
				if(time < first) first = time;
				if(time > last) last = time;
			}
		}
		
		long config_first = Config.getInstance().pls_start_time.getTimeInMillis() ;
		long config_last = Config.getInstance().pls_end_time.getTimeInMillis() ;
		
		if(config_first > first) first = config_first;
		if(config_last < last) last = config_last;

		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis((first+last)/2);
		//System.out.println(cal.getTime());
		return cal;
	}
	
	public static NetworkMap getNetworkMap() {
		return getNetworkMap(getBestTime(Config.getInstance().pls_folder));
	}
	
	
	public static NetworkMap getNetworkMap(Calendar cal) {
		return getNetworkMap(F.format(cal.getTime()));
	}
	
	public static NetworkMap getNetworkMap(String cal) {
		
		String file = findClosestNetworkFile(cal);
		
		Logger.logln("!!!! Using network file "+file);
		
		NetworkMap nm = mapnet.get(file);
		if(nm == null) {
			nm = new NetworkMap(file);
			mapnet.put(file, nm);
		}
		return nm;
	}
	
	
	public static void main(String[] args) {
		NetworkMap nm = getNetworkMap();
		System.out.println(nm);
	}
}
