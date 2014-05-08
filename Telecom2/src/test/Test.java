package test;

import static org.junit.Assert.*;
import analysis.presence_at_event.RunAll;
import region.Placemark;


// http://www.vogella.com/tutorials/JUnit/article.html

public class Test {

	@org.junit.Test
	public void test1() {
		Placemark p = Placemark.getPlacemark("Venezia");
		assertEquals("The number of cells in Venice placemark should be 216", 216, (int)p.getNumCells());
	}
	
	@org.junit.Test
	public void test2() {
		RunAll ra = new RunAll();
		//int[] rad_att = ra.radiusAndAttendance("2014-03-02","19","2014-03-03","0",11.28265300110946,43.78066799975202); // partita Fiorentina - Lazio. capienza stadio 47000
		//int[] rad_att = ra.radiusAndAttendance("2012-04-01","13","2012-04-01","18",9.123845,45.478068); // San Siro
		int[] rad_att = ra.radiusAndAttendance("2012-03-20","19","2012-03-20","23",7.641453,45.109536); // Juventus Stadium		
		assertEquals("The radius for the event considered should be 500. It results "+rad_att[0], 500, rad_att[0]);
		assertEquals("The number of attendees should be 41395. It results "+rad_att[1], 41395, rad_att[1]);
	}


}
