package dataset.file;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;
import dataset.PLSCoverageTimeI;

 class PLSCoverageTime implements PLSCoverageTimeI {
	
	static Config conf = null;
	

	public static void main(String[] args) {
		PLSCoverageTime apc = new PLSCoverageTime();
		
		Config.getInstance().pls_folder = Config.getInstance().pls_root_folder+"/file_pls_pu"; 
		List<String> allDays = apc.compute();
		Logger.logln("Days in the dataset:");
		for(String d:allDays) 
			Logger.logln(d);
		System.out.println("TOT = "+allDays.size());
		
		/*
		Map<String,Map<String,String>> all =  apc.computeAll();
		for(String file: all.keySet()) {
			Map<String,String> allDays = all.get(file);
			Logger.logln("Days in the dataset "+file+":");
			for(String d:allDays.keySet()) 
				Logger.logln(d+" = "+allDays.get(d));
			System.out.println("TOT = "+allDays.size());
		}
		*/
		
		
		/*
		Map<String,List<String>> all =  apc.computeAll();
		for(String key: all.keySet()) 
			System.out.println(key+" -> "+apc.getNumYears(all.get(key)));
		
		
		List<String> x = all.get("file_pls_ve");
		for(String d: x)
			System.out.println(d);
		
		System.out.println("*********************************FILE************************************");
		
		String js = apc.getJSMap(all);
		//System.out.println(js);
		 * 
		 */
	}
	
	
	private static SimpleDateFormat f = new SimpleDateFormat("yyyy/MMM/dd",Locale.US);
	
	public String getJSMap(Map<String,List<String>> all) {
		Calendar cal = Calendar.getInstance();
		StringBuffer sb = new StringBuffer();
		
		for(String key: all.keySet()) {
			sb.append("var dataTable_"+key+" = new google.visualization.DataTable();");
			sb.append("dataTable_"+key+".addColumn({ type: 'date', id: 'Date' });");
			sb.append("dataTable_"+key+".addColumn({ type: 'number', id: 'PLS Coverage' });");
			sb.append("dataTable_"+key+".addRows([\n");
			List<String> dmap = all.get(key);
			
			int one_year = 0;
			for(String d: dmap) {
				try {
					cal.setTime(f.parse(d));
					int year = cal.get(Calendar.YEAR);
					if(one_year == 0) one_year = year;
					int month = cal.get(Calendar.MONTH);
					int day = cal.get(Calendar.DAY_OF_MONTH);
					String s = "[ new Date("+year+", "+month+", "+day+"), 24 ],\n";
					//System.out.println(d+"-->"+s);
					sb.append(s);
				} catch(Exception e) {
					System.err.println(d);
					//e.printStackTrace();
				}
			}
			sb.append("[ new Date("+one_year+", 0, 1), 0 ],\n");
			sb.append("]);\n");
		}
		return sb.toString();
	}
	
	public int getNumYears(List<String> dmap) {
		int min_year = 3000;
		int max_year = 0;
		for(String d: dmap) {
			int year = Integer.parseInt(d.substring(0,d.indexOf("/")));
			min_year = Math.min(min_year, year);
			max_year = Math.max(max_year, year);
		}
		return max_year - min_year + 1;
	}

	
	
	public Map<String,List<String>> computeAll() {
		
		Map<String,List<String>> all;
		File odir = new File(Config.getInstance().base_folder+"/PLSCoverageTime");
		odir.mkdirs();
		
		File f = new File(odir+"/plsCoverageTime.ser");
		if(f.exists()) {
			all = (Map<String,List<String>>)CopyAndSerializationUtils.restore(f);
		}
		else {
			
			
			File basedir = new File(Config.getInstance().pls_folder);
			all = new HashMap<String,List<String>>();
			for(File dir: basedir.listFiles()) {
					//Logger.logln(dir.getAbsolutePath());
					List<String> val = all.get(dir.getName());
					if(val == null) {
						val = new ArrayList<String>();
						all.put(dir.getName(), val);
					}
					val.addAll(compute(dir));
				}
			CopyAndSerializationUtils.save(f, all);
		}
		return all;
	}
   
	
	
	public List<String> compute() {
		File dir = new File(Config.getInstance().pls_folder);
		return compute(dir);
	}
	
	public List<String> compute(File dir) {	
		List<String> allDays = new ArrayList<String>();
		try {
			analyzeDirectory(dir,allDays);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return allDays;
	}
	
	private static void analyzeDirectory(File directory, List<String> allDays) throws Exception {
		
		Logger.logln("\t"+directory.getAbsolutePath());
		
		File[] items = directory.listFiles();
		
		for(int i=0; i<items.length;i++){
			File item = items[i];
			if(item.isFile()) {
				Calendar cal = new GregorianCalendar();
				String n = item.getName();
				try {
				cal.setTimeInMillis(Long.parseLong(n.substring(n.lastIndexOf("_")+1, n.indexOf(".zip"))));
				} catch(Exception e) {
					System.out.println("BAD FILE = "+item);
				}


				String key = f.format(cal.getTime());
				
				if(!allDays.contains(key))
					allDays.add(key);
			}
			else if(item.isDirectory())
				analyzeDirectory(item,allDays);
		}	
	}
}
