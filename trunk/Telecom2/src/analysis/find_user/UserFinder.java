package analysis.find_user;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.gps.utils.LatLonPoint;
import org.gps.utils.LatLonUtils;

import region.Placemark;
import utils.Config;
import utils.Logger;
import utils.Sort;
import dataset.DataFactory;
import dataset.EventFilesFinderI;

public class UserFinder {
	
	public static int RADIUS_EXPAND = 300;
	public static int MAX_RESULT = 10;
	
	static String[] DOW = new String[]{"","Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
	
	public String find(String q) {
		
		Map<String,Double> tot_map = new HashMap<String,Double>();
		
		
		Map<String,List<String>> mapt = DataFactory.getPLSCoverageTime().computeAll();
		List<Sightseeing> ls = parseInput(q);
		for(Sightseeing s: ls) {
			
			Map<String,Integer> map = new HashMap<String,Integer>();
			
			
			File file = new File(Config.getInstance().pls_root_folder+"/"+s.dir);
			if(file!=null) {
				Config.getInstance().pls_folder = file.toString(); // set right working directory
				//System.out.println(Config.getInstance().pls_folder);
			}
			System.out.println("Processing "+s+ (s.weekly_repeat ? " REPEAT = "+DOW[s.st.get(Calendar.DAY_OF_WEEK)]:""));
			addSightseeings(map,s);
			
			if(s.weekly_repeat == true)  {
				Calendar start = null;
				Calendar end = null;
				
				SimpleDateFormat f = DataFactory.getSimpleDateFormat();
						
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
				
				Calendar[] general_start_end = getGeneralStartEnd(q);
				//System.out.println("GENERAL TIME LIMITS = "+general_start_end[0].getTime()+" - "+general_start_end[1].getTime());
				if(general_start_end!=null) {
					if(start.before(general_start_end[0])) start = general_start_end[0];
					if(end.after(general_start_end[1])) end = general_start_end[1];
				}
				
				
				//System.out.println("GENERAL TIME LIMITS = "+start.getTime()+" - "+end.getTime());
				
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
			
			
			/*
			 * Ogni sightseeing contribuisce per Math.log10(1+map.get(u)) al computo totale.
			 * Il problema che si era verificato era questo:
			 * Ho due sightseeing con repeat.
			 * Un utente è spesso in un posto e mai nell'altro.
			 * Un utente è a volte in entrambi
			 * IO tenderei a favorire il secondo che è presnete in entrambe le osserivazioni.
			 * Mentre il conteggio potrebbe favoriree il primo.
			 * Il logatimo (funzione sub-lineare) fa si che visiet multiple allo stesso sightseegin pesino sempre meno 
			 */
			
			for(String u:map.keySet()) {
				Double prev_count = tot_map.get(u);
				double new_val = Math.min(0.5,Math.log10(1+map.get(u)));		
				
				
				if(u.equals("7ccd23c665344f7c82079a9017b21961de5afef05b5e4171db8b7c13bed2d8")) System.out.println("7ccd.. ==> "+s+" count = "+map.get(u)+" log = "+new_val);
				if(u.equals("7f3e4f68105e863aa369e5c39ab5789975f0788386b45954829346b7ca63")) System.out.println("7f3e.. ==> "+s+" count = "+map.get(u)+" log = "+new_val);
				
				tot_map.put(u, prev_count == null ? new_val : prev_count+new_val);
			}
			
		}
		
		
		tot_map = Sort.sortHashMapByValuesD(tot_map, Collections.reverseOrder());
		
		StringBuffer sb = new StringBuffer();
		int cont = 0;
		for(String u: tot_map.keySet()) {
			sb.append(u+" --> "+tot_map.get(u)+"<br>\n");
			cont++;
			if(cont > MAX_RESULT) break;
		}
		
		//String gt = "7f3e4f68105e863aa369e5c39ab5789975f0788386b45954829346b7ca63";
		//System.err.println(gt+" --> "+tot_map.get(gt)+"<br>\n");
		
		
		
		return sb.toString();
	}
	
	
	private void addSightseeings(Map<String,Integer> map, Sightseeing s) {
		Set<String> users = DataFactory.getUsersAroundAnEvent().process(s);
		for(String u: users) {
			Integer c = map.get(u);
			if(c == null) c = 0;
			map.put(u, c+1);
		}
	}
	
	SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd-HH:mm",Locale.US);
	
	
	private Calendar[] getGeneralStartEnd(String q) {
		String[] e = q.split(";");
		try {
			Calendar general_st = Calendar.getInstance();
			Calendar general_et = Calendar.getInstance();
			general_st.setTime(f.parse(e[0]+"-00:00"));
			general_et.setTime(f.parse(e[1]+"-23:59"));	
			return new Calendar[]{general_st,general_et};
		} catch (ParseException e1) {
			//e1.printStackTrace();
		}
		return null;
	}
	
	private List<Sightseeing> parseInput(String q) {
		EventFilesFinderI eff = DataFactory.getEventFilesFinder();
		List<Sightseeing> ls = new ArrayList<Sightseeing>();
		String[] e = q.split(";");
		
		for(int i=2; i<e.length;i=i+6) {
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
			double r = RADIUS_EXPAND+LatLonUtils.getHaversineDistance(new LatLonPoint(lat1,lon1), new LatLonPoint(lat2,lon2));
			Placemark p = new Placemark("",new double[]{(lat1+lat2)/2,(lon1+lon2)/2},r);
			Sightseeing s = new Sightseeing(p, st, et, weekly_repeat);
			
			String dir = eff.find(e[i],e[i+1].split(":")[0],e[i+2],e[i+3].split(":")[0],lon1,lat1,lon2,lat2);
			System.out.println("--->"+dir);
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
		/*
		//7f3e4f68105e863aa369e5c39ab5789975f0788386b45954829346b7ca63	Home,45.073963,7.676248999999999	Work,45.468133,7.872852
		String h = "((45.073963,7.676249), (45.073963,7.676249));";
		String w = "((45.468133,7.872852), (45.468133,7.872852));";
		String q = "2012-03-05;2012-03-30;"+
				   "2012-03-05;18:00;2012-03-06;7:00;true;"+h+
				   "2012-03-06;18:00;2012-03-07;7:00;true;"+h+
				   "2012-03-07;18:00;2012-03-08;7:00;true;"+h+
				   "2012-03-08;18:00;2012-03-09;7:00;true;"+h+
				   "2012-03-09;18:00;2012-03-10;7:00;true;"+h+
				   "2012-03-10;18:00;2012-03-11;7:00;true;"+h+
				   "2012-03-11;18:00;2012-03-12;7:00;true;"+h+
				   
				   "2012-03-05;9:00;2012-03-05;17:00;true;"+w+
				   "2012-03-06;9:00;2012-03-06;17:00;true;"+w+
				   "2012-03-07;9:00;2012-03-07;17:00;true;"+w+
				   "2012-03-08;9:00;2012-03-08;17:00;true;"+w+
				   "2012-03-09;9:00;2012-03-09;17:00;true;"+w;	
		System.out.println(uf.find(q));
		*/
		
		String slucia = "((45.44080378864188,12.32151042933514), (45.44080378864188,12.32151042933514));";
		String smarco = "((45.43367746627466,12.33969676659992), (45.43367746627466,12.33969676659992));";
		String hfirenze = "((45.43330167128715,12.33636341730171), (45.43330167128715,12.33636341730171));";
		String rschiavoni = "((45.43385832022145,12.34456643668281), (45.43385832022145,12.34456643668281));";
		String psusa = "((45.07162182018397,7.665977493661589), (45.07162182018397,7.665977493661589));";
		String pnuova = "((45.06176100000236,7.678264000003818), (45.06176100000236,7.678264000003818));";
		String tilab = "((45.11197578759646,7.670902828541375), (45.11197578759646,7.670902828541375));";
		String q = "2012-03-05;2012-03-30;"+
				   "2014-03-15;12:35;2014-03-15;12:39;false;"+slucia+
				   "2014-03-15;16:14;2014-03-15;16:18;false;"+smarco+
				   "2014-03-16;09:55;2014-03-16;09:59;false;"+hfirenze+
				   "2014-03-16;11:58;2014-03-16;12:02;false;"+rschiavoni+
				   "2014-03-26;11:56;2014-03-26;12:00;false;"+psusa+
				   "2014-03-26;13:56;2014-03-26;14:00;false;"+tilab+
				   "2014-03-26;17:28;2014-03-26;17:32;false;"+pnuova;
		System.out.println(uf.find(q));
		
		
	}
}



