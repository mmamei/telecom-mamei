package analysis.find_user;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dataset.file.PLSParser;
import dataset.file.UsersCSVCreator;
import utils.Config;
import utils.FileUtils;
import visual.kml.KMLPath;
import analysis.EventFilesFinder;
import analysis.PLSEvent;
import analysis.UserTrace;

public class UserPlotter {

	
	private static final SimpleDateFormat F = new SimpleDateFormat("yyyy-MM-dd-hh");
	public void plot(String sday, String eday, String user, double lon1, double lat1, double lon2, double lat2) {
		try {
			
			EventFilesFinder eff = new EventFilesFinder();
			String dir = eff.find(sday,"12",eday,"12",lon1,lat1,lon2,lat2);
			if(dir == null) return;
			
			
			
			Config.getInstance().pls_folder = FileUtils.getFile("DATASET/PLS/file_pls/"+dir).toString(); 
			Config.getInstance().pls_start_time.setTime(F.parse(sday+"-0"));
			Config.getInstance().pls_end_time.setTime(F.parse(eday+"-23"));
						
			UserTrace trace = UsersCSVCreator.process(user); 
		
			KMLPath.openFile("G:/CODE/Telecom/web/kml/"+user+".kml");
			KMLPath.print(user,trace.getEvents());
			KMLPath.closeFile();		
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		UserPlotter pu = new UserPlotter();
		pu.plot("2014-03-04", "2014-03-04", "feaf164623aa5fcac0512b3b4a62496c34458ac017141a808dfe306b62759f", 12.329521196126962, 45.44161099742083,  12.330405397176719, 45.442161915033715);
		System.out.println("Done");
	} 
	
}
