package dataset;

import java.util.Calendar;

public interface EventFilesFinderI {
	public String find(String sday, String shour, String eday, String ehour, double lon1, double lat1, double lon2, double lat2);
	public String find(Calendar cs, Calendar ce, double lon1, double lat1, double lon2, double lat2);
}
