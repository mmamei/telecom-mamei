package analysis.clustering;

import java.util.List;
import java.util.Map;

import analysis.PlsEvent;
import analysis.place_recognizer.Cluster;


public abstract class Clusterer {
	public abstract Map<Integer, Cluster> buildCluster(List<PlsEvent> events);
}
