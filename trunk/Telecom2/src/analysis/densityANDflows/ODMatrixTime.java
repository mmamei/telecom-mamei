package analysis.densityANDflows;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import utils.Config;
import utils.Logger;
import area.CityEvent;
import area.Placemark;

public class ODMatrixTime {
	public static void main(String[] args) throws Exception {
		
		Placemark p = new Placemark("Torino",new double[]{45.073036,7.679733},5000);
		CityEvent ce = new CityEvent(p,"11/03/2012 17:00","11/03/2012 19:00",-1);
		
		BufferedReader br = new BufferedReader(new FileReader(new File(Config.getInstance().base_dir+"/LocationsXUserAroundAnEvent/"+ce.toFileName())));
		String line;
		while((line=br.readLine())!=null) {
			System.out.println(line);
		}
		br.close();
		Logger.logln("Done");
	}
}
