package pls_parser;

import java.io.BufferedReader;
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
	
	static boolean QUIET = false;
	
	static Config conf = null;
	private static final int BUFFER_SIZE = 1048576;
	
	
	private static Calendar startTime = null;
	private static Calendar endTime = null;
	
	static String dir;
	static long sTime,eTime;
	static int mins;
	static void parse(BufferAnalyzer ba) throws Exception {
		
		startTime = ba.getStartTime();
		endTime = ba.getEndTime();	
		
		dir = Config.getInstance().pls_folder;
		sTime = System.currentTimeMillis();
		
		analyzeDirectory(new File(dir),ba);
		
		eTime = System.currentTimeMillis();
		mins = (int)((eTime - sTime) / 60000);
		//Logger.logln("Completed after "+mins+" mins");
	}
	
	
	static final String[] MONTHS = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	
	
	
	static Map<String,String> allDays = new TreeMap<String,String>();
	
	private static void analyzeDirectory(File directory, BufferAnalyzer analyzer) throws Exception{	
		//System.out.println(directory.getAbsolutePath());
		
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
				if((i+1) % 10 == 0) {
					if(!QUIET) Logger.logln(i+"/"+items.length+" done!");
					System.gc();
				}
			}
			else if(item.isDirectory())
				analyzeDirectory(item, analyzer);
		}
		
		//Logger.logln("Days in the dataset:");
		//for(String d:allDays.keySet()) 
		//	Logger.logln(d+" = "+allDays.get(d));
		
		
	}
	
	/*
	private static void analyzeFileSLOW(File plsFile, BufferAnalyzer analyzer) {	
		//System.out.println(plsFile.getAbsolutePath());
		ZipFile zf = null;
		BufferedReader br = null;
		try {
			zf = new ZipFile(plsFile);
			ZipEntry ze = (ZipEntry) zf.entries().nextElement();
			br = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)));
			String line;
			while((line=br.readLine())!=null) 
				analyzer.analyze(line);
		}catch(Exception e) {
			System.err.println("Problems wirh file: "+plsFile.getAbsolutePath());
			e.printStackTrace();
		}
		try {
			br.close();
			zf.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	*/
	
	static ZipFile zf = null;
	static InputStreamReader isr = null;
	static ZipEntry ze;
	static char[] read_buffer,buffer;
	static int charRead,x,remainedChars;
	private static void analyzeFile(File plsFile, BufferAnalyzer analyzer) {	
		//System.out.println(plsFile.getAbsolutePath());
		try {
			zf = new ZipFile(plsFile);
			ze = (ZipEntry) zf.entries().nextElement();
			isr = new InputStreamReader(zf.getInputStream(ze));
			charRead = 0;
			read_buffer = new char[BUFFER_SIZE];
			buffer = new char[3*BUFFER_SIZE];
			x = -1;
			remainedChars = 0;
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
