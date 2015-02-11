package analysis.lda.bow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import visual.kml.KMLArrowCurved;
import analysis.lda.CreateBagOfWords;
import analysis.lda.CreateTopicModel;


public class OneDocXDayMov extends Bow {
	
	
	public static final int MIN_DIST_FOR_LONG_TRIP = 1; // min distance to be considerd long trip
	public static final int MIN_LONG_TRIPS = 1;  
	
	OneDocXDayMov() {
		
	}
	
	public  Map<String,List<String>> process(List<TimePlace> tps) {
		Map<String,List<String>> dailyPatterns = new TreeMap<String,List<String>>();
		TimePlace last = tps.get(0);
		
		int long_trip = 0;
		
		for(int i=1; i<tps.size();i++) {
			TimePlace tp = tps.get(i);
			
			boolean tc = last.day.equals(tp.day);
			boolean sc = tp.tdist(last) > 1 || tp.sdist(last) >= MIN_DIST_FOR_LONG_TRIP;
			
			if(tp.sdist(last) >= MIN_DIST_FOR_LONG_TRIP)
				long_trip ++;
			
			if(tc && sc) {
				
				String key = tp.dow+"-"+tp.day;
				
				List<String> d = dailyPatterns.get(key);
				if(d == null) {
					d = new ArrayList<String>();
					dailyPatterns.put(key, d);
				}
				String w = last.h+","+last.getGeo()+"-"+tp.h+","+tp.getGeo();
				d.add(w);
				last = tp;
			}
			if(!tc) last = tp;		
		}
		
		if(long_trip < MIN_LONG_TRIPS) return null;
		
		return dailyPatterns;
	}	
	
	// This method parses a topic string, and returns a list of words and associated probabilities
		public List<Map.Entry<String,Double>> parsePWZ(String topic){
			
			String[] e = topic.split(",|-");
			if(e.length < 7) return null;
			
			// Topic_0,19-7.6323,45.0514,0.63,15-7.6323,45.0514,0.29,22-7.6596,45.0518,0.04,8-7.6776,45.1035,0.02,8-7.68,45.0497,0.02
			List<Entry<String,Double>> list = new ArrayList<Entry<String,Double>>();
			for(int i=1;i<e.length;i=i+7) {
				 String h1 = e[i];
				 String h2 = e[i+3];
				 
				 double lon1=Double.NaN,lat1=Double.NaN,lon2=Double.NaN,lat2=Double.NaN;
				 try {
					 lon1 = Double.parseDouble(e[i+1]);
					 lat1 = Double.parseDouble(e[i+2]);
					 lon2 = Double.parseDouble(e[i+4]);
					 lat2 = Double.parseDouble(e[i+5]);
				 }catch(Exception exc) {
					 continue;
				 }
				 double prob = Double.parseDouble(e[i+6]);
				
				
				list.add(new SimpleEntry<String,Double>(h1+","+lon1+","+lat1+","+h2+","+lon2+","+lat2,prob));
			}
			return list;		
	}
		
	// This method gives a kml representation of a word/probability
		public String word2KML(Map.Entry<String,Double> wp) {
			
			String[] e = wp.getKey().split(",");
			String h1 = e[0];
			double lon1 = Double.parseDouble(e[1]);
			double lat1 = Double.parseDouble(e[2]);
			String h2 = e[3];
			double lon2 = Double.parseDouble(e[4]);
			double lat2 = Double.parseDouble(e[5]);
			double prob = wp.getValue();
			
			 StringBuffer sb = new StringBuffer();
			 
			 int sh1 = Integer.parseInt(h1);
			 String color1 = "ff00ffff";
			 if(sh1>=13) {
				 sh1 = sh1 - 12;
				 color1 = "ffffaa55";
			 }
			 
			 int sh2 = Integer.parseInt(h2);
			 String color2 = "ff00ffff";
			 if(sh2>=13) {
				 sh2 = sh2 - 12;
				 color2 = "ffffaa55";
			 }
			
			 sb.append("<Style id=\""+h1+prob+"\">\n");
			 sb.append("<IconStyle>\n");
			 sb.append("<color>"+color1+"</color>\n");
			 sb.append("<scale>"+1+"</scale>\n");
			 sb.append("<Icon><href>http://maps.google.com/mapfiles/kml/paddle/"+sh1+".png</href></Icon>\n");
			 sb.append("</IconStyle>\n");
			 sb.append("</Style>\n");
			 
			 sb.append("<Style id=\""+h2+prob+"\">\n");
			 sb.append("<IconStyle>\n");
			 sb.append("<color>"+color2+"</color>\n");
			 sb.append("<scale>"+1+"</scale>\n");
			 sb.append("<Icon><href>http://maps.google.com/mapfiles/kml/paddle/"+sh2+".png</href></Icon>\n");
			 sb.append("</IconStyle>\n");
			 sb.append("</Style>\n");
			 
			 double jitter = 0.001 - 0.0005 * Math.random();
			 
			 lon1 = lon1 + jitter;
			 lat1 = lat1 + jitter;
			 lon2 = lon2 + jitter;
			 lat2 = lat2 + jitter;
			 
			 sb.append("<Placemark>\n");
			 sb.append("<styleUrl>#"+h1+prob+"</styleUrl>\n");
			 sb.append("<Point>\n");
			 sb.append("<coordinates>"+lon1+","+lat1+",0</coordinates>\n");
			 sb.append("</Point>\n");
			 sb.append("</Placemark>\n");
			 
			 
			 sb.append("<Placemark>\n");
			 sb.append("<styleUrl>#"+h2+prob+"</styleUrl>\n");
			 sb.append("<Point>\n");
			 sb.append("<coordinates>"+lon2+","+lat2+",0</coordinates>\n");
			 sb.append("</Point>\n");
			 sb.append("</Placemark>\n");
			 
			 sb.append(KMLArrowCurved.printArrow(lon1, lat1, lon2, lat2, 6 , "ff000000", true));
			 
			 
			 return sb.toString();
	}
}
