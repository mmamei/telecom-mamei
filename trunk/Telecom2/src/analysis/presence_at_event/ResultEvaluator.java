package analysis.presence_at_event;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import utils.FileUtils;
import utils.Logger;
import visual.java.GraphScatterPlotter;

public class ResultEvaluator {
	
	
	public static boolean PLOT = false;
	
	public static boolean USE_INDIVIDUAL_EVENT = true;
	public static boolean DIFF = false;

	public static boolean LEAVE_ONE_OUT = true;
	public static boolean PIECEWISE = true;
	public static int PIECE_SIZE = 6;
	
	public static boolean RANGE = false;
	public static int RANGE_TH = 10000;
	
	
	public static boolean INTERCEPT = true;
	public static final boolean VERBOSE = false;
	
	
	private static String type = USE_INDIVIDUAL_EVENT ? "individual" : "multiple";
	private static String sdiff = DIFF ? "_diff" : "";
	
	private static File lomb = FileUtils.getFile("BASE/PresenceCounter/C_DATASET_PLS_file_pls_file_pls_lomb/result_"+type+"_0.0_3"+sdiff+".csv");
	private static File piem2012 = FileUtils.getFile("BASE/PresenceCounter/C_DATASET_PLS_file_pls_file_pls_piem_piem_2012/result_"+type+"_0.0_3"+sdiff+".csv");
	private static File piem2013 = FileUtils.getFile("BASE/PresenceCounter/C_DATASET_PLS_file_pls_file_pls_piem_piem_2013/result_"+type+"_0.0_3"+sdiff+".csv");
	//private static String piem2013_openair = FileUtils.getFileS("PresenceCounter/C_DATASET_PLS_file_pls_file_pls_piem_2013/result_openair_"+type+"_0.0_3"+sdiff+".csv");

	
	
	public static void main(String[] args) throws Exception {
		
		File[] training = new File[]{lomb,piem2012,piem2013};
		File[] testing = new File[]{lomb,piem2012,piem2013};
		
		run(training,testing);
		
		Logger.logln("Done");
	}
	
	public static int run(File file) {
		try {
			Map<String,List<double[]>>  scaled = run(new File[]{lomb,piem2012,piem2013},new File[]{file});
			return (int)scaled.values().iterator().next().get(0)[0];
		}catch(Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	
	
	public static Map<String,List<double[]>> run(File[] training_files, File[] testing_files) throws Exception {
		Map<String,List<double[]>> training_map = read(training_files);
		Map<String,List<double[]>> testing_map = read(testing_files);
		Map<String,List<double[]>> scaled = new HashMap<String,List<double[]>>();
		if(RANGE) {
			Map<String,List<double[]>>[] tra = divide(training_map,RANGE_TH);
			Map<String,List<double[]>>[] tst = divide(testing_map,RANGE_TH);
			
			Logger.logln("RESULTS FOR EVENTS BELOW "+RANGE_TH);
			INTERCEPT = false;
			scaled.putAll(run(tra[0],tst[0]));
			
			Logger.logln("\nRESULTS FOR EVENTS ABOVE "+RANGE_TH);
			INTERCEPT = true;
			scaled.putAll(run(tra[1],tst[1]));
		}
		else scaled.putAll(run(training_map,testing_map));
		return scaled;
	}

		
     // scale testing data according to training regression
		
	public static Map<String,List<double[]>> run(Map<String,List<double[]>> training_map, Map<String,List<double[]>> testing_map) throws Exception {
		
		Map<String,List<double[]>> scaled = PIECEWISE ? scalePiecewise(testing_map,training_map) : scale(testing_map,training_map);
		
		if(PLOT) {
			draw("Testing",testing_map);
			draw("Result after scaling",scaled);
		
			// compute error
			
			DescriptiveStatistics[] abs_perc_errors = computeErrorStats(scaled);
			DescriptiveStatistics abs_err_stat = abs_perc_errors[0];
			DescriptiveStatistics perc_err_stat = abs_perc_errors[1];
			
			
			Logger.logln("MEAN ABS ERROR = "+(int)abs_err_stat.getMean());
			Logger.logln("MEDIAN ABS ERROR = "+(int)abs_err_stat.getPercentile(50));
			Logger.logln("SKEWNESS ABS ERROR ="+abs_err_stat.getSkewness());
			Logger.logln("MEAN % ERROR = "+(int)perc_err_stat.getMean()+"%");
			Logger.logln("MEDIAN % ERROR = "+(int)perc_err_stat.getPercentile(50)+"%");
			Logger.logln("SKEWNESS % ERROR ="+perc_err_stat.getSkewness());
		}
		return scaled;
	}
	
	
	
	
	
	public static Map<String,List<double[]>>[] divide(Map<String,List<double[]>> x, double threshold) {
		Map<String,List<double[]>>[] ba = (Map<String,List<double[]>>[])new Map[2]; // ba = bottom - above
		ba[0] = new HashMap<String,List<double[]>>();
		ba[1] = new HashMap<String,List<double[]>>();
		for(String k: x.keySet()) {
			List<double[]> l = x.get(k);
			for(double[] v: l) {
				if(v[1] <= threshold) add(ba[0],k,v);
				else add(ba[1],k,v);
			}
		}
		return ba;
		
	}
	
	private static void add(Map<String,List<double[]>> map, String k, double[] v) {
		List<double[]> l = map.get(k);
		if(l == null) {
			l = new ArrayList<double[]>();
			map.put(k, l);
		}
		l.add(v);
	}
	

	
	
	public static Map<String,List<double[]>> scale(Map<String,List<double[]>> testing_map, Map<String,List<double[]>> training_map) {
		SimpleRegression training_sr = getRegression(training_map);
		
		printInfo("INFO: ",training_sr);
		
		Map<String,List<double[]>> scaled = new HashMap<String,List<double[]>>();
		for(String placemark: testing_map.keySet()) {
			//Logger.logln(placemark);
			List<double[]> list_est = new ArrayList<double[]>(); 
			for(double[] x : testing_map.get(placemark)) {
				double est = constrain(training_sr.predict(x[0]));
				double gt = x[1];
				list_est.add(new double[]{est,gt});
			}
			scaled.put(placemark, list_est);
		}
		return scaled;
	}
	
	
	public static Map<String,List<double[]>> scalePiecewise(Map<String,List<double[]>> testing_map, Map<String,List<double[]>> training_map) {
		Map<String,List<double[]>> scaled = new HashMap<String,List<double[]>>();		
		for(String placemark: testing_map.keySet()) {
			//Logger.logln(placemark);
			List<double[]> list_est = new ArrayList<double[]>(); 
			for(double[] x : testing_map.get(placemark)) {
				SimpleRegression sr = getPiecewiseLR(training_map,x[0]);
				double est = constrain(sr.predict(x[0]));
				double gt = x[1];
				list_est.add(new double[]{est,gt});
			}
			scaled.put(placemark, list_est);
		}
		return scaled;
	}
	
	public static double constrain(double est) {
		if(est < 0) return 0;
		if(est > 80000) return 80000;
		return est;
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
				
				if(VERBOSE) Logger.logln(p+" GT = "+(int)gt+" EST = "+(int)est+" ABS_ERR = "+(int)abserr+" %ERR = "+(int)perc+"%");	
			}
		}
		return new DescriptiveStatistics[]{abs_err_stat,perc_err_stat};
	}
	
	static final DecimalFormat F = new DecimalFormat("##.##",new DecimalFormatSymbols(Locale.US));
	public static void printInfo(String title, SimpleRegression sr) {
		double s = sr.getSlope();
		double sconf = sr.getSlopeConfidenceInterval(); 
		
		double i = sr.getIntercept();
		double iconf = sr.getInterceptStdErr();
		
		if(INTERCEPT) Logger.logln(title+": Y = "+F.format(s)+" * X + "+(int)(i));
		else  Logger.logln(title+": Y = "+F.format(s)+" * X");
		Logger.logln(title+": SLOPE CONF INTERVAL =  ["+F.format(s-sconf)+","+F.format(s+sconf)+"]");
		if(INTERCEPT) Logger.logln(title+": INTERCEPT CONNF INTERVAL =  ["+F.format(i-iconf)+","+F.format(i+iconf)+"]");
		Logger.logln(title+": r="+F.format(sr.getR()));//+", r^2="+Fsr.getRSquare()+", sse="+sr.getSumSquaredErrors());
	}
	
	public static Map<String,List<double[]>> read(File[] files) throws Exception {
		Map<String,List<double[]>> map = new HashMap<String,List<double[]>>();
		
		for(File file: files) {
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
	
	
	
	public static SimpleRegression getPiecewiseLR(Map<String,List<double[]>> training, final double x) {
		
		// create a single list
		List<double[]> all = new ArrayList<double[]>();
		for(List<double[]> l: training.values())
			all.addAll(l);
		
		Collections.sort(all, new Comparator<double[]>(){
			public int compare(double[] a, double[] b) {
				if(Math.abs(a[0]-x) > Math.abs(b[0]-x)) return 1;
				if(Math.abs(a[0]-x) < Math.abs(b[0]-x)) return -1;
				return 0;
			}
		});
		
		SimpleRegression r = new SimpleRegression(INTERCEPT);
		
		//int start = LEAVE_ONE_OUT ? 1 : 0;
		
		for(int i=0; i<all.size() && i<PIECE_SIZE; i++) {
			
			if(LEAVE_ONE_OUT && i==0 && Math.abs(x-all.get(i)[0]) < 0.0000001) {
				PIECE_SIZE ++;
				//System.out.println("-------------------------------------------------------------leave one out effective!");
				continue;
			}
			r.addData(all.get(i)[0], all.get(i)[1]);
		}

		return r;
	}
	
	
	
	public static void draw(String title, Map<String,List<double[]>> map) {
		
		if(VERBOSE) {
			Logger.logln(title+" ***************************************");
	
			for(String placemark: map.keySet()) {
				for(double[] x : map.get(placemark)) {
					double est = x[0];
					double gt = x[1];
					Logger.logln(placemark+";"+(int)est+";"+(int)gt);
				}
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


