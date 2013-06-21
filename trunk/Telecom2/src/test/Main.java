package test;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import area.CityEvent;

public class Main {
	
	private static final SimpleDateFormat F = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	
	public static void main(String[] args) throws Exception  {
		
		CityEvent event = new CityEvent(null,"9/10/2010 12:00","11/10/2010 15:00",3);
		CityEvent exclude = new CityEvent(null,"10/10/2010 12:00","10/10/2010 15:00",3);
		CityEvent fl = new CityEvent(null,"9/10/2010 14:00","11/10/2010 10:00",3);
		Calendar first = fl.st;
		Calendar last = fl.et;

		double ev_s = event.st.getTimeInMillis(); // event start
		double ev_e = event.et.getTimeInMillis(); // event end
		double ex_s = exclude.st.getTimeInMillis(); // exclude start
		double ex_e = exclude.et.getTimeInMillis(); // exclude end
		double f = first.getTimeInMillis(); // first
		double l = last.getTimeInMillis(); // last
		double ev_lenght = ev_e - ev_s;
		double ex_lenght = ex_e - ex_s;
		double ot_lenght = l - f;
		double max = ev_lenght - ex_lenght;
		
		
		if(l < ex_s || f > ex_e) System.out.println(ot_lenght / max);	
		else System.out.println((ot_lenght - ex_lenght) / max);
		
	}
}
