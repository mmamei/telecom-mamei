package pls_parser;

import java.io.File;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import utils.Config;
import utils.Logger;

public class PLSParser {
	
	static Config conf = null;
	private static final int BUFFER_SIZE = 1048576;
	
	
	private static Calendar startTime = null;
	private static Calendar endTime = null;
	
	public static void parse(BufferAnalyzer ba) throws Exception {
		
		startTime = ba.getStartTime();
		endTime = ba.getEndTime();	
		
		String dir = Config.getInstance().pls_folder;
		long startTime = System.currentTimeMillis();
		
		analyzeDirectory(new File(dir),ba);
		
		long endTime = System.currentTimeMillis();
		int mins = (int)((endTime - startTime) / 60000);
		Logger.logln("Completed after "+mins+" mins");
	}
	
	
	static final String[] MONTHS = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	
	private static void analyzeDirectory(File directory, BufferAnalyzer analyzer) throws Exception{
		
		Map<String,String> allDays = new TreeMap<String,String>();
		File[] items = directory.listFiles();
		for(int i=0; i<items.length;i++){
			File item = items[i];
			if(item.isFile()) {
				Calendar end_cal = new GregorianCalendar();
				String n = item.getName();
				end_cal.setTimeInMillis(Long.parseLong(n.substring(n.lastIndexOf("_")+1, n.indexOf(".zip"))));
				
				Calendar begin_cal = (Calendar)end_cal.clone();
				begin_cal.add(Calendar.MINUTE, -30); // a pls file with time T contains events from T-30 min, to T
				
				if(end_cal.before(startTime) || begin_cal.after(endTime)) continue;
				
				String key = end_cal.get(Calendar.DAY_OF_MONTH)+"/"+MONTHS[end_cal.get(Calendar.MONTH)]+"/"+end_cal.get(Calendar.YEAR);
				String h = allDays.get(key);
				allDays.put(key, h==null? end_cal.get(Calendar.HOUR_OF_DAY)+"-" : h+end_cal.get(Calendar.HOUR_OF_DAY)+"-");
				
				analyzeFile(item, analyzer);
				if((i+1) % 10 == 0) Logger.logln(i+"/"+items.length+" done!");
			}
			else if(item.isDirectory())
				analyzeDirectory(item, analyzer);
		}
		
		//Logger.logln("Days in the dataset:");
		//for(String d:allDays.keySet()) 
		//	Logger.logln(d+" = "+allDays.get(d));
		
		
	}
	
	
	private static void analyzeFile(File plsFile, BufferAnalyzer analyzer) {	
		//System.out.println(plsFile.getAbsolutePath());
		ZipFile zf = null;
		InputStreamReader isr = null;
		try {
			zf = new ZipFile(plsFile);
			ZipEntry ze = (ZipEntry) zf.entries().nextElement();
			isr = new InputStreamReader(zf.getInputStream(ze));
			int charRead = 0;
			char[] read_buffer = new char[BUFFER_SIZE];
			char[] buffer = new char[3*BUFFER_SIZE];
			int x = -1;
			int remainedChars = 0;
			while(((charRead = isr.read(read_buffer)) > 0)){
				if(x==-1){
						x = analyzer.process(read_buffer, charRead);
						remainedChars = (charRead-x);
						System.arraycopy(read_buffer, x, buffer, 0, remainedChars);
				}
				else{
					System.arraycopy(read_buffer, 0, buffer, remainedChars, charRead);
					x = analyzer.process(buffer, (charRead+remainedChars));
					remainedChars = (charRead+remainedChars-x);
					System.arraycopy(buffer, x, buffer, 0, remainedChars);
				}
							
			}
		}catch(Exception e) {
			System.err.println("Problems wirh file: "+plsFile.getAbsolutePath());
			e.printStackTrace();
		}
		
		try {
			isr.close();
			zf.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
}
