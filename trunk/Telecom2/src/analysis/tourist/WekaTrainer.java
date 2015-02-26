package analysis.tourist;

import java.util.Random;

import utils.Config;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;

public class WekaTrainer {
	
	public static void main(String[] args) throws Exception {
		train(Config.getInstance().base_folder+"/Tourist/resample/Venezia_July2013_resampled.arff");
		train(Config.getInstance().base_folder+"/Tourist/resample/Venezia_March2014_resampled.arff");
		train(Config.getInstance().base_folder+"/Tourist/resample/Firenze_July2013_resampled.arff");
		train(Config.getInstance().base_folder+"/Tourist/resample/Firenze_March2014_resampled.arff");
	}
	
	
	public static void train(String arff) throws Exception {
		
		DataSource source = new DataSource(arff);
		Instances data = source.getDataSet();
		data.setClassIndex(data.attribute("class").index());
		
		System.out.println("***** Attributes:");
		for(int i=0; i<data.numAttributes();i++)
			System.out.println((i+1)+" --> "+data.attribute(i).name());
		System.out.println("***** Num instances: "+data.numInstances());
		
		
		
		printClasses(data);

		
		Instances train = data.trainCV(10, 0);
		
		// classifier
		J48 j48 = new J48();
		j48.setUnpruned(false); 
		//j48.setConfidenceFactor(0.1f);
		j48.setMinNumObj(10); // minimum number of object per leaf
	
		// train
		j48.buildClassifier(train);
		
		SerializationHelper.write(arff.replace("arff", "model"), j48);
		
		Evaluation eval = new Evaluation(data);
		eval.crossValidateModel(j48, data, 10, new Random(1));
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
