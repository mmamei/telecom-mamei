package analysis.presence_at_event;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import network.NetworkCell;
import network.NetworkMap;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.gps.utils.LatLonUtils;

import pls_parser.PLSEventsAroundAPlacemark;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Logger;
import visual.GraphPlotter;
import analysis.PLSBehaviorInAnArea;
import analysis.PLSMap;
import area.CityEvent;
import area.Placemark;

public class ReleventCellsExtractor {
	
	public static final boolean DRAW = true;
	
	public static final double z_threshold = 2.5;

	public static final String[] pnames = new String[]{"Juventus Stadium (TO)","Stadio Olimpico (TO)","Stadio Silvio Piola (NO)"};
	
	public static void main(String[] args) throws Exception {
		
		String odir = Config.getInstance().base_dir+"/ReleventCellsExtractor";
		new File(odir).mkdirs();
		
		for(String pn : pnames) {
			Placemark p = Placemark.getPlacemark(pn);
			Set<String> cells = process(p,1000);
			CopyAndSerializationUtils.save(new File(odir+"/"+pn+".ser"), cells);
			
			Logger.logln(pn+" HAS N. CELLS RELEVANT = "+cells.size());
			NetworkMap nm = NetworkMap.getInstance();
			for(String cellac: cells) {
				NetworkCell nc = nm.get(Long.parseLong(cellac));
				int dist = (int)LatLonUtils.getHaversineDistance(nc.getPoint(), p.center_point);
				int radius = (int)nc.getRadius();
				Logger.logln(cellac+" --> dist = "+dist+"m, r = "+radius+"m ---> dist - r = "+(dist-radius)+" m ");
			}	
		}
		
		Logger.logln("Done");
	}
	
	public static void main2(String[] args) throws Exception {
		String odir = Config.getInstance().base_dir+"/ReleventCellsExtractor";
		new File(odir).mkdirs();
		
		for(String pn : pnames) {
			Placemark p = Placemark.getPlacemark(pn);
			Set<String> cells = p.cellsAround;
			Logger.logln(pn+" HAS N. CELLS RELEVANT = "+cells.size());
			CopyAndSerializationUtils.save(new File(odir+"/"+pn+".ser"), cells);
		}
		Logger.logln("TEST Main Done");
	}
	
	public static Set<String> process(Placemark op, double search_r) {
		
		Set<String> cells = new HashSet<String>();
		
		try {
			Placemark p = op.clone();
			p.changeRadius(search_r);
		
			String file = Config.getInstance().base_dir+"/PLSEventsAroundAPlacemark/"+p.name+"_"+p.radius+".txt";
			File f = new File(file);
			if(!f.exists()) {
				Logger.logln(file+" does not exist");
				Logger.logln("Executing PLSEventsAroundAPlacemark.process()");
				PLSEventsAroundAPlacemark.process(p);
			}
			
			//Logger.logln("Processing "+p.name);	
			
			// get the city events to be considered to find relevant cells for this placemark
			
			Collection<CityEvent> events = CityEvent.getEventsInData();
			List<CityEvent> releventEvents = new ArrayList<CityEvent>();
			for(CityEvent e : events) 
				if(e.spot.name.equals(p.name)) releventEvents.add(e);
			
			/*
			Logger.logln("Relevant Events:");
			for(CityEvent e : releventEvents) 
				Logger.logln("\t\t"+e.toString());
			*/
			
			Map<String,PLSMap> cell_plsmap = PLSBehaviorInAnArea.getPLSMap(file,true);
			
			
			for(String cell: cell_plsmap.keySet()) {
				PLSMap plsmap = cell_plsmap.get(cell);
				DescriptiveStatistics[] stats = PLSBehaviorInAnArea.getStats(plsmap);
				double[] z_pls_data = PLSBehaviorInAnArea.getZ(stats[0],plsmap.startTime);
				double[] z_usr_data =  PLSBehaviorInAnArea.getZ(stats[1],plsmap.startTime);
				
				// has this cell outliers when event take place?
				
				int outliers_count = 0;
				
				Calendar cal = (Calendar)plsmap.startTime.clone();
				int i = 0;
				next_event:
				for(CityEvent e: releventEvents) {
					
					//if(cell.equals("4004750727")) System.out.println(e);
					
					for(;i<plsmap.getHours();i++) {
						
						boolean after_event = cal.after(e.et);
						boolean found_outlier = e.st.before(cal) && e.et.after(cal) &&  
								(z_pls_data[i] > z_threshold || z_usr_data[i] > z_threshold);
						
						if(found_outlier) outliers_count ++;
						//if(cell.equals("4004750727")) System.out.println(i+" "+cal.getTime()+" -- oc -->"+outliers_count);
						
						cal.add(Calendar.HOUR_OF_DAY, 1);
						if(after_event || found_outlier) {
							i++;
							continue next_event;
						}
					}
				}
				
				if(outliers_count > Math.ceil(1.0*releventEvents.size()/2))
					cells.add(cell);
			}
			
			if(DRAW) draw(p.name,cell_plsmap,cells,true);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
			
		return cells;
	}
	
	
	public static void draw(String title, Map<String,PLSMap> cell_plsmap,Set<String> relevant,boolean individual) {
		
		if(relevant.size()==0) return;
		
		PLSMap totplsmap = new PLSMap();
		
		for(String cell: relevant) {
			PLSMap plsmap = cell_plsmap.get(cell);
			totplsmap.usr_counter.putAll(plsmap.usr_counter);
			totplsmap.pls_counter.putAll(plsmap.pls_counter);
			
			if(totplsmap.startTime == null || totplsmap.startTime.after(plsmap.startTime)) totplsmap.startTime = plsmap.startTime;
			if(totplsmap.endTime == null || totplsmap.endTime.before(plsmap.endTime)) totplsmap.endTime = plsmap.endTime;
		}
		
		DescriptiveStatistics[] stats = PLSBehaviorInAnArea.getStats(totplsmap);
		double[] z_pls_data = PLSBehaviorInAnArea.getZ(stats[0],totplsmap.startTime);
		double[] z_usr_data =  PLSBehaviorInAnArea.getZ(stats[1],totplsmap.startTime);
		
		GraphPlotter[] g = PLSBehaviorInAnArea.drawGraph(title,totplsmap.getDomain(),null,null,z_pls_data,z_usr_data);
		if(individual) {
			for(String cell: relevant) {
				PLSMap plsmap = cell_plsmap.get(cell);
				plsmap.startTime = totplsmap.startTime;
				plsmap.endTime = totplsmap.endTime;
				stats = PLSBehaviorInAnArea.getStats(plsmap);
				z_pls_data = PLSBehaviorInAnArea.getZ(stats[0],plsmap.startTime);
				z_usr_data =  PLSBehaviorInAnArea.getZ(stats[1],plsmap.startTime);
				g[0].addData(cell, z_pls_data);
				g[1].addData(cell, z_usr_data);
			}
		}	
	}	
}
