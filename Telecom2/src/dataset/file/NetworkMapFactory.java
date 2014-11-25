package dataset.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import region.RegionMap;
import utils.Config;
import dataset.NetworkMapFactoryI;

class NetworkMapFactory implements NetworkMapFactoryI  {
    
  
    private static NetworkMapFactory nmf;
	private static final SimpleDateFormat F = new SimpleDateFormat("dd/MM/yyyy");
	private static Map<String,RegionMap> mapnet = new HashMap<String,RegionMap>();
	
	private NetworkMapFactory() {
	}
	
	static NetworkMapFactory getInstance() {
		if(nmf == null) 
			nmf = new  NetworkMapFactory();
		return nmf;
	}
    
    
    private String getCalString(String f) {
            //dfl_network_20130516.bin
            String yyyy = f.substring(12,16);
            String MM = f.substring(16,18);
            String dd = f.substring(18,20);
            return dd+"/"+MM+"/"+yyyy;
    }

    
    public Calendar getCalendar(String f) {
            Calendar c = null;
            try{
                c = Calendar.getInstance();
                    c.setTime(F.parse(getCalString(f)));
            }catch(Exception e) {
                    System.err.println("--- "+f);
                    e.printStackTrace();
            }
            return c;
    }
    
    
    private String findClosestNetworkFile(String target_cal_string) {
            
            target_cal_string = target_cal_string.split(" ")[0];
            
            String best_file = null;
            
            File dir = new File(Config.getInstance().base_folder+"/NetworkMapParser");
            
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

    
    public RegionMap getNetworkMap(long time) {
    	
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(time);
            return getNetworkMap(cal);
    }
    
    
    public synchronized RegionMap getNetworkMap(Calendar calendar) {
            String cal = F.format(calendar.getTime());
    
            String file = findClosestNetworkFile(cal);
            
            //Logger.logln("!!!! Using network file "+file);
            
            RegionMap nm = mapnet.get(file);
            if(nm == null) {
                    try {
                            ObjectInputStream in_network = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File(file))));
                            nm = (RegionMap)in_network.readObject();
                            mapnet.put(file, nm);
                    } catch(Exception e) {
                            e.printStackTrace();
                    }
            }
            return nm;
    }
    
   
    public static void main(String[] args) {
    	
    		Config.getInstance().pls_start_time = new GregorianCalendar(2013,Calendar.JULY,31,0,0,0);
    		Config.getInstance().pls_end_time = new GregorianCalendar(2013,Calendar.JULY,31,23,59,59);
    		//Config.getInstance().pls_start_time = new GregorianCalendar(2014,Calendar.MARCH,1,0,0,0);
    		//Config.getInstance().pls_end_time = new GregorianCalendar(2014,Calendar.MARCH,31,23,59,59);
    	
    		NetworkMapFactory nmf = getInstance();
            RegionMap nm = nmf.getNetworkMap(Config.getInstance().pls_start_time);
            System.out.println(nm.getName());
            System.out.println(nm.getRegion("2939749220"));
    }
}