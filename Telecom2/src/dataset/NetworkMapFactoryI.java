package dataset;

import java.util.Calendar;

import region.RegionMap;

public interface NetworkMapFactoryI {
	public RegionMap getNetworkMap(Calendar calendar);
	public RegionMap getNetworkMap(long time);
	public Calendar getCalendar(String f);
}
