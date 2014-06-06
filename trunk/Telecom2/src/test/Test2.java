package test;

import static org.junit.Assert.assertEquals;

import java.io.File;

import region.RegionI;
import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;


// http://www.vogella.com/tutorials/JUnit/article.html

public class Test2 {

	@org.junit.Test
	public void testRegionMap() {
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/FIX_Piemonte.ser"));
		RegionI r = null; 
		
		r = rm.get(7.687631,45.070445);
		System.out.println(r.getName());
		assertEquals("Test Region Map FIX_Piemonte with a point in Torino", "TORINO", r.getName());	
		
		r = rm.get(7.950863499245511,45.31123601252711);
		System.out.println(r.getName());
		assertEquals("Test Region Map FIX_Piemonte with a point in Torino", "VILLAREGGIA", r.getName());	
		
		r = rm.get(7.276073651663653,44.88341453339817);
		System.out.println(r.getName());
		assertEquals("Test Region Map FIX_Piemonte with a point in Torino", "SAN SECONDO DI PINEROLO", r.getName());	
		
		r = rm.get(8.495767352632413,44.80357151257454);
		System.out.println(r.getName());
		assertEquals("Test Region Map FIX_Piemonte with a point in Torino", "BORGORATTO ALESSANDRINO", r.getName());	
	}
}
