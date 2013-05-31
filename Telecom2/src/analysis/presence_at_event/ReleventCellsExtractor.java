package analysis.presence_at_event;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import pls_parser.PLSEventsAroundAPlacemark;
import utils.Config;
import utils.Logger;
import analysis.PLSBehaviorInAnArea;
import analysis.PLSMap;
import area.CityEvent;
import area.Placemark;

public class ReleventCellsExtractor {
	
	public static final double z_threshold = 3;
	
	
	public static void main(String[] args) throws Exception {
		
		String[] pnames = new String[]{"Juventus Stadium (TO)","Stadio Olimpico (TO)","Stadio Silvio Piola (NO)"};
		
		for(String pn : pnames) {
			Placemark p = Placemark.getPlacemark(pn);
			p.changeRadius(1000);
			List<String> cells = process(p);
			for(String celllac: cells)
				System.out.println(celllac);
		}
	}
	
	public static List<String> process(Placemark p) throws Exception {
		String file = Config.getInstance().base_dir+"/PLSEventsAroundAPlacemark/"+p.name+"_"+p.radius+".txt";
		File f = new File(file);
		if(!f.exists()) {
			Logger.logln(file+" does not exist");
			Logger.logln("Executing PLSEventsAroundAPlacemark.process()");
			PLSEventsAroundAPlacemark.process(p);
		}
		
		Logger.logln("Processing "+p.name);		
		Collection<CityEvent> events = CityEvent.getEventsInData();
		List<CityEvent> releventEvents = new ArrayList<CityEvent>();
		for(CityEvent e : events) 
			if(e.spot == p) releventEvents.add(e);
		
		/*
		Logger.logln("Relevant Events:");
		for(CityEvent e : releventEvents) 
			Logger.logln(e.toString());
		*/
		
		Map<String,PLSMap> cell_plsmap = PLSBehaviorInAnArea.getPLSMap(file,true);
		List<String> cells = new ArrayList<String>();
		
		for(String cell: cell_plsmap.keySet()) {
			PLSMap plsmap = cell_plsmap.get(cell);
			DescriptiveStatistics[] stats = PLSBehaviorInAnArea.getStats(plsmap);
			double[] z_pls_data = PLSBehaviorInAnArea.getZ(stats[0]);
			double[] z_usr_data =  PLSBehaviorInAnArea.getZ(stats[1]);
			
			// this cell has outliers when event take place?
			
			int outliers_count = 0;
			
			Calendar cal = (Calendar)plsmap.startTime.clone();
			int i = 0;
			next_event:
			for(CityEvent e: releventEvents) {
				
				//if(cell.equals("4004750727")) System.out.println(e);
				
				for(;i<plsmap.getHours();i++) {
					
					
					
					boolean after_event = cal.after(e.et);
					boolean found_outlier = e.st.before(cal) && e.et.after(cal) &&  (z_pls_data[i] > z_threshold || z_usr_data[i] > z_threshold);
					
					if(found_outlier) outliers_count ++;
					//if(cell.equals("4004750727")) System.out.println(i+" "+cal.getTime()+" -- oc -->"+outliers_count);
					
					cal.add(Calendar.HOUR_OF_DAY, 1);
					if(after_event || found_outlier) {
						i++;
						continue next_event;
					}
				}
			}
			
			if(outliers_count > Math.ceil(1.0*releventEvents.size()/2)){
				cells.add(cell);
				//PLSBehaviorInAnArea.drawGraph(p.name+" Cell = "+cell+" OC = "+outliers_count,plsmap.getDomain(),null,null,z_pls_data,z_usr_data);
			}
			
		}
		return cells;
	}
			
}
