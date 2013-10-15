package analysis.presence_at_event;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math.stat.regression.SimpleRegression;

import utils.Config;
import utils.Logger;
import visual.java.GraphScatterPlotter;

public class ResultEvaluator {
	
	
	public static void main(String[] args) throws Exception {
		String[] files = new String[]{
				Config.getInstance().base_dir +"/PresenceCounter/C_DATASET_PLS_file_pls_file_pls_lomb/result_0.0_3.csv",
				Config.getInstance().base_dir +"/PresenceCounter/C_DATASET_PLS_file_pls_file_pls_piem_2012/result_0.0_3.csv",
		};
		run(files,files);
		Logger.logln("Done");
	}
	
	
	public static void run(String[] training_files, String[] testing_files) throws Exception {
		
		Envelope training_data = read(training_files);
		SimpleRegression training_sr = training_data.sr;
		//Map<String,List<double[]>> training_map = training_data.map;
		
		Envelope testing_data = read(testing_files);
		SimpleRegression testing_sr = testing_data.sr;
		Map<String,List<double[]>> testing_map = testing_data.map;
		
		Logger.logln("TRAINING: r="+training_sr.getR()+", r^2="+training_sr.getRSquare()+", sse="+training_sr.getSumSquaredErrors());
		Logger.logln("TESTING: r="+testing_sr.getR()+", r^2="+testing_sr.getRSquare()+", sse="+testing_sr.getSumSquaredErrors());
		
		DescriptiveStatistics abs_err_stat = new DescriptiveStatistics();
		DescriptiveStatistics perc_err_stat = new DescriptiveStatistics();
		
		double s = training_sr.getSlope();
		double sconf = training_sr.getSlopeConfidenceInterval(); 
		
		double i = training_sr.getIntercept();
		double iconf = training_sr.getInterceptStdErr();
		
		Logger.logln("Y = "+s+" * X + "+i);
		
		Logger.logln("TRAINING: SLOPE CONF INTERVAL =  ["+(s-sconf)+","+(s+sconf)+"]");
		Logger.logln("TRAINING: INTERCEPT CONNF INTERVAL =  ["+(i-iconf)+","+(i+iconf)+"]");
		
		for(String placemark: testing_map.keySet()) {
			Logger.logln(placemark);
			List<double[]> list_est = new ArrayList<double[]>(); 
			for(double[] x : testing_map.get(placemark)) {
				double est = Math.max(0, training_sr.predict(x[0]));
				double gt = x[1];
				list_est.add(new double[]{est,gt});
				
				double abserr = Math.abs(est - gt);
				abs_err_stat.addValue(abserr);
				
				double perc = 100*(abserr/gt);
				perc_err_stat.addValue(perc);
				
				Logger.logln("GT = "+(int)gt+" EST = "+(int)est+" ABS_ERR = "+(int)abserr+" %ERR = "+(int)perc+"%");
				
			}
			testing_map.put(placemark, list_est);
		}
		
		Logger.logln("MEAN ABS ERROR = "+(int)abs_err_stat.getMean());
		Logger.logln("MEDIAN ABS ERROR = "+(int)abs_err_stat.getPercentile(50));
		
		Logger.logln("MEAN % ERROR = "+(int)perc_err_stat.getMean()+"%");
		Logger.logln("MEDIAN % ERROR = "+(int)perc_err_stat.getPercentile(50)+"%");
		
		draw("Result",testing_map);
		
		for(String placemark: testing_map.keySet()) {
			for(double[] x : testing_map.get(placemark)) {
				double est = x[0];
				double gt = x[1];
				System.out.println(placemark+";"+(int)est+";"+(int)gt);
			}
		}
	}
	
	
	public static Envelope read(String[] files) throws Exception {
		SimpleRegression sr = new SimpleRegression();
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
				sr.addData(estimated, groundtruth);
				
				List<double[]> p = map.get(placemark);
				if(p==null) {
					p = new ArrayList<double[]>();
					map.put(placemark, p);
				}
				p.add(new double[]{estimated, groundtruth});
			}
			br.close();
		}
		
		return new Envelope(sr,map);
	}
	
	
	public static void draw(String title, Map<String,List<double[]>> map) {
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

class Envelope {
	SimpleRegression sr;
	Map<String,List<double[]>> map;
	
	Envelope(SimpleRegression sr, Map<String,List<double[]>> map) {
		this.sr = sr;
		this.map = map;
	}
	
}


