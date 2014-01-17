package analysis.tourist;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import pls_parser.UserEventCounterCellacXHour;
import utils.CopyAndSerializationUtils;
import utils.FileUtils;
import utils.Logger;
import area.region.RegionMap;
import classify.svm_predict;
import classify.svm_scale;
import classify.svm_train;

public class TouristClassify {
	
	public static final String[] MODE = new String[]{"svm","weka"};
	public static final String[] EXTENSION = new String[]{".txt",".arff"};
	
	
	public static final int USE = TouristData.WEKA;
	
	static String city = "Venezia";
	static int test_n = 10000;
	static int train_n = 100; 
	
	public static final int IT = 0;
	public static final int ROAMING = 1;
	public static final int ALL = 2;
	public static final int TYPE = ALL;
	
	public static void main(String[] args) throws Exception {
		
		int skip = createTestingSet(city, 0, test_n,TYPE);
		createTrainingSetFromMultiRegionData(city);
		//createTrainingSet(city, skip, train_n);
		
		if(USE == TouristData.WEKA) return;
		
		String d = FileUtils.getFileS("TouristData");
		String testf = d+"/svm_test_"+city+"_"+test_n+".txt";
		String trainf = d+"/svm_train_"+city+"_"+train_n+".txt";
		
		svm_scale.main(new String[]{"-l","0","-u","1","-s",d+"/scaling.parms",trainf,d+"/train.scaled"});
		double[] bestp = new double[]{0.125,0.03125}; //Test.gridSerach(d+"/train.scaled");
		svm_train.main(new String[]{"-c",""+bestp[0],"-g",""+bestp[1],d+"/train.scaled",d+"/train.scaled.model"});
		
		svm_scale.main(new String[]{"-l","0","-u","1","-r",d+"/scaling.parms",testf,d+"/test.scaled"});
		svm_predict.main(new String[]{d+"/test.scaled",d+"/train.scaled.model",d+"/predict.txt"});
		
		Logger.logln("Done");
	}
	
	
	
	
	
	public static int createTrainingSet(String city, int skip, int max_num_per_class) throws Exception {
		int how_many_read = 0;
		int[] how_many_samples_per_class = new int[2];
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(FileUtils.getFile("RegionMap/"+city+".ser"));
		BufferedReader br = FileUtils.getBR("UserEventCounter/"+city+"_cellacXhour.csv");
		if(br == null) {
			Logger.logln("Launch UserEventCounterCellacXHour first!");
			System.exit(0);
		}
		
		File f = new File(FileUtils.create("TouristData")+"/"+MODE[USE]+"_train_"+city+"_"+skip+"_"+max_num_per_class+EXTENSION[USE]);
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
		
		int i=0;
		String line;
		TouristData td = null;
		
		for(int k=0; k<skip;k++)
			br.readLine();
		
		how_many_read += skip;
		
		
		boolean header = false;
		
		while((line=br.readLine())!=null) {
			how_many_read++;
			td = new TouristData(line,rm);
			
			if(USE==TouristData.WEKA && !header) {out.println(td.wekaHeader("test_"+city+"_"+skip)); header = true;}
			
			boolean oktraining1 = td.roaming() && td.num_days < 4 && td.num_pls > 1;
			boolean oktraining2 = !td.roaming() && td.num_days > 14;
			
			if(how_many_samples_per_class[1] < max_num_per_class && oktraining1) {
				out.println(td.toString(USE,1));
				how_many_samples_per_class[1]++;
			}
			else if(how_many_samples_per_class[0] < max_num_per_class && oktraining2){
				out.println(td.toString(USE,0));
				how_many_samples_per_class[0]++;
			}
			
			// check finish
			boolean exit = true;
			for(int x: how_many_samples_per_class)
				if(x < max_num_per_class) exit = false;
			if(exit) break;
			
			if(++i % 10000 == 0) {
				Logger.logln("Processed "+i+" users...");
			}
		}
		br.close();
		out.close();
		return how_many_read;
	}
	
	
	public static void createTrainingSetFromMultiRegionData(String city) throws Exception {
		
		Set<String> tourists = new HashSet<String>();
		Set<String> residents = new HashSet<String>();
		
		BufferedReader br = FileUtils.getBR("UserEventCounter/"+city+"_Lombardia_Piemonte.csv");
		String line;
		while((line=br.readLine())!=null){
			if(line.contains("TOURIST")) tourists.add(line.substring(0,line.indexOf(",")));
			if(line.contains("RESIDENT")) residents.add(line.substring(0,line.indexOf(",")));
		}
		br.close();
		
		
		System.out.println("found "+tourists.size()+" tourists and "+residents.size()+" residents");
		
		File f = new File(FileUtils.create("TouristData")+"/"+MODE[USE]+"_train_MR_"+city+EXTENSION[USE]);
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
		
		boolean header = false;
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(FileUtils.getFile("RegionMap/"+city+".ser"));
		br = FileUtils.getBR("UserEventCounter/"+city+"_cellacXhour.csv");
		while((line=br.readLine())!=null){
			String un = line.substring(0,line.indexOf(","));
			int clazz = 2;
			if(tourists.contains(un)) clazz = 1;
			if(residents.contains(un)) clazz = 0;
			if(clazz != 0 && clazz != 1) continue;
			TouristData td = new TouristData(line,rm);
			if(USE==TouristData.WEKA && !header) {out.println(td.wekaHeader("train_MR_"+city)); header = true;}
			out.println(td.toString(USE,clazz));
		}
		br.close();
		out.close();
	}
	
	
	public static int createTestingSet(String city, int skip, int num, int type) throws Exception {
		int how_many_read = 0;
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(FileUtils.getFile("RegionMap/"+city+".ser"));
		BufferedReader br = FileUtils.getBR("UserEventCounter/"+city+"_cellacXhour.csv");
		if(br == null) {
			Logger.logln("Launch UserEventCounterCellacXHour first!");
			System.exit(0);
		}
		File f = new File(FileUtils.create("TouristData")+"/"+MODE[USE]+"_test_"+city+"_"+skip+"_"+num+EXTENSION[USE]);
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
		
		String line;
		TouristData td = null;
		
		for(int k=0; k<skip;k++)
			br.readLine();
		
		how_many_read += skip;
		
		boolean header = false;
		
		int k=0;
		while(k<num) {
			line=br.readLine();
			how_many_read ++;
			td = new TouristData(line,rm);
			if(USE==TouristData.WEKA && !header) {out.println(td.wekaHeader("test_"+city+"_"+skip)); header = true;}
			
			boolean oktype = (TYPE==IT && !td.roaming()) || (TYPE==ROAMING && td.roaming()) || TYPE==ALL;
			boolean oknum = (1.0 * td.num_pls / td.num_days) > 1;
			
			if(oktype && oknum) {
				int supposed_class = td.num_days < 4 ? 1 : 0;
				out.println(td.toString(USE,supposed_class));
				k++;
			}
		}
		br.close();
		out.close();
		return how_many_read;
	}
	
	
}
