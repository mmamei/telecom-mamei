package analysis.find_user;

import java.text.SimpleDateFormat;

import utils.Config;
import visual.kml.KMLPath;
import analysis.UserTrace;
import dataset.DataFactory;
import dataset.EventFilesFinderI;
import dataset.file.UsersCSVCreator;

public class UserPlotter {

	
	private static final SimpleDateFormat F = new SimpleDateFormat("yyyy-MM-dd-hh");
	public void plot(String sday, String eday, String user, double lon1, double lat1, double lon2, double lat2) {
		try {
			
			EventFilesFinderI eff = DataFactory.getEventFilesFinder();
			String dir = eff.find(sday,"12",eday,"12",lon1,lat1,lon2,lat2);
			if(dir == null) return;
			
			
			
			Config.getInstance().pls_folder = Config.getInstance().pls_root_folder+"/"+dir; 
			Config.getInstance().pls_start_time.setTime(F.parse(sday+"-0"));
			Config.getInstance().pls_end_time.setTime(F.parse(eday+"-23"));
						
			UserTrace trace = UsersCSVCreator.process(user); 
		
			KMLPath.openFile(Config.getInstance().web_kml_folder+"/"+user+".kml");
			KMLPath.print(user,trace.getEvents());
			KMLPath.closeFile();		
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		UserPlotter pu = new UserPlotter();
		//pu.plot("2014-03-04", "2014-03-04", "feaf164623aa5fcac0512b3b4a62496c34458ac017141a808dfe306b62759f", 12.329521196126962, 45.44161099742083,  12.330405397176719, 45.442161915033715);
		//pu.plot("2014-03-22", "2014-03-30", "95415e15efb61dd6389c7f904bc44ec7c716201d14518335616ea77c379a343", 7.676249,45.073963,7.676249,45.073963);
		pu.plot("2014-03-10", "2014-03-30", "95415e15efb61dd6389c7f904bc44ec7c716201d14518335616ea77c379a343", 12.33969676659992,45.43367746627466,12.33969676659992,45.43367746627466);
		//pu.plot("2014-02-04", "2014-02-08", "95415e15efb61dd6389c7f904bc44ec7c716201d14518335616ea77c379a343", 9.187377489875912,45.46445360087525,9.187377489875912,45.46445360087525);
		
		
		
		System.out.println("Done");
	} 
	
}
