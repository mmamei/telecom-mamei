package dataset.db.insert;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/*
 * In questi piccoli il test il db risulta più veloce di circa 3 volte.
 * Poi la chache di MySQl fa si che se ripeto il test, la query successiva milgiora di 2 ordini di grandezza.
 * Quest'ultima cosa potrebbe portare a evitare tutti i file temporanei che genero perchè tanto la volta dopo che 
 * faccio la query questa va veloce.
 */


public class DBPerformanceTest {
	public static void main(String[] args) throws Exception {
		String name = "pls_ve";
		Map<String,List<File>> filesByDay = new HashMap<String,List<File>>();
		PLSTable.organizeFilesByDay(new File("G:/DATASET/PLS/file_pls/file_"+name),filesByDay);
		
		/* some users from 20130721
		cdf076d5a7b8cfae8eac2fbddce259535b7753189916e3ccf194195563d4
		61877134882df04a2139d44dad2692995f5856a3b3122b71bf769b85262529f2
		49186bf7b7457c7548091b9bd7b49df6132c819af8a3478a87d0a46e1b57c3
		adc7fd26114ddb4a15fdb86269bfd77d8179e2344dd64e91721c3c61dddccca
		b929c586721d9477d84bd6be52eb6559a7abf0543fcd812d3477d4ef8cf320
		9941d8daba3390846bf8a5f7c7f233a53d8e71458ec2ca30959681ba569d8a4
		9fc46499c5ec9d7b1e6f3f74cde546d6f592d464dd988247291de54877ec9a3
		c14a18cbf825275826e1a7cc7adda6e9e2e7b64c988d023ef7dbaede9647a65
		*/
		
		String user = "9fc46499c5ec9d7b1e6f3f74cde546d6f592d464dd988247291de54877ec9a3";
		String day = "20130721";
		
		getUserEventsViaFile(user,filesByDay.get(day));
		getUserEventsViaDB(user,name+"_"+day);
		
		String celllac = "1972911417";
		getCellEventsViaFile(celllac,filesByDay.get(day));
		getCellEventsViaDB(celllac,name+"_"+day);
		
	}
	
	
	static void getUserEventsViaDB(String user, String table) {
		long startTime = System.currentTimeMillis();
		try {
			Statement s = DBConnection.getStatement();
			ResultSet r = s.executeQuery("select * from "+table+" where username = '"+user+"'");
			while(r.next()) {
				String celllac = r.getString("celllac");
				String timestamp = r.getString("time");
				//System.out.println(user+","+celllac+","+timestamp);
			}
			r.close();
			s.close();
			DBConnection.closeConnection();
		} catch(Exception e) {
			e.printStackTrace();
		}
		long endTime = System.currentTimeMillis();
		System.out.println("db query completes in "+1.0 * (endTime - startTime) / (1000)+" sec");
	}
	
	
	static void getCellEventsViaDB(String celllac, String table) {
		long startTime = System.currentTimeMillis();
		try {
			Statement s = DBConnection.getStatement();
			ResultSet r = s.executeQuery("select * from "+table+" where celllac = '"+celllac+"'");
			int cont = 0;
			while(r.next()) {
				cont ++;
			}
			System.out.println("N. Events = "+cont);
			r.close();
			s.close();
			DBConnection.closeConnection();
		} catch(Exception e) {
			e.printStackTrace();
		}
		long endTime = System.currentTimeMillis();
		System.out.println("db query completes in "+1.0 * (endTime - startTime) / (1000)+" sec");
	}
	
	
	static void getUserEventsViaFile(String user, List<File> files) {
		long startTime = System.currentTimeMillis();
		for(File f: files) {
			
			ZipFile zf = null;
			BufferedReader br = null;
			try {
				zf = new ZipFile(f);
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
						if(username.equals(user)) {
							//System.out.println(user+","+celllac+","+timestamp);
						}
						
					} catch(Exception e) {
						if(!badread) {
							System.err.println("bad read in file "+f);
							badread = true;
						}
					}
				}
			}catch(Exception e) {
				System.err.println("Problems with file: "+f.getAbsolutePath());
			}
			try {
				br.close();
				zf.close();
			}catch(Exception e) {
				e.printStackTrace();
			}	
		}	
		long endTime = System.currentTimeMillis();
		System.out.println("file query completes in "+1.0 * (endTime - startTime) / (1000)+" sec");
	}
	
	static void getCellEventsViaFile(String cell, List<File> files) {
		long startTime = System.currentTimeMillis();
		int cont = 0;
		for(File f: files) {
			
			ZipFile zf = null;
			BufferedReader br = null;
			try {
				zf = new ZipFile(f);
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
						if(celllac.equals(cell)) {
							cont++;
						}
						
					} catch(Exception e) {
						if(!badread) {
							System.err.println("bad read in file "+f);
							badread = true;
						}
					}
				}
			}catch(Exception e) {
				System.err.println("Problems with file: "+f.getAbsolutePath());
			}
			try {
				br.close();
				zf.close();
			}catch(Exception e) {
				e.printStackTrace();
			}	
		}	
		System.out.println("N. Events ="+cont);
		long endTime = System.currentTimeMillis();
		System.out.println("file query completes in "+1.0 * (endTime - startTime) / (1000)+" sec");
	}
	
	
	
}
