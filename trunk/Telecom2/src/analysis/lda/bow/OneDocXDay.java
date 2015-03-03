package analysis.lda.bow;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import visual.kml.KMLArrowCurved;


public class OneDocXDay extends Bow {
	
	OneDocXDay() {	
	}
	
	@Override
	public  Map<String,List<String>> process(List<TimePlace> tps) {
		Map<String,List<String>> dailyPatterns = new TreeMap<String,List<String>>();
		for(TimePlace tp : tps) {
			String key = tp.dow+"-"+tp.day;
			
			List<String> d = dailyPatterns.get(key);
			if(d == null) {
				d = new ArrayList<String>();
				dailyPatterns.put(key, d);
			}
			String w  = tp.h+"-"+tp.getGeo();
			d.add(w);
		}
		return dailyPatterns;
	}
	
	@Override
	// This method parses a topic string, and returns a list of words and associated probabilities
	public List<Entry<String,Double>> parsePWZ(String topic){
		String[] elements = topic.split(",|-");
		if(elements.length < 4) return null;
		
		// Topic_0,19-7.6323,45.0514,0.63,15-7.6323,45.0514,0.29,22-7.6596,45.0518,0.04,8-7.6776,45.1035,0.02,8-7.68,45.0497,0.02
		List<Entry<String,Double>> list = new ArrayList<Entry<String,Double>>();
		for(int i=1;i<elements.length;i=i+4) {
			String h = elements[i];
			double lon=Double.NaN,lat=Double.NaN;
			try {
				lon = Double.parseDouble(elements[i+1]);
				lat = Double.parseDouble(elements[i+2]);
			} catch(Exception e) {
				continue;
			}
			double prob = Double.parseDouble(elements[i+3]);
			list.add(new SimpleEntry<String,Double>(h+","+lon+","+lat,prob));
		}
		return list;		
	}
	
	@Override
	// This method gives a kml representation of a word/probability
	public String word2KML(Map.Entry<String,Double> wp) {
		String[] e = wp.getKey().split(",");
		String h = e[0];
		double lon = Double.parseDouble(e[1]);
		double lat = Double.parseDouble(e[2]);
		double prob = wp.getValue();
		
		 StringBuffer sb = new StringBuffer();
		 
		 int sh1 = Integer.parseInt(h);
		 String color1 = "ff00ffff";
		 if(sh1>=13) {
			 sh1 = sh1 - 12;
			 color1 = "ffffaa55";
		 }
		 
		
		 sb.append("<Style id=\""+h+prob+"\">\n");
		 sb.append("<IconStyle>\n");
		 sb.append("<color>"+color1+"</color>\n");
		 sb.append("<scale>"+1+"</scale>\n");
		 sb.append("<Icon><href>http://maps.google.com/mapfiles/kml/paddle/"+sh1+".png</href></Icon>\n");
		 sb.append("</IconStyle>\n");
		 sb.append("</Style>\n");
		 
		
		 
		 double jitter = 0.001 - 0.0005 * Math.random();
		 
		 lon = lon + jitter;
		 lat = lat + jitter;
		 
		 
		 sb.append("<Placemark>\n");
		 sb.append("<styleUrl>#"+h+prob+"</styleUrl>\n");
		 sb.append("<Point>\n");
		 sb.append("<coordinates>"+lon+","+lat+",0</coordinates>\n");
		 sb.append("</Point>\n");
		 sb.append("</Placemark>\n");
		 
		

		 return sb.toString();
	}
	
}
