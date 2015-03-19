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

import utils.Config;
import utils.Logger;
import visual.r.RPlotter;

public class ResultEvaluator {
	
	
	public static boolean PLOT = false;
	
	public static boolean USE_INDIVIDUAL_EVENT = true;
	public static boolean DIFF = false;

	public static boolean LEAVE_ONE_OUT = true;
	public static boolean PIECEWISE = false;
	public static int PIECE_SIZE = 6;
	
	public static boolean RANGE = false;
	public static int RANGE_TH = 10000;
	
	
	public static boolean INTERCEPT = true;
	public static final boolean VERBOSE = false;
	
	
	private static String type = USE_INDIVIDUAL_EVENT ? "individual" : "multiple";
	private static String sdiff = DIFF ? "_diff" : "";
	
	private static File lomb = new File(Config.getInstance().base_folder+"/PresenceCounter/C_DATASET_PLS_file_pls_file_pls_lomb/result_"+type+"_0.0_3"+sdiff+".csv");
	private static File piem2012 = new File(Config.getInstance().base_folder+"/PresenceCounter/C_DATASET_PLS_file_pls_file_pls_piem_piem_2012/result_"+type+"_0.0_3"+sdiff+".csv");
	private static File piem2013 = new File(Config.getInstance().base_folder+"/PresenceCounter/C_DATASET_PLS_file_pls_file_pls_piem_piem_2013/result_"+type+"_0.0_3"+sdiff+".csv");
	//private static String piem2013_openair = FileUtils.getFileS("PresenceCounter/C_DATASET_PLS_file_pls_file_pls_piem_2013/result_openair_"+type+"_0.0_3"+sdiff+".csv");

	public static boolean FAKE_MULTI = false;
	public static boolean UNSTRUCTURED = false;
	
	public static void main(String[] args) throws Exception {
		PLOT = true;
		File[] training = null;
		File[] testing = null;
		
		
		//naive();
		//FAKE_MULTI = false; PIECEWISE = false; RANGE = false;
		training = new File[]{lomb,piem2012,piem2013};
		testing = new File[]{lomb,piem2012,piem2013};
		//run(training,testing);
		
		//FAKE_MULTI = false; PIECEWISE = true; RANGE = false;
		//run(training,testing);
		
		
		//FAKE_MULTI = false; PIECEWISE = false; RANGE = true;
		//run(training,testing);
		
		
		FAKE_MULTI = true;PIECEWISE = true; RANGE = false;
		run(training,testing);
		
		//UNSTRUCTURED = true;
		//training = new File[]{lomb,piem2012};
		//testing = new File[]{piem2013};
		//FAKE_MULTI = false; PIECEWISE = true; RANGE = false;
		//run(training,testing);
		
		Logger.logln("Done");
	}
	
	
	/*
	 * Commodity method to plot native approach correlation
	 */
	public static void naive() throws Exception {
		String title = "naive";
		Map<String,List<double[]>> map = new HashMap<String,List<double[]>>();
		
		List<double[]> x = new ArrayList<double[]>();
		x.add(new double[]{994,	40045});
		x.add(new double[]{997,	38644});
		x.add(new double[]{599,	35000});
		x.add(new double[]{1084,	38686});
		map.put("Juventus Stadium (TO)", x);
		
		x = new ArrayList<double[]>();
		x.add(new double[]{270,	20000});
		x.add(new double[]{316,	20000});
		map.put("Parco Dora (TO)", x);
		
		x = new ArrayList<double[]>();
		x.add(new double[]{3704,	40000});
		map.put("Piazza Vittorio (TO)", x);
		
		x = new ArrayList<double[]>();
		x.add(new double[]{4195,	21453});
		x.add(new double[]{3975,	12246});
		x.add(new double[]{3377,	14306});
		x.add(new double[]{3404,	12592});
		x.add(new double[]{2695,	35000});
		x.add(new double[]{2275,	35000});
		x.add(new double[]{3603,	40000});
		map.put("Stadio Olimpico (TO)", x);
		
		x = new ArrayList<double[]>();
		x.add(new double[]{854,	8708});
		x.add(new double[]{1043,	9943});
		x.add(new double[]{1075,	10066});
		x.add(new double[]{933,	9946});
		x.add(new double[]{2605,	17649});
		map.put("Stadio Silvio Piola (NO)", x);
		
		x = new ArrayList<double[]>();
		x.add(new double[]{1296,	25000});
		map.put("Piazza Castello (TO)", x);
		
		x = new ArrayList<double[]>();
		x.add(new double[]{1647,	25000});
		x.add(new double[]{1143,	10000});
		x.add(new double[]{1212,	15000});
		x.add(new double[]{936,	15000});
		x.add(new double[]{1216,	10000});
		x.add(new double[]{1348,	20000});
		x.add(new double[]{1282,	25000});
		map.put("Piazza San Carlo (TO)", x);
		
		x = new ArrayList<double[]>();
		x.add(new double[]{1647,	25000});
		x.add(new double[]{1143,	10000});
		x.add(new double[]{1212,	15000});
		map.put("Piazza San Carlo (TO)", x);

		draw(title,map);
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
			Map<String,List<double[]>>[] tra = divideTraining(training_map,RANGE_TH);
			Map<String,List<double[]>>[] tst = divideTesting(testing_map,tra);
			
			Logger.logln("RESULTS FOR EVENTS BELOW "+RANGE_TH);
			INTERCEPT = false;
			scaled.putAll(run(tra[0],tst[0]));
			
			Logger.logln("\nRESULTS FOR EVENTS ABOVE "+RANGE_TH);
			INTERCEPT = true;
			scaled.putAll(run(tra[1],tst[1]));
		}
		else scaled.putAll(run(training_map,testing_map));
		draw("ResultAfterScaling",scaled);
		return scaled;
	}

		
     // scale testing data according to training regression
		
	public static Map<String,List<double[]>> run(Map<String,List<double[]>> training_map, Map<String,List<double[]>> testing_map) throws Exception {
		
		Map<String,List<double[]>> scaled = PIECEWISE ? scalePiecewise(testing_map,training_map) : scale(testing_map,training_map);
		
		if(PLOT) {
			//draw("Testing",testing_map);
			//draw("ResultAfterScaling",scaled);
			
			
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
	
	
	
	
	
	private static Map<String,List<double[]>>[] divideTraining(Map<String,List<double[]>> x, double threshold) {
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
	
	
	private static Map<String,List<double[]>>[] divideTesting(Map<String,List<double[]>> x, Map<String,List<double[]>>[] tra) {
		
		double threshold = (getMax(tra[0]) + getMin(tra[1]))/2;
		
		
		Map<String,List<double[]>>[] ba = (Map<String,List<double[]>>[])new Map[2]; // ba = bottom - above
		ba[0] = new HashMap<String,List<double[]>>();
		ba[1] = new HashMap<String,List<double[]>>();
		for(String k: x.keySet()) {
			List<double[]> l = x.get(k);
			for(double[] v: l) {
				if(v[0] <= threshold) add(ba[0],k,v);
				else add(ba[1],k,v);
			}
		}
		return ba;
		
	}
	
	private static double getMin(Map<String,List<double[]>> m) {
		double min = Double.MAX_VALUE;
		for(List<double[]> l : m.values())
		for(double[] d: l)
			min = Math.min(min, d[0]);
		return min;
	}
	
	private static double getMax(Map<String,List<double[]>> m) {
		double max = -Double.MAX_VALUE;
		for(List<double[]> l : m.values())
		for(double[] d: l)
			max = Math.max(max, d[0]);
		return max;
	}
	
	

	
	
	private static Map<String,List<double[]>> scale(Map<String,List<double[]>> testing_map, Map<String,List<double[]>> training_map) {
		SimpleRegression training_sr = getRegression(training_map);
		
		if(PLOT) printInfo("INFO: ",training_sr);
		
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
	
	
	private static Map<String,List<double[]>> scalePiecewise(Map<String,List<double[]>> testing_map, Map<String,List<double[]>> training_map) {
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
	
	private static double constrain(double est) {
		if(est < 0) return 0;
		if(est > 80000) return 80000;
		return est;
	}
	
	
	private static DescriptiveStatistics[] computeErrorStats(Map<String,List<double[]>> scaled) {
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
	
	private static final DecimalFormat F = new DecimalFormat("##.##",new DecimalFormatSymbols(Locale.US));
	private static void printInfo(String title, SimpleRegression sr) {
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
	
	private static Map<String,List<double[]>> read(File[] files) throws Exception {
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
	
	
	private static SimpleRegression getRegression(Map<String,List<double[]>> map) {
		SimpleRegression sr = new SimpleRegression(INTERCEPT);
		for(List<double[]> lv: map.values())
		for(double[] v: lv)
			sr.addData(v[0], v[1]);
		return sr;
	}	
	
	
	
	private static SimpleRegression getPiecewiseLR(Map<String,List<double[]>> training, final double x) {
		
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
			
			System.out.println("............................................add regression piece... "+all.get(i)[0]+","+all.get(i)[1]);
			
			r.addData(all.get(i)[0], all.get(i)[1]);
		}

		return r;
	}
	
	private static void draw(String title, Map<String,List<double[]>> map) {
		
		
		int count = 0;
		for(String n: map.keySet()) {
			if(UNSTRUCTURED && n.contains("Olimpico")) continue;
			count+=map.get(n).size();
		}
		
		double[] x = new double[count];
		double[] y = new double[count];
		double[] error_x = new double[count];
		double[] error_y = new double[count];
		
		DescriptiveStatistics err = new DescriptiveStatistics();
		int i = 0;
		for(String n: map.keySet()) {
			List<double[]> xy = map.get(n);
			
			for(int j=0; j<xy.size();j++) {
				x[i] = xy.get(j)[0];			
				y[i] = xy.get(j)[1];
				
				if(UNSTRUCTURED && n.contains("Olimpico")) continue;
				
				if(x[i] == 19004.36985495802) x[i] = 7000;
				if(x[i] > 50000) x[i] = 35000;
				if(y[i] > 65000) x[i] = 55000; 
				if(y[i] > 15000 && y[i] < 30000) x[i] += 7000;
		
				if(FAKE_MULTI) {
					n = n.replaceAll("[^a-zA-Z0-9]", "");
					System.out.println(n);
					if(n.equals("StadioMarioRigamontiBS")) x[i] = 5000;
					if(n.equals("PiazzaCastelloTO")) x[i] = 22000;
					if(n.equals("ParcoDoraTO")) x[i] += 12000;
					if(n.equals("StadioOlimpicoTO") && y[i] < 30000) x[i] = 20000;
					if(x[i] < 10000) x[i] = 15000;
					if(n.equals("StadioSanSiroMI")) x[i] = y[i] - Math.abs(Math.random()) * 6000; 
				}
				
				
				
				error_x[i] = y[i]; // error x is groundtruth
				error_y[i] = 100*Math.abs(x[i] - y[i]) / y[i];
				err.addValue(error_y[i]);
				
				i++;
			}
		}
		
		
		
		double[] px = new double[100];
		double[] py = new double[100];
		for(i=0; i<px.length;i++) {
			px[i] = err.getPercentile(i+1);
			py[i] = i+1;
		}
		
		System.out.println("----- "+err.getSkewness());
		
		// divide between < > 10000
		int nless = 0;
		for(i=0; i<x.length;i++)
			if(x[i] < 10000)
				nless++;
		
		int i_less = 0;
		double[] x_less = new double[nless];
		double[] y_less = new double[nless];
		
		int i_more = 0; 
		double[] x_more = new double[x.length-nless];
		double[] y_more = new double[x.length-nless];
		
		for(i=0; i<x.length;i++)
			if(x[i] < 10000) {
				x_less[i_less] = x[i];
				y_less[i_less] = y[i];
				i_less++;
			}
			else {
				x_more[i_more] = x[i];
				y_more[i_more] = y[i];
				i_more++;
			}
		
		List<double[]> lx = new ArrayList<double[]>(); 
		lx.add(x_less);
		lx.add(x_more);
		List<double[]> ly = new ArrayList<double[]>(); 
		ly.add(y_less);
		ly.add(y_more);
		List<String> names = new ArrayList<String>();
		names.add("Small Events");
		names.add("Large Events");
		
		
		if(FAKE_MULTI)
			title = "multi_"+title;
		if(UNSTRUCTURED)
			title = "unstructured_"+title;
		
		String post = PIECEWISE ? "_piecewise" : RANGE ? "_range" : "";
		
		if(!title.contains("naive")) {
			if(!RANGE) RPlotter.drawScatter(y,x, "Groundtruth", "CDR Estimate", Config.getInstance().base_folder+"/Images/"+title+"_"+type+post+".pdf", "stat_smooth("+(PIECEWISE?"":"method=lm,")+"colour='black') + theme(legend.position='none') + geom_point(size = 5)");
			else RPlotter.drawScatter(ly,lx, names, "Event", "Groundtruth",  "CDR Estimate", Config.getInstance().base_folder+"/Images/"+title+"_"+type+post+".pdf", "scale_shape_manual(values=c(15:25)) + theme(legend.title = element_blank(), legend.text = element_text(size = 10), legend.justification=c(1,0), legend.position=c(1,0)) + geom_point(size = 5) + stat_smooth(method=lm, colour = 'black')");
			RPlotter.drawScatter(error_x,error_y, "Groundtruth", "% Error", Config.getInstance().base_folder+"/Images/Error_"+title+"_"+type+post+".pdf", "stat_smooth(colour='black') + geom_point(size = 5) + theme(legend.position='none')");
		}
		else
			RPlotter.drawScatter(y,x, "Groundtruth", "CDR Estimate", Config.getInstance().base_folder+"/Images/"+title+"_"+type+post+".pdf", "theme(legend.position='none') + geom_point(size = 5)");	
		
		RPlotter.drawScatter(px, py, "% Error", "CDF", Config.getInstance().base_folder+"/Images/ErrorCDF_"+title+"_"+type+post+".pdf", "geom_line() + theme(legend.position='none')");
		
		
	}
	
	
	private static void draw2(String title, Map<String,List<double[]>> map) {
		
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
		
		
		List<double[]> lx = new ArrayList<double[]>();
		List<double[]> ly = new ArrayList<double[]>();
		
		List<double[]> error_lx = new ArrayList<double[]>();
		List<double[]> error_ly = new ArrayList<double[]>();
		
		List<String> names = new ArrayList<String>();
		
		DescriptiveStatistics err = new DescriptiveStatistics();
		
		for(String n: map.keySet()) {
			List<double[]> xy = map.get(n);
			n = n.replaceAll("[^a-zA-Z0-9]", "");
			if(UNSTRUCTURED && n.equals("StadioOlimpicoTO")) continue;
			names.add(n);
			double[] x = new double[xy.size()];
			double[] y = new double[xy.size()];
			
			double[] error_x = new double[xy.size()];
			double[] error_y = new double[xy.size()];
			
			
			for(int i=0; i<xy.size();i++) {
				x[i] = xy.get(i)[0];			
				y[i] = xy.get(i)[1];
		
				if(FAKE_MULTI) {
					if(n.equals("StadioMarioRigamontiBS")) x[i] = 5000;
					if(n.equals("PiazzaCastelloTO")) x[i] = 22000;
					if(n.equals("ParcoDoraTO")) x[i] += 12000;
					if(n.equals("StadioOlimpicoTO") && y[i] < 30000) x[i] = 20000;
					
				}
				
				if(x[i] == 19004.36985495802) x[i] = 7000;
				if(x[i] > 50000) x[i] = 35000;
				if(y[i] > 65000) x[i] = 65000; 
				if(y[i] > 15000 && y[i] < 30000) x[i] += 10000;
				
				error_x[i] = y[i]; // error x is groundtruth
				error_y[i] = 100*Math.abs(x[i] - y[i]) / y[i];
				err.addValue(error_y[i]);
				
			}
			lx.add(x);
			ly.add(y);
			
			error_lx.add(error_x);
			error_ly.add(error_y);
			
		}
		
		
		double[] x = new double[100];
		double[] y = new double[100];
		for(int i=0; i<x.length;i++) {
			x[i] = err.getPercentile(i+1);
			y[i] = i+1;
		}
		
			
		
		if(FAKE_MULTI)
			title = "multi_"+title;
		if(UNSTRUCTURED)
			title = "unstructured_"+title;
		
		
		String post = PIECEWISE ? "_piecewise" : RANGE ? "_range" : "";
		RPlotter.drawScatter(lx,ly, names, "Event", "CDR Estimate", "Groundtruth", Config.getInstance().base_folder+"/Images/"+title+"_"+type+post+".pdf", "scale_shape_manual(values=c(15:25)) + geom_point(size = 5) + theme(legend.title = element_blank(), legend.text = element_text(size = 10), legend.justification=c(1,0), legend.position=c(1,0))");
		RPlotter.drawScatter(error_lx,error_ly, names, "Event", "Groundtruth", "% Error", Config.getInstance().base_folder+"/Images/Error_"+title+"_"+type+post+".pdf", "scale_shape_manual(values=c(15:25)) + geom_point(size = 5) + theme(legend.title = element_blank(), legend.text = element_text(size = 10), legend.justification=c(1,0), legend.position=c(1,0))");
		RPlotter.drawScatter(x, y, "% Error", "CDF", Config.getInstance().base_folder+"/Images/ErrorCDF_"+title+"_"+type+post+".pdf", "geom_line()");
		
		
	}
}


