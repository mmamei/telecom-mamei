package analysis.densityANDflows.density;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserPlaces {
	public String username;
	public Map<String,List<double[]>> places;
	
	UserPlaces(String username) {
		this.username = username;
		places = new HashMap<String,List<double[]>>();
	}
	
	void add(String kop, double lon, double lat) {
		List<double[]> p = places.get(kop);
		if(p==null) {
			p = new ArrayList<double[]>();
			places.put(kop, p);
		}
		p.add(new double[]{lon,lat});
	}
	
	
	public static Map<String,UserPlaces> readUserPlaces(String file) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		String[] elements;
		Map<String,UserPlaces> up = new HashMap<String,UserPlaces>();
		while((line = br.readLine())!=null) {
			elements = line.split(",");
			String username = elements[0];
			UserPlaces places = up.get(username);
			if(places==null) {
				places = new UserPlaces(username);
				up.put(username, places);
			}
			String kind = elements[1];
			for(int i=2;i<elements.length;i++){
				String c = elements[i];
				double lon = Double.parseDouble(c.substring(0,c.indexOf(" ")));
				double lat = Double.parseDouble(c.substring(c.indexOf(" ")+1));
				places.add(kind, lon, lat);
			} 
		}
		br.close();
		return up;
	}
}
