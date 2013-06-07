package utils;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Test {
	public static void main(String[] args) {
		Calendar cal = new GregorianCalendar();
		cal.set(Calendar.YEAR, 2012);
		cal.set(Calendar.MONTH, 2);
		cal.set(Calendar.DAY_OF_MONTH, 24);
		for(int i=0;i<100;i++) {
			System.out.println(cal.getTime());
			cal.add(Calendar.HOUR_OF_DAY, 1);
		}
	}
}
