package visual.kml;

import java.util.Map;

import utils.Colors;

import network.NetworkCell;
import network.NetworkMap;
import network.NetworkMapFactory;




public class KMLHeatMap {
	
	public static String drawHeatMap(String name, Map<Long,Double> map, double max) {
		NetworkMap nm = NetworkMapFactory.getNetworkMap();
		StringBuffer result = new StringBuffer();
		for(long celllac: map.keySet()) {
			NetworkCell nc = nm.get(celllac);
			int index = (int)(map.get(celllac) / max * (Colors.HEAT_COLORS.length-1));
			
			result.append(nc.toKml(Colors.rgb2kmlstring(Colors.HEAT_COLORS[index]), "", ""));
		}
		return result.toString();
	}
	
	
}
