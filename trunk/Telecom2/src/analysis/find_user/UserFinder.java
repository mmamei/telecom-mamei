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

import dataset.db.query.AnalyzePLSCoverageTime;
import dataset.file.PLSParser;
import dataset.file.UsersAroundAnEvent;
import region.Placemark;
import utils.Config;
import utils.FileUtils;
import utils.Logger;
import utils.Sort;
import analysis.EventFilesFinder;

public class UserFinder {
	
	public static int MAX_RESULT = 10;
	
	static String[] DOW = new String[]{"","Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
	
	public String find(String q) {
		
		Map<String,Integer> map = new HashMap<String,Integer>();
		
		Map<String,List<String>> mapt = new AnalyzePLSCoverageTime().computeAll();
		List<Sightseeing> ls = parseInput(q);
		for(Sightseeing s: ls) {
			Config.getInstance().pls_folder = FileUtils.getFile("DATASET/PLS/file_pls/"+s.dir).toString(); // set right working directory
			System.out.println(Config.getInstance().pls_folder);
			System.out.println("Processing "+s+ (s.weekly_repeat ? " REPEAT = "+DOW[s.st.get(Calendar.DAY_OF_WEEK)]:""));
			addSightseeings(map,s);
			if(s.weekly_repeat == true)  {
				Calendar start = null;
				Calendar end = null;
				SimpleDateFormat f = new SimpleDateFormat("yyyy/MMM/dd",Locale.US);
				for(String day: mapt.get(s.dir)) {
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
		
		
		Set<String> users = UsersAroundAnEvent.process(s, false);
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
			Placemark p = new Placemark("",new double[]{(lat1+lat2)/2,(lon1+lon2)/2},r);
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
		
		//7f3e4f68105e863aa369e5c39ab5789975f0788386b45954829346b7ca63	Home,45.073963,7.676248999999999	Work,45.468133,7.872852
		String h = "((45.073963,7.676249), (45.073963,7.676249));";
		String w = "((45.468133,7.872852), (45.468133,7.872852));";
		String q = "2012-03-05;5:00;2012-03-05;7:00;true;"+h+
				   "2012-03-06;5:00;2012-03-06;7:00;true;"+h+
				   "2012-03-07;5:00;2012-03-07;7:00;true;"+h+
				   "2012-03-08;5:00;2012-03-08;7:00;true;"+h+
				   "2012-03-09;5:00;2012-03-09;7:00;true;"+h+
				   "2012-03-10;5:00;2012-03-10;7:00;true;"+h+
				   "2012-03-11;5:00;2012-03-11;7:00;true;"+h+
				   
				   "2012-03-05;9:00;2012-03-05;17:00;true;"+w;
				   //"2012-03-06;9:00;2012-03-06;17:00;true;"+w+
				   //"2012-03-07;9:00;2012-03-07;17:00;true;"+w+
				   //"2012-03-08;9:00;2012-03-08;17:00;true;"+w+
				   //"2012-03-09;9:00;2012-03-09;17:00;true;"+w;
				
		System.out.println(uf.find(q));
	}
}



