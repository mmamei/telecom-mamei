package analysis.place_recognizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gps.utils.LatLonPoint;

import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.FilterAndCounterUtils;
import utils.Logger;
import analysis.PlsEvent;
import analysis.clustering.AgglomerativeClusterer;
import analysis.weighting.WeightFunction;
import analysis.weighting.WeightOnDay;
import analysis.weighting.WeightOnDiversity;
import analysis.weighting.WeightOnTime;
import analysis.weighting.Weights;

public class PlaceRecognizer {
	
	
	public static List<LatLonPoint> analyze(String username, String kind_of_place, List<PlsEvent> events, 
			                   double alpha, double beta, double delta, double rwf) {
		
		
		List<PlsEvent> workingset = CopyAndSerializationUtils.clone(events);
				
				
		Logger.logln("Processing "+username.substring(0,5)+" "+kind_of_place);
		
		double[][] weights = Weights.get(kind_of_place);
		
		List<PlsEvent> refEvents = Thresholding.buildReferenceTower(workingset,weights);
		workingset.addAll(refEvents);
		
		
		String tperiod = events.get(0).getTimeStamp()+"-"+events.get(events.size()-1).getTimeStamp();
		
		Map<Integer, Cluster> clusters = null;
		File f = new File("BASE/cache/"+username+"-"+kind_of_place+"-"+tperiod+"-"+delta+".ser");
		if(f.exists()) 
			clusters = (Map<Integer, Cluster>)CopyAndSerializationUtils.restore(f);
		else {
			clusters = new AgglomerativeClusterer(3,weights,delta).buildCluster(workingset);
			CopyAndSerializationUtils.save(f, clusters);
		}
		
		// rename the reference cluster to -1
		int found = -1;
		for(int k : clusters.keySet()) {
			Cluster c = clusters.get(k);
			if(c.getEvents().get(0).getCellac() == Thresholding.REF_NETWORK_CELLAC) {
				found = k;
				break;
			}	
		}
		if(found != -1) clusters.put(-1, clusters.remove(found));
		
		
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
				placemarks.add(p);
			}
		}
		 
		if(VERBOSE) PlaceRecognizerLogger.log(username, kind_of_place, clusters);
		PlaceRecognizerLogger.logkml(kind_of_place, clusters, placemarks);
		PlaceRecognizerLogger.logcsv(username,kind_of_place,placemarks);
		
		return placemarks;
	}
	
	
	
	public static boolean VERBOSE = false;
	
	public static void main(String[] args) throws Exception {
		String dir = "file_pls_piem_users_above_2000";
		String in_dir = "BASE/UsersCSVCreator/"+dir;
		String out_dir = "BASE/PlaceRecognizer/"+dir;
		File d = new File(out_dir);
		if(!d.exists()) d.mkdirs();
		
		PlaceRecognizerLogger.openTotalCSVFile(out_dir+"/results.csv");
		PlaceRecognizerLogger.openKMLFile(out_dir+"/results.kml");
		File[] files = new File(in_dir).listFiles();
		
		Map<String, List<LatLonPoint>> allResults = new HashMap<String, List<LatLonPoint>>();
		
		String[] kind_of_places = new String[]{"HOME","WORK","SATURDAY_NIGHT","SUNDAY"};
		for(int i=0; i<files.length; i++){
			File f = files[i];	
			if(!f.isFile()) continue;
			String filename = f.getName();
			String username = filename.substring(0, filename.indexOf(".csv"));
			List<PlsEvent> events = PlsEvent.readEvents(f);
			PlaceRecognizerLogger.openUserFolderKML(username);
			
			for(String kind_of_place:kind_of_places)
				allResults.put(username+"_"+kind_of_place, analyze(username,kind_of_place,events,0.25,0.25,2000,0.6));
				
			PlaceRecognizerLogger.closeUserFolderKML();
		}
		PlaceRecognizerLogger.closeKMLFile();
		PlaceRecognizerLogger.closeTotalCSVFile();
		
		//PlaceRecognizerEvaluator rs = new PlaceRecognizerEvaluator(2000);
		//rs.evaluate(allResults);
		
		
		Logger.logln("Done!");
	}
	
	
	
	
}
