package analysis.presence_at_event;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import utils.Config;
import utils.Logger;
import visual.java.GraphScatterPlotter;

public class ResultEvaluator {
	
	
	public static final boolean INTERCEPT = false;
	
	public static void main(String[] args) throws Exception {
		String[] files = new String[]{
				Config.getInstance().base_dir +"/PresenceCounter/C_DATASET_PLS_file_pls_file_pls_lomb/result_0.0_3.csv",
				Config.getInstance().base_dir +"/PresenceCounter/C_DATASET_PLS_file_pls_file_pls_piem_2012/result_0.0_3.csv",
		};
		
		
		String[] training = new String[]{Config.getInstance().base_dir +"/PresenceCounter/C_DATASET_PLS_file_pls_file_pls_piem_2012/result_individual_0.0_3.csv"};
		String[] testing = new String[]{Config.getInstance().base_dir +"/PresenceCounter/C_DATASET_PLS_file_pls_file_pls_piem_2013/result_individual_0.0_3.csv"};
		
		
		run(training,testing);
		//runSeparate(files,files);
		Logger.logln("Done");
	}
	
	
	
	
	public static void run(String[] training_files, String[] testing_files) throws Exception {
		Map<String,List<double[]>> training_map = read(training_files);
		Map<String,List<double[]>> testing_map = read(testing_files);

		SimpleRegression training_sr = getRegression(training_map);
		SimpleRegression testing_sr = getRegression(testing_map);
		
		printInfo("TRAINING",training_sr);
		printInfo("TESTING",testing_sr);
		
		// scale testing data according to training regression
		Map<String,List<double[]>> scaled = scale(testing_map,training_sr);
		draw("Result",scaled);
		
		// compute error
		DescriptiveStatistics[] abs_perc_errors = computeErrorStats(scaled);
		DescriptiveStatistics abs_err_stat = abs_perc_errors[0];
		DescriptiveStatistics perc_err_stat = abs_perc_errors[1];
		
		Logger.logln("MEAN ABS ERROR = "+(int)abs_err_stat.getMean());
		Logger.logln("MEDIAN ABS ERROR = "+(int)abs_err_stat.getPercentile(50));
		
		Logger.logln("MEAN % ERROR = "+(int)perc_err_stat.getMean()+"%");
		Logger.logln("MEDIAN % ERROR = "+(int)perc_err_stat.getPercentile(50)+"%");
	}
	
	
	
	public static void runSeparate(String[] training_files, String[] testing_files) throws Exception {
		Map<String,List<double[]>> training_map = read(training_files);
		Map<String,List<double[]>> testing_map = read(testing_files);
		
		Map<String,Map<String,List<double[]>>> sep_training_map = separate(training_map);
		Map<String,Map<String,List<double[]>>> sep_testing_map = separate(testing_map);
		
		// overall error statistics
		
		DescriptiveStatistics overall_abs_err_stat = new DescriptiveStatistics();
		DescriptiveStatistics overall_perc_err_stat = new DescriptiveStatistics();
		
		for(String p: sep_training_map.keySet()) {
			Map<String,List<double[]>> individual_training_map = sep_training_map.get(p);
			Map<String,List<double[]>> individual_testing_map = sep_testing_map.get(p);
			
			SimpleRegression training_sr = getRegression(individual_training_map);
			SimpleRegression testing_sr = getRegression(individual_testing_map);
			
			printInfo("TRAINING",training_sr);
			printInfo("TESTING",testing_sr);
			
			// scale testing data according to training regression
			Map<String,List<double[]>> scaled = scale(individual_testing_map,training_sr);
			draw(p,individual_testing_map);
			
			// compute error
			DescriptiveStatistics[] abs_perc_errors = computeErrorStats(scaled);
			DescriptiveStatistics abs_err_stat = abs_perc_errors[0];
			DescriptiveStatistics perc_err_stat = abs_perc_errors[1];
					
			Logger.logln(p+" MEAN ABS ERROR = "+(int)abs_err_stat.getMean());
			Logger.logln(p+" MEDIAN ABS ERROR = "+(int)abs_err_stat.getPercentile(50));
			
			Logger.logln(p+" MEAN % ERROR = "+(int)perc_err_stat.getMean()+"%");
			Logger.logln(p+" MEDIAN % ERROR = "+(int)perc_err_stat.getPercentile(50)+"%");
			
			// add error values to the overall error statis
			for(double v: abs_err_stat.getSortedValues()) overall_abs_err_stat.addValue(v);
			for(double v: perc_err_stat.getSortedValues()) overall_perc_err_stat.addValue(v);
		}
		
		Logger.logln("OVERALL MEAN ABS ERROR = "+(int)overall_abs_err_stat.getMean());
		Logger.logln("OVERALL MEDIAN ABS ERROR = "+(int)overall_abs_err_stat.getPercentile(50));
		
		Logger.logln("OVERALL MEAN % ERROR = "+(int)overall_perc_err_stat.getMean()+"%");
		Logger.logln("OVERALL MEDIAN % ERROR = "+(int)overall_perc_err_stat.getPercentile(50)+"%");
	}
	
	
	public static Map<String,Map<String,List<double[]>>> separate(Map<String,List<double[]>> map) {
		Map<String,Map<String,List<double[]>>> sep = new HashMap<String,Map<String,List<double[]>>>();
		for(String p: map.keySet()) {
			Map<String,List<double[]>> s = new HashMap<String,List<double[]>>();
			s.put(p, map.get(p));
			sep.put(p, s);
		}
		return sep;
	}
	
	public static Map<String,List<double[]>> scale(Map<String,List<double[]>> testing_map, SimpleRegression training_sr) {
		Map<String,List<double[]>> scaled = new HashMap<String,List<double[]>>();
		for(String placemark: testing_map.keySet()) {
			Logger.logln(placemark);
			List<double[]> list_est = new ArrayList<double[]>(); 
			for(double[] x : testing_map.get(placemark)) {
				double est = Math.max(0, training_sr.predict(x[0]));
				double gt = x[1];
				list_est.add(new double[]{est,gt});
			}
			scaled.put(placemark, list_est);
		}
		return scaled;
	}
	
	
	public static DescriptiveStatistics[] computeErrorStats(Map<String,List<double[]>> scaled) {
		DescriptiveStatistics abs_err_stat = new DescriptiveStatistics();
		DescriptiveStatistics perc_err_stat = new DescriptiveStatistics();
		for(String p: scaled.keySet()) {
			List<double[]> lv = scaled.get(p);
			for(double[] v: lv) {
				double est = v[0];
				double gt = v[1];
				double abserr = Math.abs(est - gt);
				abs_err_stat.addValue(abserr);
					
				double perc = 100*(abserr/gt);
				perc_err_stat.addValue(perc);
					
				Logger.logln(p+" GT = "+(int)gt+" EST = "+(int)est+" ABS_ERR = "+(int)abserr+" %ERR = "+(int)perc+"%");	
			}
		}
		return new DescriptiveStatistics[]{abs_err_stat,perc_err_stat};
	}
	
	public static void printInfo(String title, SimpleRegression sr) throws Exception {
		Logger.logln(title+": r="+sr.getR()+", r^2="+sr.getRSquare()+", sse="+sr.getSumSquaredErrors());
		
		double s = sr.getSlope();
		double sconf = sr.getSlopeConfidenceInterval(); 
		
		double i = sr.getIntercept();
		double iconf = sr.getInterceptStdErr();
		
		Logger.logln(title+"Y = "+s+" * X + "+i);
		Logger.logln(title+": SLOPE CONF INTERVAL =  ["+(s-sconf)+","+(s+sconf)+"]");
		Logger.logln(title+": INTERCEPT CONNF INTERVAL =  ["+(i-iconf)+","+(i+iconf)+"]");
		
	}
	
	public static Map<String,List<double[]>> read(String[] files) throws Exception {
		Map<String,List<double[]>> map = new HashMap<String,List<double[]>>();
		
		for(String file: files) {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			br.readLine(); // skip header
			while((line = br.readLine()) != null) {
				String[] e = line.split(",");
				String placemark = e[0].substring(0,e[0].indexOf("-"));
				double estimated = Double.parseDouble(e[1]);
				double groundtruth = Double.parseDouble(e[2]);
				List<double[]> p = map.get(placemark);
				if(p==null) {
					p = new ArrayList<double[]>();
					map.put(placemark, p);
				}
				p.add(new double[]{estimated, groundtruth});
			}
			br.close();
		}
		return map;
	}
	
	
	public static SimpleRegression getRegression(Map<String,List<double[]>> map) {
		SimpleRegression sr = new SimpleRegression(INTERCEPT);
		for(List<double[]> lv: map.values())
		for(double[] v: lv)
			sr.addData(v[0], v[1]);
		return sr;
	}	
	
	
	
	
	
	public static void draw(String title, Map<String,List<double[]>> map) {
		

		for(String placemark: map.keySet()) {
			for(double[] x : map.get(placemark)) {
				double est = x[0];
				double gt = x[1];
				Logger.logln(placemark+";"+(int)est+";"+(int)gt);
			}
		}
		
		
		List<String> labels = new ArrayList<String>();
		List<double[][]> data = new ArrayList<double[][]>();
		
		for(String placemark: map.keySet()) {
			labels.add(placemark);
			List<double[]> p = map.get(placemark);
			double[][] x = new double[p.size()][2];
			for(int i=0; i<p.size();i++)
				x[i] = p.get(i);
			data.add(x);
		}
		
		new GraphScatterPlotter(title,"Estimated","GroundTruth",data,labels);
	}
}


