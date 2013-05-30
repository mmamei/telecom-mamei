package visual;

import java.util.Map;

import utils.Colors;

import network.NetworkCell;
import network.NetworkMap;




public class KMLHeatMap {
	
	public static String drawHeatMap(String name, Map<Long,Double> map, double max) {
		NetworkMap nm = NetworkMap.getInstance();
		StringBuffer result = new StringBuffer();
		for(long celllac: map.keySet()) {
			NetworkCell nc = nm.get(celllac);
			int index = (int)(map.get(celllac) / max * (Colors.HEAT_COLORS.length-1));
			
			result.append(nc.toKml(rgb2kmlstring(Colors.HEAT_COLORS[index]), "", ""));
		}
		return result.toString();
	}
	
	private static String rgb2kmlstring(int[] rgb) {
		String r = Integer.toHexString(rgb[0]);
		String g = Integer.toHexString(rgb[1]);
		String b = Integer.toHexString(rgb[2]);
		r = r.length() == 1 ? "0"+r : r;
		g = g.length() == 1 ? "0"+g : g;
		b = b.length() == 1 ? "0"+b : b;
		return b+g+r;
	}
}
