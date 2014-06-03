package visual.java;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import region.CityEvent;
import analysis.PLSTimeDensity;

public class PLSPlotter {
	
	static final String[] MONTHS = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	
public static GraphPlotter drawGraph(String title, String[] domain, double[] data, PLSTimeDensity plsmap, List<CityEvent> relevantEvents) {
		
		GraphPlotter gps =  GraphPlotter.drawGraph(title, "", "hour", "n.", "", domain, data);
		
		if(relevantEvents!=null) {
			// draw events' annotations 
			Calendar cal = (Calendar)plsmap.startTime.clone();
			int i = 0;
			next_event:
			for(CityEvent e: relevantEvents) {
				for(;i<plsmap.getHours();i++) {
					boolean after_event = cal.after(e.et);
					boolean in_event = e.st.before(cal) && e.et.after(cal);
					if(in_event) {
						String label = e.st.get(Calendar.DAY_OF_MONTH)+" "+MONTHS[e.st.get(Calendar.MONTH)];
						gps.addAnnotation(label,i+0.5*e.durationH(),2);
					}
					cal.add(Calendar.HOUR_OF_DAY, 1);
					if(after_event || in_event) {
						i++;
						continue next_event;
					}
				}
			}
		}
		return gps;
	}
	
	public static GraphPlotter[] drawGraph(String title, String[] domain, double[] pls_data,double[] usr_data,double[] z_pls_data,double[] z_usr_data, PLSTimeDensity plsmap, List<CityEvent> relevantEvents) {
		List<String> labels = new ArrayList<String>();
		List<String> titles = new ArrayList<String>();
		List<String[]> domains = new ArrayList<String[]>();
		List<double[]> data = new ArrayList<double[]>();
		if(pls_data != null) {
			labels.add("pls");
			titles.add("N. PLS Events around "+title);
			domains.add(domain);
			data.add(pls_data);
		}
		if(z_pls_data != null) {
			labels.add("z-pls");
			titles.add("Z-Score PLS Events around "+title);
			domains.add(domain);
			data.add(z_pls_data);
		}
		if(usr_data != null) {
			labels.add("users");
			titles.add("N. Users around "+title);
			domains.add(domain);
			data.add(usr_data);
		}
		if(z_usr_data != null) {
			labels.add("z-users");
			titles.add("Z-Score Users around "+title);
			domains.add(domain);
			data.add(z_usr_data);
		}
		
		int nrows = 1;
		int ncols = 1;
		
		if(data.size()==2) {
			ncols = 2;
		}
		else if(data.size()==4) {
			nrows = 2;
			ncols = 2;
		}
	
		GraphPlotter[] gps =  GraphPlotter.drawMultiGraph(nrows, ncols, title, titles, "hour", "n.", labels, domains, data);
		
		if(relevantEvents!=null) {
			// draw events' annotations 
			Calendar cal = (Calendar)plsmap.startTime.clone();
			int i = 0;
			next_event:
			for(CityEvent e: relevantEvents) {
				for(;i<plsmap.getHours();i++) {
					boolean after_event = cal.after(e.et);
					boolean in_event = e.st.before(cal) && e.et.after(cal);
					if(in_event) {
						for(GraphPlotter gp: gps){
							String label = e.st.get(Calendar.DAY_OF_MONTH)+" "+MONTHS[e.st.get(Calendar.MONTH)];
							gp.addAnnotation(label,i+0.5*e.durationH(),2);
						}
					}
					cal.add(Calendar.HOUR_OF_DAY, 1);
					if(after_event || in_event) {
						i++;
						continue next_event;
					}
				}
			}
		}
		return gps;
	}
}
