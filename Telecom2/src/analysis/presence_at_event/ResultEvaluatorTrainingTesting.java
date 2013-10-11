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

public class ResultEvaluatorTrainingTesting {
	public static void main(String[] args) throws Exception {
		
		String training = Config.getInstance().base_dir +"/PresenceCounterProbability/result_0.0_5t.csv";
		String testing = Config.getInstance().base_dir +"/PresenceCounterProbability/result_0.0_5.csv";
		
		Object[] regression_map = read(training);
		SimpleRegression training_sr = (SimpleRegression)regression_map[0];
		Map<String,List<double[]>> training_map = (Map<String,List<double[]>>)regression_map[1];
		
		regression_map = read(testing);
		SimpleRegression testing_sr = (SimpleRegression)regression_map[0];
		Map<String,List<double[]>> testing_map = (Map<String,List<double[]>>)regression_map[1];
		
		Logger.logln("TRAINING: r="+training_sr.getR()+", r^2="+training_sr.getRSquare()+", sse="+training_sr.getSumSquaredErrors());
		Logger.logln("TESTING: r="+testing_sr.getR()+", r^2="+testing_sr.getRSquare()+", sse="+testing_sr.getSumSquaredErrors());
		
		
		DescriptiveStatistics ds1 = new DescriptiveStatistics();
		DescriptiveStatistics ds2 = new DescriptiveStatistics();
		
		
		
		double s = training_sr.getSlope();
		double sconf = training_sr.getSlopeConfidenceInterval(); 
		
		double i = training_sr.getIntercept();
		double iconf = training_sr.getInterceptStdErr();
		
		Logger.logln("Y = "+s+" * X + "+i);
		
		
		Logger.logln("SLOPE CONF INTERVAL =  ["+(s-sconf)+","+(s+sconf)+"]");
		Logger.logln("INTERCEPT CONNF INTERVAL =  ["+(i-iconf)+","+(i+iconf)+"]");
		
		for(String placemark: testing_map.keySet()) {
			System.out.println(placemark);
			List<double[]> list_est = new ArrayList<double[]>(); 
			for(double[] x : testing_map.get(placemark)) {
				double est = Math.max(0, training_sr.predict(x[0]));
				double gt = x[1];
				list_est.add(new double[]{est,gt});
				
				double abserr = Math.abs(est - gt);
				ds1.addValue(abserr);
				
				double perr = 100*(abserr/gt);
				Logger.logln("GT = "+(int)gt+" EST = "+(int)est+" ABS_ERR = "+(int)abserr+" %ERR = "+(int)perr+"%");
				ds2.addValue(perr);
			}
			testing_map.put(placemark, list_est);
		}
		
		Logger.logln("MEAN ABS ERROR = "+(int)ds1.getMean());
		Logger.logln("MEDIAN ABS ERROR = "+(int)ds1.getPercentile(50));
		
		Logger.logln("MEAN % ERROR = "+(int)ds2.getMean()+"%");
		Logger.logln("MEDIAN % ERROR = "+(int)ds2.getPercentile(50)+"%");
		draw("Result",testing_map);
		
		
		for(String placemark: testing_map.keySet()) {
			for(double[] x : testing_map.get(placemark)) {
				double est = x[0];
				double gt = x[1];
				System.out.println(placemark+";"+(int)est+";"+(int)gt);
			}
		}
		
	}
	
	
	public static Object[] read(String file) throws Exception {
		SimpleRegression sr = new SimpleRegression();
		Map<String,List<double[]>> map = new HashMap<String,List<double[]>>();
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
		return new Object[]{sr,map};
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
