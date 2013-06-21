package analysis.presence_at_event;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.stat.regression.SimpleRegression;

import utils.Config;
import utils.Logger;
import visual.GraphScatterPlotter;

public class ResultEvaluator {
	public static void main(String[] args) throws Exception {
		
		String file = Config.getInstance().base_dir +"/PresenceCounter/result_0.0_3.csv";
		
		
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
		
		Logger.logln("r="+sr.getR()+", r^2="+sr.getRSquare()+", sse="+sr.getSumSquaredErrors());
		Logger.logln("Y = "+sr.getSlope()+" * X + "+sr.getIntercept());
		for(String placemark: map.keySet()) {
			System.out.println(placemark);
			for(double[] x : map.get(placemark)) {
				double est = Math.max(0, sr.predict(x[0]));
				double gt = x[1];
				double abserr = Math.abs(est - gt);
				System.out.println("GT = "+(int)gt+" EST = "+(int)est+" ABS_ERR = "+(int)abserr+" %ERR = "+(int)100*(abserr/gt)+"%");
			}
		}
		
		
		draw(map);
		
	}
	
	
	public static void draw(Map<String,List<double[]>> map) {
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
		
		new GraphScatterPlotter("Result","Estimated","GroundTruth",data,labels);
	}
}
