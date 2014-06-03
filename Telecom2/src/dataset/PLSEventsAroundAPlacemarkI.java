package dataset;

import java.util.Map;

import region.Placemark;

public interface PLSEventsAroundAPlacemarkI {
	public void process(Placemark p, Map<String,Object> constraints) throws Exception;
	public String getFileSuffix(Map<String,Object> constraints);
}
