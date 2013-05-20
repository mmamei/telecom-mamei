package analysis.weighting;

import analysis.place_recognizer.Cluster;

public interface WeightFunction {
	public void weight(Cluster c);
}
