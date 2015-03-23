package analysis.tourist;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import utils.Config;
import utils.CopyAndSerializationUtils;
import visual.r.RPlotter;
import visual.text.TextPlotter;

public class GroundTruthAnalysis {
	public static void main(String[] args) throws Exception {
		//plotBaseGT();
		plotCorr();
	}
	
	public static void plotBaseGT() {
		
		String[] months = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
		
		List<String> names  = new ArrayList<String>();
		names.add("Venice-Fore");
		names.add("Venice-IT");
		names.add("Florence-Fore");
		names.add("Florence-IT");
		names.add("Lecce-Fore");
		names.add("Lecce-IT");
		names.add("Torino-Fore");
		names.add("Torino-IT");
		List<double[]> data = new ArrayList<double[]>();
		data.add(new double[]{320551,452961,627140,709738,952294,919715,1066091,1043505,966843,926649,516848,391011});
		data.add(new double[]{109439,136465,144965,147174,137277,123871,114025,135573,135308,140027,163045,128301});
		data.add(new double[]{285167,263507,465698,521187,664739,642474,756472,698720,710132,668742,371250,302678});
		data.add(new double[]{166427,147885,211550,206242,161227,148546,113979,114008,129452,163062,193211,176575});
		data.add(new double[]{1260,1494,2181,3964,10834,14541,26252,21014,18492,10219,1825,1143});
		data.add(new double[]{2530,2429,3851,5731,8379,39345,82838,115752,35318,6087,3208,2602});
		data.add(new double[]{135296,132147,142541,85565,83801,88246,123281,163838,105994,94738,77520,79034});
		data.add(new double[]{433094,456669,486802,423259,372892,330383,460200,363032,333210,343581,288618,332864});

		RPlotter.drawLine(months, data, names, "regions", "months", "n. tourists", Config.getInstance().paper_folder+"/img/dati-ufficiali-month.pdf", "theme(legend.position=c(0.15, 0.8),legend.background = element_rect(size=1))");
		
		
		
		/*
		String[] countries = new String[]{"IT","US","FR","UK","DE","AU","CN","ES","JP","BR","Other"};
		double[] prob = new double[]{15,11,10,8,7,4,3,3,3,3,32};
		RPlotter.drawBar(countries, prob, "countries", "%",  Config.getInstance().paper_folder+"/img/dati-ufficiali-mnt-venice.pdf","annotate('text', x = 2, y = 35, size=7, label = 'Venice')");
		
		
		countries = new String[]{"IT","US","FR","JP","UK","ES","DE","CN","BR","RU","Other"};
		prob = new double[]{23,15,5,5,4,4,3,3,3,3,30};
		RPlotter.drawBar(countries, prob, "countries", "%",  Config.getInstance().paper_folder+"/img/dati-ufficiali-mnt-florence.pdf","annotate('text', x = 2, y = 35, size=7, label = 'Firenze')");	
		*/
	}
	
	static final DecimalFormat F = new DecimalFormat("#.##",new DecimalFormatSymbols(Locale.US));
	public static void plotCorr() throws Exception {
		double[] fm14 =  getTouristITFORE(Config.getInstance().base_folder+"/Tourist/Firenze_March2014_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_fi_Firenze_cellXHour_March2014.csv");
		double[] vm14 =  getTouristITFORE(Config.getInstance().base_folder+"/Tourist/Venezia_March2014_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_ve_Venezia_cellXHour_March2014.csv");
		double[] fj13 =  getTouristITFORE(Config.getInstance().base_folder+"/Tourist/Firenze_July2013_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_fi_Firenze_cellXHour_July2013.csv");
		double[] vj13 =  getTouristITFORE(Config.getInstance().base_folder+"/Tourist/Venezia_July2013_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_ve_Venezia_cellXHour_July2013.csv");
		double[] to14 =  getTouristITFORE(Config.getInstance().base_folder+"/Tourist/Torino_Oct2014_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_piem_Torino_cellXHour_Oct2014.csv");
		double[] la14 =  getTouristITFORE(Config.getInstance().base_folder+"/Tourist/Lecce_Aug2014_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_pu_Lecce_cellXHour_Aug2014.csv");
		double[] ls14 =  getTouristITFORE(Config.getInstance().base_folder+"/Tourist/Lecce_Sep2014_noregion_classes.ser",Config.getInstance().base_folder+"/UserEventCounter/file_pls_pu_Lecce_cellXHour_Sep2014.csv");
		
		double[] res = new double[]{fm14[0],fm14[1],vm14[0],vm14[1],fj13[0],fj13[1],vj13[0],vj13[1],to14[0],to14[1],la14[0],la14[1],ls14[0],ls14[1]};
		double[] gt = new double[]{238949,430781,144965,627140,113979,756472,114025,1066091,343581,94738,(0.08*1446905),(0.19*110600),(0.08*441474),(0.19*97325)};
				
		
		SimpleRegression sr = new SimpleRegression();
		for(int i=0; i<res.length;i++)
			sr.addData(res[i],gt[i]);
		
		System.out.println("r = "+sr.getR());
		System.out.println("r^2 = "+sr.getRSquare());
		
		double avg_rel_abs_error = 0;
		for(int i=0; i<gt.length;i++) 
			avg_rel_abs_error += Math.abs(gt[i]-sr.predict(res[i])) / gt[i];
		avg_rel_abs_error /= gt.length;
		
		RPlotter.drawScatter(gt, res, "GT Tourists", "CDR Tourists", Config.getInstance().paper_folder+"/img/correlation.pdf", "stat_smooth(method=lm,colour='black') + theme(legend.position='none') + geom_point(size = 5)");
		
		
		Map<String,Object> tm = new HashMap<String,Object>();
		tm.put("r", F.format(sr.getR()));
		tm.put("intercept", ((int)sr.getIntercept()));
		tm.put("slope",  F.format(sr.getSlope()));
		tm.put("avg_rel_abs_error", ((int)(100.0*avg_rel_abs_error)));
		/*
		for(String profile: profile_descriptions.keySet())
			tm.put(profile+"Places", profile_descriptions.get(profile));
		*/
		TextPlotter.getInstance().run(tm, "src/analysis/tourist/GroundTruthAnalysisCorr.ftl", Config.getInstance().paper_folder+"/img/correlation.tex");
	}
	
	public static double[] getTouristITFORE(String classes_ser_file, String cellXHourFile) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(new File(cellXHourFile)));
		
		Map<String,String> user_prof = (Map<String,String>)CopyAndSerializationUtils.restore(new File(classes_ser_file));
		
		
		Map<String,Integer> map = new HashMap<String,Integer>();
		int c = 0;
		for(String profile: GTExtractor.PROFILES) {
			map.put(profile, c);
			c++;
		}
		
		double[] it_count = new double[GTExtractor.PROFILES.length];
		double[] fore_count = new double[GTExtractor.PROFILES.length];
		
		
		String line;
		while((line=br.readLine())!=null) {
			if(line.startsWith("//")) continue;
			String[] e = line.split(",");
			String user =e[0];
			String profile = user_prof.get(user);
			if(profile!=null) {
				if(e[1].startsWith("222")) it_count[map.get(profile)]++;
				else  fore_count[map.get(profile)]++;
			}
		}
		br.close();
		
		return new double[]{it_count[map.get("Tourist")],fore_count[map.get("Tourist")]};
	}
	
	
	
	
}
