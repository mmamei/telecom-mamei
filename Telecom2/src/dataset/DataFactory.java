package dataset;



public class DataFactory {
	public static final int FILE = 0;
	public static final int DB = 1;
	public static int TYPE = FILE;
	
	public static PLSEventsAroundAPlacemarkI getPLSEventsAroundAPlacemark() {
		if(TYPE==FILE) return new dataset.file.PLSEventsAroundAPlacemark();
		else if(TYPE==DB) return new dataset.db.PLSEventsAroundAPlacemark();
		return null;
	}
	
	public static PLSCoverageSpaceI getPLSCoverageSpace() {
		if(TYPE==FILE) return new dataset.file.PLSCoverageSpace();
		else if(TYPE==DB) return new dataset.db.PLSCoverageSpace();
		return null;
	}
	
	public static PLSCoverageTimeI getPLSCoverageTime() {
		if(TYPE==FILE) return new dataset.file.PLSCoverageTime();
		else if(TYPE==DB) return new dataset.db.PLSCoverageTime();
		return null;
	}
}
