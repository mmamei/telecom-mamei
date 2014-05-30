package dataset.db;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.Config;
import dataset.PLSCoverageTimeI;
import dataset.db.insert.DBConnection;

 class PLSCoverageTime implements PLSCoverageTimeI  {
	
	static Config conf = null;
	

	public static void main(String[] args) {
		PLSCoverageTime apc = new PLSCoverageTime();
			
		Map<String,List<String>> all =  apc.computeAll();
		for(String key: all.keySet()) 
			System.out.println(key+" -> "+apc.getNumYears(all.get(key)));
		
		
		System.out.println(apc.getJSMap(all));
	}
	
	
	
	public String getJSMap(Map<String,List<String>> all) {
		StringBuffer sb = new StringBuffer();
		for(String key: all.keySet()) {
			sb.append("var dataTable_"+key+" = new google.visualization.DataTable();");
			sb.append("dataTable_"+key+".addColumn({ type: 'date', id: 'Date' });");
			sb.append("dataTable_"+key+".addColumn({ type: 'number', id: 'PLS Coverage' });");
			sb.append("dataTable_"+key+".addRows([\n");
			List<String> dmap = all.get(key);
			
			String one_year = null;
			for(String d: dmap) {
				try {
					String year = d.substring(0,4);
					if(one_year == null) one_year = year;
					int month = Integer.parseInt(d.substring(4,6))-1;
					String day = d.substring(6,8);
					String s = "[ new Date("+year+", "+month+", "+day+"), 24 ],\n";
					sb.append(s);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			sb.append("[ new Date("+one_year+", 0, 1), 0 ],\n");
			sb.append("]);\n");
		}
		return sb.toString();
	}
	
	public int getNumYears(List<String> dmap) {
		int min_year = 3000;
		int max_year = 0;
		for(String d: dmap) {
			int year = Integer.parseInt(d.substring(0,4));
			min_year = Math.min(min_year, year);
			max_year = Math.max(max_year, year);
		}
		return max_year - min_year + 1;
	}

	//static final String[] MONTHS = new String[]{"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	public Map<String,List<String>> computeAll() {
		Map<String,List<String>> map = new HashMap<String,List<String>>();
		Statement s = DBConnection.getStatement();
		try {
			ResultSet r = s.executeQuery("SELECT table_name FROM information_schema.tables");
			while(r.next()) {
				String t = r.getString("table_name"); 
				if(t.startsWith("pls_")) {
					String key = t.substring(0, t.lastIndexOf("_"));
					String day = t.substring(t.lastIndexOf("_")+1);
					List<String> m = map.get(key);
					if(m == null) {
						m = new ArrayList<String>();
						map.put(key, m);
					}
					m.add(day);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
	/*	
	public List<String> compute() {
		String dir = Config.getInstance().pls_folder;
		dir = dir.substring(dir.indexOf("file_pls/")+9);
		dir = dir.substring(0,dir.indexOf("/"));
		return computeAll().get(dir);
		
	}
	*/
}
