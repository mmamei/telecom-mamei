package analysis.tourist;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

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
	public static final int SVM = 0;
	public static final int WEKA = 1;
	
	public static final int USE = WEKA;
	
	static String city = "Venezia";
	static int[] test_bounds = new int[]{0,100};
	static int[] train_bounds = new int[]{100,200}; // it is always better to have testing indices first!
	
	public static void main(String[] args) throws Exception {
		
		createTestingSet(city, test_bounds[0], test_bounds[1]);
		createTrainingSet(city, train_bounds[0], train_bounds[1]);
		
		
		if(USE == WEKA) return;
		
		String d = FileUtils.getFileS("TouristData");
		String testf = d+"/svm_test_"+city+"_"+test_bounds[0]+"_"+test_bounds[1]+".txt";
		String trainf = d+"/svm_train_"+city+"_"+train_bounds[0]+"_"+train_bounds[1]+".txt";
		
		svm_scale.main(new String[]{"-l","0","-u","1","-s",d+"/scaling.parms",trainf,d+"/train.scaled"});
		double[] bestp = new double[]{0.125,0.03125}; //Test.gridSerach(d+"/train.scaled");
		svm_train.main(new String[]{"-c",""+bestp[0],"-g",""+bestp[1],d+"/train.scaled",d+"/train.scaled.model"});
		
		svm_scale.main(new String[]{"-l","0","-u","1","-r",d+"/scaling.parms",testf,d+"/test.scaled"});
		svm_predict.main(new String[]{d+"/test.scaled",d+"/train.scaled.model",d+"/predict.txt"});
		
		Logger.logln("Done");
	}
	
	
	public static void createTrainingSet(String city, int skip, int max_num_per_class) throws Exception {
		int[] how_many_samples_per_class = new int[2];
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(FileUtils.getFile("RegionMap/"+city+".ser"));
		BufferedReader br = FileUtils.getBR("UserEventCounter/"+city+"_cellacXhour.csv");
		if(br == null) {
			UserEventCounterCellacXHour.process(city);
			br = FileUtils.getBR("UserEventCounter/"+city+"_cellacXhour.csv");
		}
		
		File f = new File(FileUtils.create("TouristData")+"/"+MODE[USE]+"_train_"+city+"_"+skip+"_"+max_num_per_class+EXTENSION[USE]);
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
		
		int i=0;
		String line;
		TouristData td = null;
		
		for(int k=0; k<skip;k++)
			br.readLine();
		
		boolean header = false;
		
		while((line=br.readLine())!=null) {
			td = new TouristData(line,rm);
			
			if(USE==WEKA && !header) {out.println(td.wekaHeader("test_"+city+"_"+skip)); header = true;}
			
			boolean oktraining1 = td.roaming() && td.num_days < 4 && td.num_pls > 1;
			boolean oktraining2 = !td.roaming() && td.num_days > 14;
			
			if(how_many_samples_per_class[1] < max_num_per_class && oktraining1) {
				if(USE == WEKA)	out.println(td.toWEKAString(1));
				if(USE == SVM)	out.println(td.toSVMString(1));
				how_many_samples_per_class[1]++;
			}
			else if(how_many_samples_per_class[0] < max_num_per_class && oktraining2){
				if(USE == WEKA)	out.println(td.toWEKAString(0));
				if(USE == SVM)	out.println(td.toSVMString(0));
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
		
	}
	
	public static void createTestingSet(String city, int skip, int num) throws Exception {
			
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(FileUtils.getFile("RegionMap/"+city+".ser"));
		BufferedReader br = FileUtils.getBR("UserEventCounter/"+city+"_cellacXhour.csv");
		if(br == null) {
			UserEventCounterCellacXHour.process(city);
			br = FileUtils.getBR("UserEventCounter/"+city+"_cellacXhour.csv");
		}
		
		File f = new File(FileUtils.create("TouristData")+"/"+MODE[USE]+"_test_"+city+"_"+skip+"_"+num+EXTENSION[USE]);
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
		
		String line;
		TouristData td = null;
		
		for(int k=0; k<skip;k++)
			br.readLine();
		
		boolean header = false;
		for(int k=0; k<num;k++) {
			line=br.readLine();
			td = new TouristData(line,rm);
			
			if(USE==WEKA && !header) {out.println(td.wekaHeader("test_"+city+"_"+skip)); header = true;}
			
			
			int supposed_class = td.num_days < 4 ? 1 : 0;
			if(USE == WEKA)	out.println(td.toWEKAString(supposed_class));
			if(USE == SVM)	out.println(td.toSVMString(supposed_class));
		}
		br.close();
		out.close();
	}
	
	
}
