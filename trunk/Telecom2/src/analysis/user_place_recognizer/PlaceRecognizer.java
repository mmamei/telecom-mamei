package analysis.user_place_recognizer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gps.utils.LatLonPoint;

import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.FilterAndCounterUtils;
import utils.Logger;
import analysis.PLSEvent;
import analysis.user_place_recognizer.clustering.AgglomerativeClusterer;
import analysis.user_place_recognizer.weight_functions.WeightFunction;
import analysis.user_place_recognizer.weight_functions.WeightOnDay;
import analysis.user_place_recognizer.weight_functions.WeightOnDiversity;
import analysis.user_place_recognizer.weight_functions.WeightOnTime;
import analysis.user_place_recognizer.weight_functions.Weights;
import dataset.DataFactory;
import dataset.EventFilesFinderI;
import dataset.file.UsersCSVCreator;

public class PlaceRecognizer {
	
	
	public static boolean SAVE_CLUSTERS = false;
	
	public static boolean VERBOSE = false;
	
	
	public static Object[] analyze(String username, String kind_of_place, List<PLSEvent> events, 
			                   double alpha, double beta, double delta, double rwf) {
		
		
		List<PLSEvent> workingset = CopyAndSerializationUtils.clone(events);
	
				
		Logger.logln("Processing "+(username.length() > 5 ? username.substring(0,5) : username)+" "+kind_of_place);
		
		double[][] weights = Weights.get(kind_of_place);
		
		List<PLSEvent> refEvents = Thresholding.buildReferenceTower(workingset,weights);
		workingset.addAll(refEvents);
		
		
		String tperiod = events.get(0).getTimeStamp()+"-"+events.get(events.size()-1).getTimeStamp();
		
		Map<Integer, Cluster> clusters = null;
		
		if(!SAVE_CLUSTERS)
			clusters = new AgglomerativeClusterer(3,weights,delta).buildCluster(workingset);
		else {
			File dir = new File(Config.getInstance().base_folder+"/PlaceRecognizer/Clusters");
			dir.mkdirs();
			File f = new File(dir+"/"+username+"-"+kind_of_place+"-"+tperiod+"-"+delta+".ser");
			if(f.exists()) 
				clusters = (Map<Integer, Cluster>)CopyAndSerializationUtils.restore(f);
			else {
				clusters = new AgglomerativeClusterer(3,weights,delta).buildCluster(workingset);
				CopyAndSerializationUtils.save(f, clusters);
			}
		}
		// rename the reference cluster to -1
		int found = -1;
		for(int k : clusters.keySet()) {
			Cluster c = clusters.get(k);
			if(c.getEvents().get(0).getCellac().equals(Thresholding.REF_NETWORK_CELLAC)) {
				found = k;
				break;
			}	
		}
		if(found != -1) clusters.put(-1, clusters.remove(found));
		
		/*
		System.out.println("Number of clusters --> "+clusters.size());
		for(int key:  clusters.keySet()) {
			System.out.println("  "+key+" --> "+clusters.get(key).size());
			for(PLSEvent pe :clusters.get(key).getEvents()) 
				System.out.print(pe.getCellac()+", ");
			System.out.println();
		}
		*/
		WeightFunction[] wfunctions = new WeightFunction[]{
				new WeightOnTime(1.0,weights),
				new WeightOnDay(alpha),
				new WeightOnDiversity(beta,weights)
		};
		
		
		for(Cluster c: clusters.values()) 
		for(WeightFunction wf : wfunctions) 
			wf.weight(c);
		
		
		double threshold = Double.MAX_VALUE;
		if(clusters.get(-1) != null) 
			threshold = Thresholding.weight2Threshold(kind_of_place, FilterAndCounterUtils.getNumDays(workingset), clusters.get(-1), rwf);
		
		
		List<LatLonPoint> placemarks = new ArrayList<LatLonPoint>();
		for(int k : clusters.keySet()) {
			if(k==-1) continue;
			Cluster c = clusters.get(k);
			if(c.totWeight() > threshold) {
							
				LatLonPoint p = c.getCenter(weights);
				if(p!=null) placemarks.add(p);
			}
		}		
		return new Object[]{clusters, placemarks};
	}
	
	
	
	private static final String[] KIND_OF_PLACES = new String[]{"HOME","WORK","SATURDAY_NIGHT","SUNDAY"};
	private static final SimpleDateFormat F = new SimpleDateFormat("yyyy-MM-dd-hh");
	public Map<String, List<LatLonPoint>> runSingle(String sday, String eday, String user, double lon1, double lat1, double lon2, double lat2) {
		
		System.out.println("****** PARAMS");
		System.out.println(sday+","+eday+","+user+","+lon1+","+lat1+","+lon2+","+lat2);
		System.out.println("****** PARAMS");
		
		Map<String, List<LatLonPoint>> results = null;
		try {
			EventFilesFinderI eff = DataFactory.getEventFilesFinder();
			String dir = eff.find(sday,"12",eday,"12",lon1,lat1,lon2,lat2);
			if(dir == null) return null;
			
			Config.getInstance().pls_folder = new File(Config.getInstance().pls_root_folder+"/"+dir).toString(); 
			Config.getInstance().pls_start_time.setTime(F.parse(sday+"-0"));
			Config.getInstance().pls_end_time.setTime(F.parse(eday+"-23"));
			List<PLSEvent> events = UsersCSVCreator.process(user).getEvents(); 
			results = new HashMap<String, List<LatLonPoint>>();
			PlaceRecognizerLogger.openKMLFile(Config.getInstance().web_kml_folder+"/"+user+".kml");
			for(String kind_of_place:KIND_OF_PLACES) {
				Object[] clusters_points = analyze(user,kind_of_place,events,0.25,0.25,2000,0.6);
				Map<Integer, Cluster> clusters = (Map<Integer, Cluster>)clusters_points[0];
				List<LatLonPoint> points = (List<LatLonPoint>)clusters_points[1];
				results.put(kind_of_place, points);
				
				if(VERBOSE) PlaceRecognizerLogger.log(user, kind_of_place, clusters);
				PlaceRecognizerLogger.logcsv(user,kind_of_place,points);
				PlaceRecognizerLogger.logkml(kind_of_place, clusters, points);
			}
			PlaceRecognizerLogger.closeKMLFile();
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		return results;
	}
	
	
	// USED IN BATCH RUN *****
	
	static Map<String, List<LatLonPoint>> allResults = new HashMap<String, List<LatLonPoint>>();
	
	public static synchronized void process(Map<String, Object[]> res) {
		
		String username = res.keySet().iterator().next().split("*")[0];
		if(KML_OUTPUT) PlaceRecognizerLogger.openUserFolderKML(username);
		for(String k: res.keySet()) {
			String[] user_kop = k.split("*");
			String user = user_kop[0];
			String kind_of_place = user_kop[1];
			Object[] clusters_points = res.get(k);
			Map<Integer, Cluster> clusters = (Map<Integer, Cluster>)clusters_points[0];
			List<LatLonPoint> points = (List<LatLonPoint>)clusters_points[1];
			allResults.put(k, points);
			if(VERBOSE) PlaceRecognizerLogger.log(user, kind_of_place, clusters);
			PlaceRecognizerLogger.logcsv(user,kind_of_place,points);
			if(KML_OUTPUT) PlaceRecognizerLogger.logkml(kind_of_place, clusters, points);
			
		}
		if(KML_OUTPUT) PlaceRecognizerLogger.closeUserFolderKML();
	}
	
	
	public String[] getComputedResults() {
		List<String> results = new ArrayList<String>();
		File dir = new File(Config.getInstance().base_folder+"/PlaceRecognizer");
		for(File subdir: dir.listFiles()) {
			File f = new File(subdir+"/results.csv");
			if(f.exists())
				results.add(f.getAbsolutePath());
		}
		return results.toArray(new String[results.size()]);
	}
	
	
	public static boolean KML_OUTPUT = false;
	
	public static void main(String[] args) throws Exception {
		
		/**************************************************************************************************************************/
		/**************************************   				 BATCH RUN 					***************************************/
		/**************************************************************************************************************************/
		
		//Config.getInstance().changeDataset("ivory-set3");
		//String dir = "file_pls_ivory_users_2000_10000";
		
		
		String dir = "file_pls_piem_users_200_10000";
		String in_dir = Config.getInstance().base_folder+"/UsersCSVCreator/"+dir;
		String out_dir = Config.getInstance().base_folder+"/PlaceRecognizer/"+dir;
		File d = new File(out_dir);
		if(!d.exists()) d.mkdirs();
		
		PlaceRecognizerLogger.openTotalCSVFile(out_dir+"/results.csv");
		if(KML_OUTPUT) PlaceRecognizerLogger.openKMLFile(out_dir+"/results.kml");
		File[] files = new File(in_dir).listFiles();
		
		
		int total_size = files.length;
		int n_thread = 8;
		int size = total_size / n_thread;
		Worker[] w = new Worker[n_thread];
		for(int t = 0; t < n_thread;t++) {
			int start = t*size;
			int end = t == (n_thread-1) ? total_size : (t+1)*size;
			w[t] = new Worker(KIND_OF_PLACES,files,start,end);		
		}
		
		for(int t = 0; t < n_thread;t++) 
			w[t].start();

		for(int t = 0; t < n_thread;t++) 
			w[t].join();
		
		System.out.println("All thread completed!");
		
		
		
		if(KML_OUTPUT) PlaceRecognizerLogger.closeKMLFile();
		PlaceRecognizerLogger.closeTotalCSVFile();
		
		//PlaceRecognizerEvaluator rs = new PlaceRecognizerEvaluator(2000);
		//rs.evaluate(allResults);
		
		
		/**************************************************************************************************************************/
		/**************************************   				SINGLE RUN 					***************************************/
		/**************************************************************************************************************************/
		
		/*
		PlaceRecognizer pr = new PlaceRecognizer();
		Map<String, List<LatLonPoint>> res = pr.runSingle("2012-03-06", "2012-03-07", "362f6cf6e8cfba0e09b922e21d59563d26ae0207744af2de3766c5019415af", 7.6855,45.0713,  7.6855,45.0713);
		//pr.runSingle("2012-03-06", "2012-04-30", "7f3e4f68105e863aa369e5c39ab5789975f0788386b45954829346b7ca63", 7.6855,45.0713,  7.6855,45.0713);
		for(String k: res.keySet()) {
			System.out.println(k);
			for(LatLonPoint p: res.get(k))
				System.out.println(p.getLongitude()+","+p.getLatitude());
		}
		Logger.logln("Done!");
		*/
	}
}


// USED IN BATCH RUN *****

class Worker extends Thread {
	String[] KIND_OF_PLACES;
	File[] files;
	int start;
	int end;
	Worker(String[] KIND_OF_PLACES,File[] files,int start,int end) {
		this.KIND_OF_PLACES = KIND_OF_PLACES;
		this.files = files;
		this.start = start;
		this.end = end;
	}
	public void run() {
		System.out.println("Thread "+start+"-"+end+" starting!");
		for(int i=start;i<end;i++) {
			try {
				File f = files[i];
				if(!f.isFile()) continue;
				String filename = f.getName();
				String username = filename.substring(0, filename.indexOf(".csv"));
				
				// if(!username.equals("699b8a4680a419d66791766828669f1a8cfedcd9087a050f994e18c0f2ad51")) continue;
				
				List<PLSEvent> events = PLSEvent.readEvents(f);			
				Map<String, Object[]> res = new HashMap<String, Object[]>();
				for(String kind_of_place:KIND_OF_PLACES) {
					
					// if(!kind_of_place.equals("HOME")) continue;
					
					res.put(username+"*"+kind_of_place, PlaceRecognizer.analyze(username,kind_of_place,events,0.25,0.25,2000,0.6));
				}
				PlaceRecognizer.process(res);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("Thread "+start+"-"+end+" completed!");
	}
}
