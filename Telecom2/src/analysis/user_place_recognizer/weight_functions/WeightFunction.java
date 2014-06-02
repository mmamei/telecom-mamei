package analysis.user_place_recognizer.weight_functions;

import analysis.user_place_recognizer.Cluster;

public interface WeightFunction {
	public void weight(Cluster c);
}
