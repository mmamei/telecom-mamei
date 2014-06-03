package dataset.db.insert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import utils.Config;
import utils.Logger;
import utils.Mail;

public class PLSTable {
	
	/*
	 * Considerazioni:
	 * ho fatto una tabella MyISAM perchè dovrebbe essere più veloce, perchè così è in un file separato e posso esportare il db più facilmente, supporta anche gli indici spaziali che però non uso
	 * alla fine non ho de-normalizzato il db mettendo le coordinate nella tabella pls. perchè alla fine la ricerca spaziale mi serve sul file di rete. dopo ad esempio trovo i celllac delle celle rilevanti 
	 * ho messo come indici solo (username,time) e (celllac,time).
	 * In questo modo sono coperte le query username, (username,time), celllac, (celllac,time).
	 * Sono scoperte le query su imsi perchè comunque abbastanza rare e perchè cmq ritornano molti record (es. se ho metà turisti e metà italiani l'indice mi dimezza al massimo il tempo non me lo riduce enormemente).
	 * Sono scoperte le query solo sul timestamp che su tutta la regione non sono utilissime. Piuttosto le combino con query doppie coperte dagli indici precedenti.
	 * Così sembra fare un giorno ogni 2-3 minuti con un occupazione di ram stabile.
	 * '
	 */
	
	
	/* NOTA BENE:
	 * Per come sono organizzati i file pls, ogni tabella conttiene l'ultima mezz'ora del giorno precendte e manca l'ultima mezz'ora del giorno 
	 * attuale (che è nel file successivo)
	 */
	
	
	private static final SimpleDateFormat F = new SimpleDateFormat("yyyyMMdd");
	
	
	public static void main(String[] args) throws Exception {
		
			PrintWriter out = new PrintWriter(new FileWriter("badtables.txt"));
			String name = "pls_lomb";
			Map<String,List<File>> filesByDay = new HashMap<String,List<File>>();
			organizeFilesByDay(new File("G:/DATASET/PLS/file_pls/file_"+name),filesByDay);
			int cont = 0;
			for(String day: filesByDay.keySet()) {
				cont ++;
				String tName = name+"_"+day;
				try {
				
					Statement s = DBConnection.getStatement();
					
					long start = System.currentTimeMillis();
					
					boolean proceed = true;
					ResultSet r = s.executeQuery("SELECT table_name FROM information_schema.tables");
					while(r.next()) {
						String tableName = r.getString("table_name"); 
						if(tableName.equals(tName)) {
							Logger.logln(cont+"/"+filesByDay.size()+": "+tName+" alreay processed!");
							proceed = false;
							break;
						}
					}
					r.close();
					if(proceed) {
						s.executeUpdate("create table "+tName+" (username VARCHAR(70), imsi VARCHAR(5), celllac VARCHAR(15), time DATETIME, PRIMARY KEY (username,time)) engine=MyISAM");
						
						Logger.log(day+" Inserting...");
						for(File f: filesByDay.get(day))
							insertFile(f, s, tName);
						Logger.log(cont+"/"+filesByDay.size()+": "+"Indexing...");
						
						s.executeUpdate("create index c_index ON "+tName+" (celllac,time)");
						
						Logger.logln(" Completed in "+(System.currentTimeMillis()-start)/(60*1000)+" mins!");
					}
					s.close();
				} catch(Exception e) {
					Logger.logln(">>>>>> Something went wrong with table "+tName);
					out.println(tName);
				}
				
				DBConnection.closeConnection();
			}
			out.close();
			Mail.send("completed!");
			
			// delete the file plsCoverageSpace.ser as new regions might have been added
			File odir = new File(Config.getInstance().base_folder+"/RegionMap");
			File f = new File(odir+"/plsCoverageSpace.ser");
			f.delete();
			
			Logger.logln("Done");
	}
	
	public static Set<String> getTables(String region,Calendar startTime,Calendar endTime) {
	
		
		// get the right tables
		Set<String> tables = new HashSet<String>();
		Calendar c = Calendar.getInstance();
		try {
			
			Calendar st = Calendar.getInstance();
			Calendar et = Calendar.getInstance();
			st.setTime(F.parse(F.format(startTime.getTime())));
			et.setTime(F.parse(F.format(endTime.getTime())));
			et.add(Calendar.HOUR_OF_DAY, 1); // the data of this day from 23:30 onward are on the next table. Adding 1 allows to consider also that table.
			
			Statement s = DBConnection.getStatement();
		
			ResultSet r = s.executeQuery("select table_name from information_schema.tables");
			while(r.next()) {
				String t = r.getString("table_name"); 
				
				if(t.startsWith(region)) {	
					//System.out.println(t);
					c.setTime(F.parse(t.substring(t.lastIndexOf("_")+1)));
					if(!st.after(c) && !et.before(c))
						tables.add(t);
				}
			}
			r.close();
			s.close();
			DBConnection.closeConnection();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tables;
	}
	
	
	static void organizeFilesByDay(File dir, Map<String,List<File>> map) {
		File[] items = dir.listFiles();
		for(int i=0; i<items.length;i++){
			File item = items[i];
			if(item.isFile()) {
				String n = item.getName();
				String day = getDay(Long.parseLong(n.substring(n.lastIndexOf("_")+1, n.indexOf(".zip"))));
				List<File> l = map.get(day);
				if(l == null) {
					l = new ArrayList<File>();
					map.put(day, l);
				}
				l.add(item);
			}
			else if(item.isDirectory())
				organizeFilesByDay(item, map);
		}
	}
	
	
	private static String getDay(long time) {
		return F.format(new Date(time));
	}	
	
	private static void insertFile(File plsFile, Statement s, String tName) {	
		ZipFile zf = null;
		BufferedReader br = null;
		try {
			zf = new ZipFile(plsFile);
			ZipEntry ze = (ZipEntry) zf.entries().nextElement();
			br = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)));
			String line;
			String[] fields;
			String username;
			String imsi;
			String celllac;
			long timestamp;		
			boolean badread = false;
			while((line=br.readLine())!=null)  {
				try {
					fields = line.split("\t");
					username = fields[0];
					imsi = fields[1];
					celllac = fields[2];
					timestamp = Long.parseLong(fields[3]);
					String query = "insert ignore into "+tName+" values('"+username+"','"+imsi+"',"+celllac+",'"+new Timestamp(timestamp)+"')";
					s.executeUpdate(query);
				} catch(Exception e) {
					if(!badread) {
						System.err.println("bad read in file "+plsFile);
						badread = true;
					}
				}
			}
		}catch(Exception e) {
			System.err.println("Problems with file: "+plsFile.getAbsolutePath());
			//e.printStackTrace();
		}
		try {
			br.close();
			zf.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
}
