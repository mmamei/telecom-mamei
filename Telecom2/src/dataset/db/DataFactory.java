package dataset.db;

import dataset.EventFilesFinderI;
import dataset.NetworkMapFactoryI;
import dataset.PLSCoverageSpaceI;
import dataset.PLSCoverageTimeI;
import dataset.PLSEventsAroundAPlacemarkI;
import dataset.UsersAroundAnEventI;

public class DataFactory {
	
	public static PLSEventsAroundAPlacemarkI getPLSEventsAroundAPlacemark() {
		return new PLSEventsAroundAPlacemark();
	}
	
	public static PLSCoverageSpaceI getPLSCoverageSpace() {
		return new PLSCoverageSpace();
	}
	
	public static PLSCoverageTimeI getPLSCoverageTime() {
		return new PLSCoverageTime();
	}
	
	public static EventFilesFinderI getEventFilesFinder() {
		return new EventFilesFinder();
	}
	
	public static NetworkMapFactoryI getNetworkMapFactory() {
		return NetworkMapFactory.getInstance();
	}
	
	public static UsersAroundAnEventI getUsersAroundAnEvent() {
		return new UsersAroundAnEvent();
	}

	
}
