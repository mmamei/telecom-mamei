package analysis.find_user;

import java.util.Calendar;

import area.region.CityEvent;
import area.region.Placemark;

public class Sightseeing extends CityEvent {
	
	String dir; // directory where to find the associated pls data
	boolean weekly_repeat;
	
	Sightseeing( Placemark p, Calendar st, Calendar et, boolean weekly_repeat) {
		super(p,st,et,0);
		this.weekly_repeat = weekly_repeat;
	}
}
