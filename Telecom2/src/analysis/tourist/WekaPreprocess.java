package analysis.tourist;

import java.io.File;

import utils.Config;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.RemoveWithValues;
import weka.filters.unsupervised.instance.Resample;

public class WekaPreprocess {
	
	public static void main(String[] args) throws Exception {
		preprocess("Venezia","_July2013");
		preprocess("Venezia","_March2014");
		preprocess("Firenze","_July2013");
		preprocess("Firenze","_March2014");
		System.out.println("Done");
	}
	
	public static void preprocess(String city, String post) throws Exception {
		double resample = city.equals("Venezia") ? 4 : 2;
		DataSource source = new DataSource(Config.getInstance().base_folder+"/Tourist/"+city+post+".arff");
		Instances data = source.getDataSet();
		data.setClassIndex(data.attribute("class").index());
		
		// filter out instances with missing (class) values.
		RemoveWithValues rwv = new RemoveWithValues();
		rwv.setMatchMissingValues(true);
		rwv.setInvertSelection(true);
		rwv.setInputFormat(data);
		data = Filter.useFilter(data, rwv);
		
		// resample 2-4% of the instances
		Resample res = new Resample();
		res.setSampleSizePercent(resample);
		res.setNoReplacement(true);
		res.setInputFormat(data);
		data = Filter.useFilter(data, res);
		
		// remove attribute mnt
		Remove rem = new Remove();
		rem.setAttributeIndices("1");
		rem.setInputFormat(data);
		data = Filter.useFilter(data, rem);
		
		ArffSaver saver = new ArffSaver();
		saver.setInstances(data);
		saver.setFile(new File(Config.getInstance().base_folder+"/Tourist/resample/"+city+post+"_resampled.arff"));
		saver.writeBatch();
	}
	
}
