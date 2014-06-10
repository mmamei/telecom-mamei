package dataset;

import region.Placemark;
import analysis.Constraints;

public interface PLSEventsAroundAPlacemarkI {
	public void process(Placemark p, Constraints constraints) throws Exception;
}
