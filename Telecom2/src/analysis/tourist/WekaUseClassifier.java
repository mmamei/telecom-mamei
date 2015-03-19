package analysis.tourist;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.LatexUtils;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.RemoveWithValues;

public class WekaUseClassifier {
	
	public static void main(String[] args) throws Exception {
		
		File dir = new File(Config.getInstance().base_folder+"/Tourist");
		for(File f: dir.listFiles()) {
			if(f.getAbsolutePath().endsWith(".arff")) {
				File classifier = new File(Config.getInstance().base_folder+"/Tourist/Resampled/"+f.getName().replaceAll(".arff", "_resampled_j48.model"));
				//System.out.println(classifier);
				if(classifier.exists())
					classify(f.getAbsolutePath(),classifier.getAbsolutePath());
				else
					System.out.println("Classifier for "+f.getName()+" not found!");
			}
		} 
		
		
		//classify(Config.getInstance().base_folder+"/Tourist/Firenze_March2014.arff",Config.getInstance().base_folder+"/Tourist/resample/Firenze_March2014_resampled.model");
	}
	
	
	public static void classify(String arff, String model) throws Exception {
		
		DataSource source = new DataSource(arff);
		Instances data = source.getDataSet();
		data.setClassIndex(data.attribute("class").index());
	
		Classifier cls = (Classifier) weka.core.SerializationHelper.read(model);
		
		Remove rem = new Remove();
		rem.setAttributeIndices("1,2");
		rem.setInputFormat(data);
		Instances cdata = Filter.useFilter(data, rem);
		
		Evaluation eval = new Evaluation(cdata);
		eval.evaluateModel(cls, cdata);
		System.out.println(eval.toSummaryString("\nResults\n======\n", false));
		System.out.println(eval.toMatrixString());
		
		
		String[] classes = new String[]{"Resident","Tourist","Commuter","Transit","Excursionist"};
		Map<String,Integer> cs = new HashMap<String,Integer>();
		for(int i=0; i<classes.length;i++)
			cs.put(classes[i], i);
		
		int[][] cm = new int[cs.size()][cs.size()];
		
		Map<String,String> mu = new HashMap<String,String>(); // user profile
		for(int i=0; i<data.numInstances();i++) {
			Instance inst = data.instance(i);
			String user = inst.stringValue(0);
			String gt = inst.stringValue(data.classAttribute());
			String clazz = data.classAttribute().value((int)cls.classifyInstance(cdata.instance(i)));
			mu.put(user, clazz);
			
			if(cs.get(gt) == null) continue;
			//System.out.println(user+" "+gt+" "+clazz+" ["+cs.get(gt)+"]["+cs.get(clazz)+"]");
			cm[cs.get(gt)][cs.get(clazz)]++;
		}
		
	
		PrintWriter pw = new PrintWriter(new FileWriter(arff.replace(".arff", "_classes.csv")));
		for(String user: mu.keySet())
			pw.println(user+","+mu.get(user));
		pw.close();
		
		CopyAndSerializationUtils.save(new File(arff.replace(".arff", "_classes.ser")), mu);
		
		String texfile = new File(arff).getName().replace(".arff", ".tex");
		LatexUtils.printTable(Config.getInstance().paper_folder+"/img/tables/"+texfile,"classified as --$>$", classes, classes, cm);
		/*
		for(int j=0; j<classes.length;j++)
			System.out.print(classes[j].substring(0,3)+"\t");
		System.out.println("<-- classified as");
		for(int i=0; i<cm.length;i++) {
			for(int j=0; j<cm[i].length;j++)
				System.out.print(cm[i][j]+"\t");
			System.out.println(classes[i]);
		}
		*/
		
		
	   
		System.out.println("Done!");
	}
	
	
	private static void printClasses(Instances data) {
		int nResident = 0;
		int nTourist = 0;
		int nTransit = 0;
		int nCommuter = 0;
		int nMissing = 0;
		
		// count how many instances per class and how many missing 
		for(int i=0; i<data.numInstances();i++) {
			String c = data.instance(i).stringValue(data.attribute("class").index());
			if(c.equals("Resident")) nResident++;
			if(c.equals("Tourist")) nTourist++;
			if(c.equals("Transit")) nTransit++;
			if(c.equals("Commuter")) nCommuter++;
			if(c.equals("?")) nMissing++;
		}
		
		System.out.println("nResident = "+nResident);
		System.out.println("nTourist = "+nTourist);
		System.out.println("nTransit = "+nTransit);
		System.out.println("nCommuter = "+nCommuter);
		System.out.println("nMissing = "+nMissing);
		System.out.println("TOT = "+(nResident+nTourist+nTransit+nCommuter+nMissing));
	}
	
}
