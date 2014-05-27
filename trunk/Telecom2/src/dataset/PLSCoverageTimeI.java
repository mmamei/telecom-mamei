package dataset;

import java.util.List;
import java.util.Map;

public interface PLSCoverageTimeI {
	public String getJSMap(Map<String,List<String>> all);
	public int getNumYears(List<String> dmap);
	public Map<String,List<String>> computeAll();
}
