package test;

import static org.junit.Assert.*;
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
		Placemark p = Placemark.getPlacemark("Venezia");
		assertEquals("The number of cells in Venice placemark should be 216", 216, (int)p.getNumCells());
	}


}
