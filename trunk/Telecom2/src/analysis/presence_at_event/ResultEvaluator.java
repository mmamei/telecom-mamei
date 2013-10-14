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
		run(files);
	}
	
	public static void run(String[] files) throws Exception {
		
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
		DescriptiveStatistics ds1 = new DescriptiveStatistics();
		DescriptiveStatistics ds2 = new DescriptiveStatistics();
		
		Logger.logln("r="+sr.getR()+", r^2="+sr.getRSquare()+", sse="+sr.getSumSquaredErrors());
		
		double s = sr.getSlope();
		double sconf = sr.getSlopeConfidenceInterval(); 
		
		double i = sr.getIntercept();
		double iconf = sr.getInterceptStdErr();
		
		Logger.logln("Y = "+s+" * X + "+i);
		
		
		Logger.logln("SLOPE CONF INTERVAL =  ["+(s-sconf)+","+(s+sconf)+"]");
		Logger.logln("INTERCEPT CONNF INTERVAL =  ["+(i-iconf)+","+(i+iconf)+"]");
		
		for(String placemark: map.keySet()) {
			System.out.println(placemark);
			for(double[] x : map.get(placemark)) {
				double est = Math.max(0, sr.predict(x[0]));
				double gt = x[1];
				double abserr = Math.abs(est - gt);
				
				ds1.addValue(abserr);
				
				double perr = 100*(abserr/gt);
				Logger.logln("GT = "+(int)gt+" EST = "+(int)est+" ABS_ERR = "+(int)abserr+" %ERR = "+(int)perr+"%");
				ds2.addValue(perr);
			}
		}
		
		Logger.logln("MEAN ABS ERROR = "+(int)ds1.getMean());
		Logger.logln("MEDIAN ABS ERROR = "+(int)ds1.getPercentile(50));
		
		Logger.logln("MEAN % ERROR = "+(int)ds2.getMean()+"%");
		Logger.logln("MEDIAN % ERROR = "+(int)ds2.getPercentile(50)+"%");
		draw("Result",map);
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
