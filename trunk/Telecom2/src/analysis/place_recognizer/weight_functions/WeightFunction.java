package analysis.place_recognizer.weight_functions;

import analysis.place_recognizer.Cluster;

public interface WeightFunction {
	public void weight(Cluster c);
}
