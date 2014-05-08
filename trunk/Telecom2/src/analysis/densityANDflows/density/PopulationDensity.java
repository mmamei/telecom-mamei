package analysis.densityANDflows.density;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import region.Region;
import region.RegionI;
import region.RegionMap;
import utils.CopyAndSerializationUtils;
import utils.Logger;

public class PopulationDensity {
	
	public static void main(String[] args) throws Exception {
		String region = "Piemonte";
		//String region = "TorinoGrid20";
		String kind_of_place = "SATURDAY_NIGHT";
		String exclude_kind_of_place = null;//"HOME";
		
		File input_obj_file = new File("BASE/cache/"+region+".ser");
		if(!input_obj_file.exists()) {
			System.out.println(input_obj_file+" does not exist... run the region parser first!");
			System.exit(0);
		}
		
		RegionMap rm = (RegionMap)CopyAndSerializationUtils.restore(input_obj_file); 
		Map<String,UserPlaces> up = UserPlaces.readUserPlaces("BASE/PlaceRecognizer/file_pls_piem_users_above_2000/results.csv");
		
		
		Map<String,Double> density = process(rm,up,kind_of_place,exclude_kind_of_place);
		String title = rm.getName()+"-"+kind_of_place+"-"+exclude_kind_of_place;
		
	}
	
	
	public static Map<String,Double> process(RegionMap rm, Map<String,UserPlaces> up, String kind_of_place, String exclude_kind_of_place) {
		
		Map<String,Double> density = new HashMap<String,Double>();
		
		for(UserPlaces p: up.values()) {
			List<double[]> lkop = p.places.get(kind_of_place);
			List<double[]> lnokop = p.places.get(exclude_kind_of_place);
			List<double[]> r = exclude_nopkop(rm,lkop,lnokop);
			if(r != null)
			for(double[] ll: r) {
				RegionI reg = rm.get(ll[0], ll[1]);
				if(reg == null) 
					Logger.logln(ll[0]+","+ll[1]+" is outside "+rm.getName());
				else {
					Double val = density.get(reg.getName());
					if(val == null) val = 0.0;
					val += 1.0;
					density.put(reg.getName(), val);
				}
			}
		}
		return density;
	}
	
	
	public static List<double[]> exclude_nopkop(RegionMap rm, List<double[]> kop, List<double[]> nokop) {
		if(kop==null) return null;
		if(nokop==null) return kop;
		List<double[]> r = new ArrayList<double[]>();
		for(double[] p1: kop) {
			boolean found = false;
			RegionI r1 = rm.get(p1[0],p1[1]);
			for(double[] p2 : nokop) {
				RegionI r2 = rm.get(p2[0],p2[1]);;
				if(r1!=null && r2!=null && r1.equals(r2)) found = true;
			}
			if(!found) r.add(p1);
		}
		return r;
	}
	
	
}
