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
		
		
				
		//Config.getInstance().pls_start_time = new GregorianCalendar(2014,Calendar.AUGUST,1,0,0,0);
		//Config.getInstance().pls_end_time = new GregorianCalendar(2014,Calendar.AUGUST,31,23,59,59);
		//runProcess("file_pls_pu_","Lecce","_Aug2014",null);
			
		//Config.getInstance().pls_start_time = new GregorianCalendar(2014,Calendar.SEPTEMBER,1,0,0,0);
		//Config.getInstance().pls_end_time = new GregorianCalendar(2014,Calendar.SEPTEMBER,31,23,59,59);
		//runProcess("file_pls_pu_","Lecce","_Sep2014",null);
		
		//Config.getInstance().pls_start_time = new GregorianCalendar(2014,Calendar.OCTOBER,1,0,0,0);
		//Config.getInstance().pls_end_time = new GregorianCalendar(2014,Calendar.OCTOBER,31,23,59,59);
		//runProcess("file_pls_piem_","Torino","_Oct2014",null);
		//runProcess("file_pls_piem_","Torino","_Oct2014",Config.getInstance().base_folder+"/RegionMap/TorinoArea.ser");
			
		//Config.getInstance().pls_start_time = new GregorianCalendar(2013,Calendar.JULY,1,0,0,0);
		//Config.getInstance().pls_end_time = new GregorianCalendar(2013,Calendar.JULY,31,23,59,59);
		//runProcess("file_pls_ve_","Venezia","_July2013",null);
		//runProcess("file_pls_fi_","Firenze","_July2013",null);
			
				
		Config.getInstance().pls_start_time = new GregorianCalendar(2014,Calendar.MARCH,1,0,0,0);
		Config.getInstance().pls_end_time = new GregorianCalendar(2014,Calendar.MARCH,31,23,59,59);
		runProcess("file_pls_ve_","Venezia","_March2014",null);
		runProcess("file_pls_fi_","Firenze","_March2014",null);
		
		runProcess("file_pls_ve_","Venezia","_March2014",Config.getInstance().base_folder+"/RegionMap/VeneziaRealCenter.ser");
		runProcess("file_pls_fi_","Firenze","_March2014",Config.getInstance().base_folder+"/RegionMap/FirenzeRealCenter.ser");
		
		
		System.out.println("Done");
	}
	
	public static void runProcess(String pre, String placemark, String post, String region) throws Exception {
		createARFF(pre,placemark,post,region);
		preprocess(placemark,post,region);
	}
	
	
	public static void createARFF(String pre, String city, String month, String regionSerFile) throws Exception {
		File f = regionSerFile == null ? null : new File(regionSerFile);
		//System.out.println(f.getName());
		String cellXHourFile =Config.getInstance().base_folder+"/UserEventCounter/"+pre+city+"_cellXHour"+month+".csv";
		String gt_ser_file = Config.getInstance().base_folder+"/Tourist/"+city+"_gt_profiles"+month+".ser";
		String weka_file = Config.getInstance().base_folder+"/Tourist/"+city+month+(regionSerFile==null ? "_noregion" : "_"+f.getName().substring(0,f.getName().lastIndexOf(".ser")))+".arff";
		Placemark placemark = Placemark.getPlacemark(city);
		RegionMap rm = regionSerFile == null ? null : (RegionMap)CopyAndSerializationUtils.restore(new File(regionSerFile));
		PLSSpaceDensity.process(rm,cellXHourFile,gt_ser_file,null,weka_file,placemark);
	}
	
	public static void preprocess(String city, String month, String regionSerFile) throws Exception {
		String inFile = Config.getInstance().base_folder+"/Tourist/"+city+month+(regionSerFile==null ? "_noregion" : "_"+regionSerFile.substring(regionSerFile.lastIndexOf("/")+1,regionSerFile.lastIndexOf(".ser")))+".arff";
		System.out.println(inFile);
		DataSource source = new DataSource(inFile);
		Instances data = source.getDataSet();
		double resample = data.numInstances() < 200000 ? 4 : 2;
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
		saver.setFile(new File(Config.getInstance().base_folder+"/Tourist/Resampled/"+city+month+(regionSerFile==null ? "_noregion" : "_"+regionSerFile.substring(regionSerFile.lastIndexOf("/")+1,regionSerFile.lastIndexOf(".ser")))+"_resampled.arff"));
		saver.writeBatch();
	}
	
}
