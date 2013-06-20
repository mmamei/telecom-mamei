package test;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Main {
	public static void main(String[] args) throws Exception  {
		Calendar c = new GregorianCalendar();
		Calendar c1 = (Calendar)c.clone();
		System.out.println(c.getTime());
		c1.set(Calendar.YEAR, 2000);
		System.out.println(c.getTime());
		System.out.println(c1.getTime());
	}
}
