package analysis.tourist;

import java.awt.BorderLayout;
import java.util.Random;

import javax.swing.JFrame;

import utils.Config;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.RemoveWithValues;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;

public class WekaClassifier {
	
	public static void main(String[] args) throws Exception {
		String city = "Venezia";
		DataSource source = new DataSource(Config.getInstance().base_folder+"/Tourist/"+city+".arff");
		Instances data = source.getDataSet();
		data.setClassIndex(data.attribute("class").index());
		
		System.out.println("***** Attributes:");
		for(int i=0; i<data.numAttributes();i++)
			System.out.println((i+1)+" --> "+data.attribute(i).name());
		System.out.println("***** Num instances: "+data.numInstances());
		
		
		
		//printClasses(data);

		// filter out instances with missing (class) values.
		RemoveWithValues filter = new RemoveWithValues();
		filter.setMatchMissingValues(true);
		filter.setInvertSelection(true);
		filter.setInputFormat(data);
		Instances fdata = Filter.useFilter(data, filter);
		
		
		
		System.out.println("***** Num instances after filtering missing values: "+fdata.numInstances());
		
		//printClasses(fdata);
		
		
		
		Instances train = fdata.trainCV(10, 0);
		Instances test = fdata.testCV(10, 1);
		
		Remove rm = new Remove();
		rm.setAttributeIndices("1,2,3,7,9");  // remove attributes (count from 1)
		// classifier
		J48 j48 = new J48();
		j48.setUnpruned(false); 
		//j48.setConfidenceFactor(0.1f);
		j48.setMinNumObj(10); // minimum number of object per leaf
		// meta-classifier
		FilteredClassifier fc = new FilteredClassifier();
		fc.setFilter(rm);
		fc.setClassifier(j48);
		
		// train
		fc.buildClassifier(train);
		
		
		
		Evaluation eval = new Evaluation(fdata);
		eval.crossValidateModel(fc, fdata, 10, new Random(1));
		System.out.println(eval.toSummaryString("\nResults\n======\n", false));
		System.out.println(eval.toMatrixString());
		
		
		// display the tree
		
		// display classifier
	    JFrame jf = new JFrame("Weka Classifier Tree Visualizer: J48");
	    jf.setSize(500,400);
	    jf.getContentPane().setLayout(new BorderLayout());
	    TreeVisualizer tv = new TreeVisualizer(null,j48.graph(),new PlaceNode2()); 
	    jf.getContentPane().add(tv, BorderLayout.CENTER);
	    jf.addWindowListener(new java.awt.event.WindowAdapter() {
	      public void windowClosing(java.awt.event.WindowEvent e) {
	        jf.dispose();
	      }
	    });
	    jf.setVisible(true);
	    tv.fitToScreen();
		
	   
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
