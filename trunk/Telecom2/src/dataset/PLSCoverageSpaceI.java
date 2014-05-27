package dataset;

import java.util.Map;

import region.RegionMap;

public interface PLSCoverageSpaceI {
	public Map<String,RegionMap> getPlsCoverage();
	public String getJSMap(Map<String,RegionMap> map);
	public String getJSMapCenterLatLng(Map<String,RegionMap> map);
}
