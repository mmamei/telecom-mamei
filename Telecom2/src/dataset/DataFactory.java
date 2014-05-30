package dataset;

import java.text.SimpleDateFormat;
import java.util.Locale;



public class DataFactory {
	public static final int FILE = 0;
	public static final int DB = 1;
	public static int TYPE = FILE;
	
	
	public static PLSEventsAroundAPlacemarkI getPLSEventsAroundAPlacemark() {
		if(TYPE==FILE) return dataset.file.DataFactory.getPLSEventsAroundAPlacemark();
		else if(TYPE==DB) return dataset.db.DataFactory.getPLSEventsAroundAPlacemark();
		return null;
	}
	
	public static PLSCoverageSpaceI getPLSCoverageSpace() {
		if(TYPE==FILE) return dataset.file.DataFactory.getPLSCoverageSpace();
		else if(TYPE==DB) return dataset.db.DataFactory.getPLSCoverageSpace();
		return null;
	}
	
	public static PLSCoverageTimeI getPLSCoverageTime() {
		if(TYPE==FILE) return dataset.file.DataFactory.getPLSCoverageTime();
		else if(TYPE==DB) return dataset.db.DataFactory.getPLSCoverageTime();
		return null;
	}
	
	public static EventFilesFinderI getEventFilesFinder() {
		if(TYPE==FILE) return dataset.file.DataFactory.getEventFilesFinder();
		else if(TYPE==DB) return dataset.db.DataFactory.getEventFilesFinder();
		return null;
	}
	
	public static NetworkMapFactoryI getNetworkMapFactory() {
		if(TYPE==FILE) return dataset.file.DataFactory.getNetworkMapFactory();
		else if(TYPE==DB) return dataset.db.DataFactory.getNetworkMapFactory();
		return null;
	}
	
	
	public static UsersAroundAnEventI getUsersAroundAnEvent() {
		if(TYPE==FILE) return dataset.file.DataFactory.getUsersAroundAnEvent();
		else if(TYPE==DB) return dataset.db.DataFactory.getUsersAroundAnEvent();
		return null;
	}
	
	public static SimpleDateFormat getSimpleDateFormat() {
		if(TYPE==FILE) return new SimpleDateFormat("yyyy/MMM/dd",Locale.US);
		else if(TYPE==DB) return new SimpleDateFormat("yyyyMMdd");
		return null;
	}
}
