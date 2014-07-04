package region;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import utils.Config;

public class ParserDatiISTAT {
	public static void main(String[] args) throws Exception {
		String region = "Lombardia";
		String dir = "G:/DATASET/CENSUS/ISTAT/DatiDemografici/"+region;
		Map<String,Integer> data = parse(dir);
		
		File out_dir = new File(Config.getInstance().base_folder+"/CENSUS");
		out_dir.mkdirs();
		PrintWriter out = new PrintWriter(new FileWriter(out_dir+"/"+region+".csv"));
		
		for(String comune: data.keySet()) 
			out.println(comune.toUpperCase()+","+data.get(comune));
		out.close();
	}
	
	
	public static Map<String,Integer> load(File f) throws Exception {

		BufferedReader br = new BufferedReader(new FileReader(f));
		
		Map<String,Integer> istat = new HashMap<String,Integer>();
		
		String line;
		while((line=br.readLine())!=null) {
			String[] e = line.split(",");
			istat.put(e[0].toLowerCase(), Integer.parseInt(e[1]));
		}
		br.close();
		return istat;
	}
	
	public static Map<String,Integer> parse(String d) throws Exception {
		
		Map<String,Integer> data = new HashMap<String,Integer>();
		File dir = new File(d);
		for(File f: dir.listFiles()) 
			data.putAll(parseFile(f));
		return data;
	}
	
	
	public static Map<String,Integer> parseFile(File f) throws Exception {
		Map<String,Integer> data = new HashMap<String,Integer>();
		
		ZipFile zf = new ZipFile(f);
		ZipEntry ze = (ZipEntry) zf.entries().nextElement();
		BufferedReader br = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)));
		
		Map<String,Integer> cod_pop = new HashMap<String,Integer>();
		Map<String,String> cod_name = new HashMap<String,String>();
		
		//skip the first 3 lines that are title
		br.readLine();
		br.readLine();
		br.readLine();
		
		boolean cod_name_section = false;
		String line;
		while((line = br.readLine()) != null) {
			line = line.trim();
			if(line.length() < 1) continue; // empty line
			
			if(cod_name_section) {
				String[] elements = line.split(",");
				cod_name.put(elements[0], elements[1]);
			}
			
			if(line.equals("Tavola descrizione codici comuni")){
				br.readLine(); // skip Codice Comune,Nome Comune
				cod_name_section = true;
			}
			
			if(!cod_name_section) {
				String[] elements = line.split(",");
				String cod = elements[0];
				int eta = Integer.parseInt(elements[1]);
				if(eta == 999) {
					int num_m = Integer.parseInt(elements[6]);
					int num_f = Integer.parseInt(elements[11]);
					cod_pop.put(cod, num_m+num_f);
				}
			}
		}

		br.close();
		zf.close();
		
		for(String cod: cod_name.keySet()) 
			data.put(cod_name.get(cod).toLowerCase(),cod_pop.get(cod));
		return data;
	}
	
}
