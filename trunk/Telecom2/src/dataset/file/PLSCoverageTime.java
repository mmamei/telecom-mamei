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
import utils.FileUtils;
import utils.Logger;
import dataset.PLSCoverageTimeI;

public class PLSCoverageTime implements PLSCoverageTimeI {
	
	static Config conf = null;
	

	public static void main(String[] args) {
		PLSCoverageTime apc = new PLSCoverageTime();
		/*
		Map<String,String> allDays = apc.compute();
		Logger.logln("Days in the dataset:");
		for(String d:allDays.keySet()) 
			Logger.logln(d+" = "+allDays.get(d));
		System.out.println("TOT = "+allDays.size());
		*/
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
		Map<String,List<String>> all =  apc.computeAll();
		for(String key: all.keySet()) 
			System.out.println(key+" -> "+apc.getNumYears(all.get(key)));
		
		
		List<String> x = all.get("file_pls_lomb");
		for(String d: x)
			System.out.println(d);
		
		System.out.println("**************************************************************************");
		
		String js = apc.getJSMap(all);
		//System.out.println(js);
	}
	
	public PLSCoverageTime() {
		
	}
	
	
	SimpleDateFormat f = new SimpleDateFormat("yyyy/MMM/dd",Locale.US);
	
	public String getJSMap(Map<String,List<String>> all) {
		StringBuffer sb = new StringBuffer();
		Calendar cal = Calendar.getInstance();
		for(String key: all.keySet()) {
			sb.append("var dataTable_"+key+" = new google.visualization.DataTable();");
			sb.append("dataTable_"+key+".addColumn({ type: 'date', id: 'Date' });");
			sb.append("dataTable_"+key+".addColumn({ type: 'number', id: 'PLS Coverage' });");
			sb.append("dataTable_"+key+".addRows([\n");
			List<String> dmap = all.get(key);
			
			for(String day: dmap) {
				try {
					cal.setTime(f.parse(day));
					String s = "[ new Date("+cal.get(Calendar.YEAR)+", "+cal.get(Calendar.MONTH)+", "+cal.get(Calendar.DAY_OF_MONTH)+"), 24],\n";
					
					if(key.equals("file_pls_lomb"))
						System.out.print(s);
					
					sb.append(s);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
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
		File odir = FileUtils.createDir("BASE/PLSCoverageTime");
		File f = new File(odir+"/plsCoverageTime.ser");
		if(f.exists()) {
			all = (Map<String,List<String>>)CopyAndSerializationUtils.restore(f);
		}
		else {
			File[] basedirs = FileUtils.getFiles("DATASET/PLS/file_pls");
			all = new HashMap<String,List<String>>();
			for(File basedir: basedirs) {
				for(File dir: basedir.listFiles()) {
					Logger.logln(dir.getAbsolutePath());
					List<String> val = all.get(dir.getName());
					if(val == null) {
						val = new ArrayList<String>();
						all.put(dir.getName(), val);
					}
					val.addAll(compute(dir));
				}
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
	
	static final String[] MONTHS = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	private static void analyzeDirectory(File directory, List<String> allDays) throws Exception {
		
		Logger.logln("\t"+directory.getAbsolutePath());
		
		File[] items = directory.listFiles();
		
		for(int i=0; i<items.length;i++){
			File item = items[i];
			if(item.isFile()) {
				Calendar cal = new GregorianCalendar();
				String n = item.getName();
				
				cal.setTimeInMillis(Long.parseLong(n.substring(n.lastIndexOf("_")+1, n.indexOf(".zip"))));
				
				int day =  cal.get(Calendar.DAY_OF_MONTH);
				String sday = day < 10 ? "0"+day : ""+day;
				
				String key = cal.get(Calendar.YEAR)+"/"+MONTHS[cal.get(Calendar.MONTH)]+"/"+sday;
				
				if(!allDays.contains(key))
					allDays.add(key);
			}
			else if(item.isDirectory())
				analyzeDirectory(item,allDays);
		}	
	}
}
