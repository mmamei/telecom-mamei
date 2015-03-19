package analysis.tourist;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import utils.Config;
import utils.LatexUtils;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.Resample;

public class WekaCompareClassifiers {
	
public static void main(String[] args) throws Exception {
		
		// place --> pct correct for various classifiers
		Map<String, double[]> map = new TreeMap<String,double[]>();
	
		File dir = new File(Config.getInstance().base_folder+"/Tourist");
		for(File f: dir.listFiles()) {
			if(f.getAbsolutePath().endsWith(".arff")) {
				
				//if(!f.getName().contains("Firenze_July2013_noregion")) continue;
				
				double[] pctCorrect = new double[WekaTrainer.classifiers.length];
				for(int i=0; i<WekaTrainer.classifiers.length;i++) {
					File classifier = new File(Config.getInstance().base_folder+"/Tourist/Resampled/"+f.getName().replaceAll(".arff", "_resampled_"+WekaTrainer.classifiers[i]+".model"));
					if(classifier.exists()) {
						double resample = WekaTrainer.classifiers[i].equals("nn1") ? 1 : 10;
						pctCorrect[i] = classify(f.getAbsolutePath(),classifier.getAbsolutePath(),resample);
					}
					else
						System.out.println("Classifier "+WekaTrainer.classifiers[i]+" for "+f.getName()+" not found!");
				}
				String name = f.getName().replaceAll(".arff", "");
				name = name.replaceAll("_", " ");
				name = name.replaceAll("noregion", "");
				map.put(name, pctCorrect);
			}
		}
	
		
		double[][] m = new double[map.size()][WekaTrainer.classifiers.length];
		String[] places = new String[map.size()];
		int i = 0;
		for(String p: map.keySet()) {
			places[i] = p;
			for(int j=0; j<m[i].length;j++)
				m[i][j] = map.get(p)[j];
			i++;
		}
		String texfile = "compare_classifiers.tex";
		
		
		
		LatexUtils.printTable(Config.getInstance().paper_folder+"/img/tables/"+texfile, "", places, WekaTrainer.classifiers, m);
		
	}
	
	
	public static double classify(String arff, String model, double resample) throws Exception {
		
		System.out.println("\n*************************************************************************************************************");
		System.out.println("RUNNING CLASSIFIER "+model+"\n");
		
		DataSource source = new DataSource(arff);
		Instances data = source.getDataSet();
		data.setClassIndex(data.attribute("class").index());
		
		
		Resample res = new Resample();
		res.setSampleSizePercent(resample);
		res.setNoReplacement(true);
		res.setInputFormat(data);
		data = Filter.useFilter(data, res);
	
		Classifier cls = (Classifier) weka.core.SerializationHelper.read(model);
		
		Remove rem = new Remove();
		rem.setAttributeIndices("1,2");
		rem.setInputFormat(data);
		Instances cdata = Filter.useFilter(data, rem);
		
		Evaluation eval = new Evaluation(cdata);
		eval.evaluateModel(cls, cdata);
		System.out.println(eval.toSummaryString("\nResults\n======\n", false));
		return eval.pctCorrect();
	}
}
