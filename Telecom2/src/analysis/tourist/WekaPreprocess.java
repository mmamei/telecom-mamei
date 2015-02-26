package analysis.tourist;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;

import analysis.PLSSpaceDensity;
import region.Placemark;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.instance.RemoveWithValues;
import weka.filters.unsupervised.instance.Resample;

public class WekaPreprocess {
	
	
	/*
	 * This program pre-process all the files.
	 * It removes instances with missing values, removes the mnt field and subsample 2-4% of the population.
	 */
	
	public static void main(String[] args) throws Exception {
		
		
		createARFF("file_pls_ve_","Venezia","July2013",null);
		createARFF("file_pls_ve_","Venezia","March2014",null);
		createARFF("file_pls_fi_","Firenze","July2013",null);
		createARFF("file_pls_fi_","Firenze","March2014",null);

		
		preprocess("Venezia","July2013");
		preprocess("Venezia","March2014");
		preprocess("Firenze","July2013");
		preprocess("Firenze","March2014");
		
		System.out.println("Done");
	}
	
	
	public static void createARFF(String pre, String city, String month, String regionSerFile) throws Exception {
		if(month.equals("July2013")) {
			Config.getInstance().pls_start_time = new GregorianCalendar(2013,Calendar.JULY,1,0,0,0);
			Config.getInstance().pls_end_time = new GregorianCalendar(2013,Calendar.JULY,31,23,59,59);
		}
		else if(month.equals("March2014")) {
			Config.getInstance().pls_start_time = new GregorianCalendar(2014,Calendar.MARCH,1,0,0,0);
			Config.getInstance().pls_end_time = new GregorianCalendar(2014,Calendar.MARCH,31,23,59,59);
		}
		else {
			System.out.println("Check Month");
			System.exit(0);
		}
		String cellXHourFile =Config.getInstance().base_folder+"/UserEventCounter/"+pre+city+"_cellXHour_"+month+".csv";
		String gt_ser_file = Config.getInstance().base_folder+"/Tourist/"+city+"_gt_profiles_"+month+".ser";
		String weka_file = Config.getInstance().base_folder+"/Tourist/"+city+"_"+month+".arff";
		Placemark placemark = Placemark.getPlacemark(city);
		RegionMap rm = regionSerFile == null ? null : (RegionMap)CopyAndSerializationUtils.restore(new File(regionSerFile));
		PLSSpaceDensity.process(rm,cellXHourFile,gt_ser_file,null,weka_file,placemark);
	}
	
	public static void preprocess(String city, String month) throws Exception {
		double resample = city.equals("Venezia") ? 4 : 2;
		DataSource source = new DataSource(Config.getInstance().base_folder+"/Tourist/"+city+"_"+month+".arff");
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
		rem.setAttributeIndices("1,2");
		rem.setInputFormat(data);
		data = Filter.useFilter(data, rem);
		
		ArffSaver saver = new ArffSaver();
		saver.setInstances(data);
		saver.setFile(new File(Config.getInstance().base_folder+"/Tourist/resample/"+city+"_"+month+"_resampled.arff"));
		saver.writeBatch();
	}
	
}
