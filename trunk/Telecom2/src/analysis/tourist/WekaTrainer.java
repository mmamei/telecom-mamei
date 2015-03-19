package analysis.tourist;

import java.io.File;
import java.util.Random;

import utils.Config;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.classifiers.rules.OneR;
import weka.classifiers.functions.Logistic;
import weka.classifiers.lazy.IB1;
import weka.classifiers.meta.ClassificationViaClustering;
import weka.clusterers.SimpleKMeans;
import weka.classifiers.neural.lvq.Som;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;

public class WekaTrainer {
	
	static String[] classifiers = new String[]{"j48","1R","logistic","nn1","clustering-kmeans","clustering-som"};
	//static String[] classifiers = new String[]{"clustering-kmeans"};
	
	
	public static void main(String[] args) throws Exception {
		
		
		File resampledDir = new File(Config.getInstance().base_folder+"/Tourist/Resampled");
		for(File file: resampledDir.listFiles()) {
			if(file.getAbsolutePath().endsWith(".arff"))
				for(String classifier: classifiers)
					train(file.getAbsolutePath(),classifier);
		}
		
		//train(Config.getInstance().base_folder+"/Tourist/resample/Firenze_March2014_resampled.arff");
		System.out.println("Done!");
	}
	
	
	public static void train(String arff, String classifier) throws Exception {
		
		System.out.println("\n************************************************************************************************************");
		System.out.println(arff+"\n");
		
		DataSource source = new DataSource(arff);
		Instances data = source.getDataSet();
		data.setClassIndex(data.attribute("class").index());
		
		System.out.println("***** Attributes:");
		for(int i=0; i<data.numAttributes();i++)
			System.out.println((i+1)+" --> "+data.attribute(i).name());
		System.out.println("***** Num instances: "+data.numInstances());
		
		
		printClasses(data);

		
		Instances train = data.trainCV(10, 0);
		
		Classifier c = null;
		
		if(classifier.equals("j48")) {
			// classifier
			J48 j48 = new J48();
			j48.setUnpruned(false); 
			//j48.setConfidenceFactor(0.1f);
			j48.setMinNumObj(10); // minimum number of object per leaf
			c = j48;
		}
		else if(classifier.equals("1R")) {
			OneR oner = new OneR();
			c = oner;
		}
		else if(classifier.equals("logistic")) {
			Logistic log = new Logistic();
			c = log;
		}
		else if(classifier.equals("nn1")) {
			IB1 ib1 = new IB1();
			c = ib1;
		}
		else if(classifier.startsWith("clustering")) {
			ClassificationViaClustering cvc = new ClassificationViaClustering();
			String type = classifier.substring(classifier.indexOf("-")+1);
			if(type.equals("kmeans")) {
				cvc.setClusterer(new SimpleKMeans());
				c = cvc;
			}
			if(type.equals("som")) {
				c = new Som();
			}
		}
		
		
		if(c == null) {
			System.err.println(classifier+" NOT FOUND!!!");
			return;
		}
		
		// train
		c.buildClassifier(train);
		
		SerializationHelper.write(arff.replace(".arff", "_"+classifier+".model"), c);
		
		Evaluation eval = new Evaluation(data);
		eval.crossValidateModel(c, data, 10, new Random(1));
		System.out.println(eval.toSummaryString("\nResults\n======\n", false));
		System.out.println(eval.toMatrixString());
		
	   
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
