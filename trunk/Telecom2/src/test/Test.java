package test;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gps.utils.LatLonPoint;

import region.Placemark;
import analysis.PLSTimeDensity;
import analysis.presence_at_event.RunAll;
import analysis.user_place_recognizer.PlaceRecognizer;


// http://www.vogella.com/tutorials/JUnit/article.html

public class Test {

	@org.junit.Test
	public void testPlacemark() {
		Placemark p = Placemark.getPlacemark("Venezia");
		assertEquals("The number of cells in Venice placemark should be 189", 189, (int)p.getNumCells()); // was 213
	}
	
	@org.junit.Test
	public void testAttendance() {
		RunAll ra = new RunAll();
		//int[] rad_att = ra.radiusAndAttendance("2014-03-02","19","2014-03-03","0",11.28265300110946,43.78066799975202); // partita Fiorentina - Lazio. capienza stadio 47000
		//int[] rad_att = ra.radiusAndAttendance("2012-04-01","13","2012-04-01","18",9.123845,45.478068); // San Siro
		int[] rad_att = ra.radiusAndAttendance("2012-03-20","19","2012-03-20","23",7.641453,45.109536,null); // Juventus Stadium		
		assertEquals("The radius for the event considered should be 500. It results "+rad_att[0], 500, rad_att[0]);
		assertEquals("The number of attendees should be 39611. It results "+rad_att[1], 39611, rad_att[1]); // was 41395
	}
	
	@org.junit.Test
	public void testPLSTimeDensity() {
		PLSTimeDensity pbia = new PLSTimeDensity();
		Object[] plsdata = pbia.process("2014-03-10","18","2014-03-11","1",11.2523,43.7687,11.2545,43.7672,"");
		String js = pbia.getJSMap(plsdata);
		String res = "var data = google.visualization.arrayToDataTable([['Day', 'PLS'],['10-Mon:17',  733.0],['10-Mon:18',  3965.0],['10-Mon:19',  3592.0],['10-Mon:20',  2965.0],['10-Mon:21',  2488.0],['10-Mon:22',  2227.0],['10-Mon:23',  1874.0],['11-Tue:0',  1553.0],['11-Tue:1',  1315.0]]);";
		System.out.println(res);
		System.out.println("--------------------");
		System.out.println(js);
		assertEquals("Test the JS result of PLSTimeDensity", res, js);
	}
	
	
	@org.junit.Test
	public void testPlaceRecognizer() {
		
		Map<String, List<LatLonPoint>> gt = new HashMap<String, List<LatLonPoint>>();
		gt.put("HOME", new ArrayList<LatLonPoint>());
		gt.put("SUNDAY", new ArrayList<LatLonPoint>());
		gt.put("SATURDAY_NIGHT", new ArrayList<LatLonPoint>());
		gt.put("WORK", new ArrayList<LatLonPoint>());
		gt.get("HOME").add(new LatLonPoint(45.0695846,7.6899746));
		gt.get("WORK").add(new LatLonPoint(45.11202361806452,7.669145007741936));
		
		
		PlaceRecognizer pr = new PlaceRecognizer();
		Map<String, List<LatLonPoint>> res = pr.runSingle("2012-03-06", "2012-03-07", "362f6cf6e8cfba0e09b922e21d59563d26ae0207744af2de3766c5019415af", 7.6855,45.0713,  7.6855,45.0713);
		
		assertEquals("Test the result has the correct number of entries", gt.size(),res.size());
		for(String k: gt.keySet()) {
			List<LatLonPoint> lgt = gt.get(k);
			List<LatLonPoint> lrs = res.get(k);
			assertEquals("Test the result for "+k+" has the correct number of points", lgt.size(),lrs.size());
			for(int i=0; i<lgt.size();i++) {
				assertEquals("Test the "+i+" latitude", lgt.get(i).getLatitude(),lrs.get(i).getLatitude(),0.01);
				assertEquals("Test the "+i+" longitute", lgt.get(i).getLongitude(),lrs.get(i).getLongitude(),0.01);
			}
		}
	}
}
