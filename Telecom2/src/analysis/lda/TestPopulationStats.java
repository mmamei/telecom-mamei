package analysis.lda;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import utils.FileUtils;
import utils.Logger;

public class TestPopulationStats {
	public static void main(String[] args) throws Exception {
		
		
		int[] h_num_pls = new int[50];
		int[] h_num_days = new int[50];
		
		
		BufferedReader br = new BufferedReader(new FileReader(FileUtils.getFile("BASE/UserEventCounter/torino_cellXHour.csv")));		
		String line;
		while((line=br.readLine())!=null) {
			if(line.startsWith("//")) {Logger.logln(line); continue;}
			
			String[] p = line.split(",");
			
			int num_pls = Integer.parseInt(p[2]);
			int num_days = Integer.parseInt(p[3]);
			
			int f = num_pls / num_days;
			if(f >= h_num_pls.length) f = h_num_pls.length-1;
			
			h_num_pls[f]++;
			h_num_days[num_days]++;
		}
		
		File dir = FileUtils.createDir("BASE/Topic");
		PrintWriter pw = new PrintWriter(new FileWriter(dir+"/stat.csv"));
		pw.println("num pls per day");
		for(int i=0; i<h_num_pls.length;i++)
			pw.println(i+","+h_num_pls[i]);
		pw.println("num days");
		for(int i=0; i<h_num_days.length;i++)
			pw.println(i+","+h_num_days[i]);
		
		pw.close();
		System.out.println("Done!");
		
	}
}
