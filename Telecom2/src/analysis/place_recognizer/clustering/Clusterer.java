package analysis.place_recognizer.clustering;

import java.util.List;
import java.util.Map;

import analysis.PlsEvent;
import analysis.place_recognizer.Cluster;


public interface Clusterer {
	public Map<Integer, Cluster> buildCluster(List<PlsEvent> events);
}
