package dataset.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import utils.Config;
import utils.Logger;

public class PLSParser {
	
	
	public static int MIN_HOUR = 0;
	public static int MAX_HOUR = 25;
	
	
	public static boolean REMOVE_BOGUS = true;
	
	private static boolean QUIET = false;
	
	//private static Config conf = null;
	//private static final int BUFFER_SIZE = 1048576;
	
	
	private static Calendar startTime = null;
	private static Calendar endTime = null;
	
	private static String dir;
	private static long sTime,eTime;
	private static int mins;
	
	static void parse(BufferAnalyzer ba) throws Exception {
		
		startTime = ba.getStartTime();
		endTime = ba.getEndTime();	
		
		dir = Config.getInstance().pls_folder;
		sTime = System.currentTimeMillis();
		
		analyzeDirectory(new File(dir),ba,null);
		
		eTime = System.currentTimeMillis();
		mins = (int)((eTime - sTime) / 60000);
		//Logger.logln("Completed after "+mins+" mins");
	}
	
	
	//private  static final String[] MONTHS = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	
	
	
	//private static Map<String,String> allDays = new TreeMap<String,String>();
	
	private static void analyzeDirectory(File directory, BufferAnalyzer analyzer, Set<String> bogus) throws Exception{	
		

		if(REMOVE_BOGUS && bogus == null) {
			bogus = new HashSet<String>();
			System.out.println("Loading bogus users ... "+Config.getInstance().pls_folder);
			System.out.println(directory.getAbsolutePath());
			String d = directory.getAbsolutePath().substring(Config.getInstance().pls_root_folder.length()+1);
			if(d.indexOf("\\") > 0) d = d.substring(0,d.indexOf("\\"));
			File f = new File(Config.getInstance().base_folder+"/UserEventCounter/"+d+"_bogus.txt");
			if(f.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(f));
				String line;
				while((line=br.readLine())!=null) 
					bogus.add(line);
				br.close();
			}
			else {
				System.err.println(Config.getInstance().base_folder+"/UserEventCounter/"+d+"_bogus.txt NOT FOUND");
			}
		}
		
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
				

				
				if(end_cal.get(Calendar.HOUR_OF_DAY) < MIN_HOUR || begin_cal.get(Calendar.HOUR_OF_DAY) > MAX_HOUR) continue;
				
				if(!QUIET) System.out.println(n+" ==> "+begin_cal.getTime()+", "+end_cal.getTime());
				
				//String key = end_cal.get(Calendar.DAY_OF_MONTH)+"/"+MONTHS[end_cal.get(Calendar.MONTH)]+"/"+end_cal.get(Calendar.YEAR);
				//String h = allDays.get(key);
				//allDays.put(key, h==null? end_cal.get(Calendar.HOUR_OF_DAY)+"-" : h+end_cal.get(Calendar.HOUR_OF_DAY)+"-");
				
				analyzeFile(item, analyzer,bogus);
				if((i+1) % 10 == 0) 
					Thread.sleep(1000);
			}
			else if(item.isDirectory())
				analyzeDirectory(item, analyzer,bogus);
		}
		
		//Logger.logln("Days in the dataset:");
		//for(String d:allDays.keySet()) 
		//	Logger.logln(d+" = "+allDays.get(d));
		
		
	}
	
	
	private static void analyzeFile(File plsFile, BufferAnalyzer analyzer, Set<String> bogus) {	
		//System.out.println(plsFile.getAbsolutePath());
		ZipFile zf = null;
		BufferedReader br = null;
		try {
			zf = new ZipFile(plsFile);
			ZipEntry ze = (ZipEntry) zf.entries().nextElement();
			br = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)));
			String line;
			String u;
			while((line=br.readLine())!=null) { 
				u = line.substring(0,line.indexOf("\t"));
				if(bogus==null || !bogus.contains(u)) analyzer.analyze(line);
			}
		}catch(Exception e) {
			System.err.println("Problems with file: "+plsFile.getAbsolutePath());
			e.printStackTrace();
		}
		try {
			br.close();
			zf.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	private static ZipFile zf = null;
	private static InputStreamReader isr = null;
	private static ZipEntry ze;
	private static char[] read_buffer,buffer;
	private static int charRead,x,remainedChars;
	
	
	private static void analyzeFile(File plsFile, BufferAnalyzer analyzer, Set<String> bogus) {	
		
		//System.out.println(">>> "+plsFile.getAbsolutePath());
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
						x = process(analyzer,bogus,read_buffer, charRead);
						remainedChars = (charRead-x);
						System.arraycopy(read_buffer, x, buffer, 0, remainedChars);
				}
				else{
					System.arraycopy(read_buffer, 0, buffer, remainedChars, charRead);
					x = process(analyzer,bogus,buffer, (charRead+remainedChars));
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
	
	
	private static int process(BufferAnalyzer analyzer, Set<String> bogus, char[] buffer, int length){
		int start = 0;
		String s,u;
		for(int i=0; i<length; i++){
			if(buffer[i]=='\n'){ // QUESTO A VOLTE GENERA UN BUG NEL CASO LA LINEA TERMINA CON CR-LF PIUTTOSTO CHE SOLO LF
				s = new String(buffer, start, i-start);
				try {
					u = s.substring(0,s.indexOf("\t"));
					if(!bogus.contains(u))
						analyzer.analyze(s);
				} catch(Exception e) {
					System.err.println("BAD READ = "+s);
				}
				start = i+1;
			}
		}
		return start;
	}
	*/
}
