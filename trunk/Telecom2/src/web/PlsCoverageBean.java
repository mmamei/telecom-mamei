package web;

import java.io.File;
import java.util.Map;

import pls_parser.AnalyzePLSCoverage;
import utils.FileUtils;
import utils.Logger;

public class PlsCoverageBean {
	
	public String run() {
		StringBuffer sb = new StringBuffer();
		File[] files = FileUtils.getFiles("DATASET/PLS/file_pls");
		for(File f: files) {
			Map<String,String> allDays = AnalyzePLSCoverage.compute(f);
			sb.append(f);
			sb.append("<br>\n");
			for(String d:allDays.keySet()) {
				sb.append(d+" = "+allDays.get(d));
				sb.append("<br>\n");
			}
			sb.append("TOT = "+allDays.size()+"<br>");
		}
		return sb.toString();
	}
	
	
	public static void main(String[] args) {
		PlsCoverageBean pcb = new PlsCoverageBean();
		System.out.println(pcb.run());
	}
	
}
