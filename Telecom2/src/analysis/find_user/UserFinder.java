package analysis.find_user;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.gps.utils.LatLonPoint;
import org.gps.utils.LatLonUtils;

import pls_parser.AnalyzePLSCoverageTime;
import pls_parser.PLSParser;
import pls_parser.UsersAroundAnEvent;
import utils.Config;
import utils.FileUtils;
import utils.Logger;
import utils.Sort;
import analysis.EventFilesFinder;
import area.Placemark;

public class UserFinder {
	
	public static int MAX_RESULT = 10;
	
	public String find(String q) {
		
		Map<String,Integer> map = new HashMap<String,Integer>();
		
		Map<String,Map<String,String>> mapt = new AnalyzePLSCoverageTime().computeAll();
		List<Sightseeing> ls = parseInput(q);
		for(Sightseeing s: ls) {
			Config.getInstance().pls_folder = FileUtils.getFile("DATASET/PLS/file_pls/"+s.dir).toString(); // set right working directory
			addSightseeings(map,s);
			
			if(s.weekly_repeat == true)  {
				Calendar start = null;
				Calendar end = null;
				SimpleDateFormat f = new SimpleDateFormat("yyyy/MMM/dd",Locale.US);
				for(String day: mapt.get(s.dir).keySet()) {
					try {
						Calendar cal = Calendar.getInstance();
						cal.setTime(f.parse(day));
						if(start == null || start.after(cal)) start = cal;
						if(end == null || end.before(cal)) end = cal;
					}catch(Exception e) {
						e.printStackTrace();
					}
				}
				
				Sightseeing s1 = new Sightseeing(s.spot,(Calendar)s.st.clone(),(Calendar)s.et.clone(),false);
				while(s1.et.before(end)) {
					s1.st.add(Calendar.DAY_OF_MONTH, 7);
					s1.et.add(Calendar.DAY_OF_MONTH, 7);
					addSightseeings(map,s1);
				}
				s1 = new Sightseeing(s.spot,(Calendar)s.st.clone(),(Calendar)s.et.clone(),false);
				while(s1.st.after(start)) {
					s1.st.add(Calendar.DAY_OF_MONTH, -7);
					s1.et.add(Calendar.DAY_OF_MONTH, -7);
					addSightseeings(map,s1);
				}
			}
		}
		
		
		map = Sort.sortHashMapByValuesD(map, Collections.reverseOrder());
		
		StringBuffer sb = new StringBuffer();
		int cont = 0;
		for(String u: map.keySet()) {
			sb.append(u+" --> "+map.get(u)+"<br>\n");
			cont++;
			if(cont > MAX_RESULT) break;
		}
		
		return sb.toString();
	}
	
	
	private void addSightseeings(Map<String,Integer> map, Sightseeing s) {
		System.out.println("Processing "+s);
		UsersAroundAnEvent ba = new UsersAroundAnEvent(s);
		
		try {
			PLSParser.parse(ba);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Set<String> users = ba.getUsers();
		for(String u: users) {
			Integer c = map.get(u);
			if(c == null) c = 0;
			map.put(u, c+1);
		}
	}
	
	private List<Sightseeing> parseInput(String q) {
		EventFilesFinder eff = new EventFilesFinder();
		List<Sightseeing> ls = new ArrayList<Sightseeing>();
		String[] e = q.split(";");
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd-HH:mm",Locale.US);
		for(int i=0; i<e.length;i=i+6) {
			Calendar st = Calendar.getInstance();
			Calendar et = Calendar.getInstance();
			try {
				st.setTime(f.parse(e[i]+"-"+e[i+1]));
				et.setTime(f.parse(e[i+2]+"-"+e[i+3]));			
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			boolean weekly_repeat = e[i+4].equals("true") ? true : false;
			String[] coord = e[i+5].replaceAll("\\(|\\)", "").split(",");
			double lat1 = Double.parseDouble(coord[0].trim());
			double lon1 = Double.parseDouble(coord[1].trim());
			double lat2 = Double.parseDouble(coord[2].trim());
			double lon2 = Double.parseDouble(coord[3].trim());
			double r = LatLonUtils.getHaversineDistance(new LatLonPoint(lat1,lon1), new LatLonPoint(lat2,lon2));
			Placemark p = new Placemark("","",new double[]{(lat1+lat2)/2,(lon1+lon2)/2},r);
			Sightseeing s = new Sightseeing(p, st, et, weekly_repeat);
			
			String dir = eff.find(e[i],e[i+1].split(":")[0],e[i+2],e[i+3].split(":")[0],lon1,lat1,lon2,lat2);
			if(dir == null) {
				Logger.logln(s+" not covered by data");
				continue;
			}
			
			s.dir = dir;
			ls.add(s);
		}
		return ls;
	}
	
	
	public static void main(String[] args) {
		UserFinder uf = new UserFinder();
		String q = "2014-03-04;9:14;2014-03-04;9:15;false;((45.44161099742083, 12.329521196126962), (45.442161915033715, 12.330405397176719));";
		System.out.println(uf.find(q));
	}
}



