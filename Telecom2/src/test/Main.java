package test;

import java.io.File;
import java.util.List;

import visual.KMLPath;

import analysis.PlsEvent;

public class Main {
	public static void main(String[] args) throws Exception  {
		List<PlsEvent> m = PlsEvent.readEvents(new File("D:/CODE/TELECOM/Telecom2/output/example/b21f45d1e79b1b628d8e0121c32e15d8a2b91ea1093e1a419f69a5829939a64.csv"));
		KMLPath.openFile("example.kml");
		KMLPath.print("example", m);
		KMLPath.closeFile();
	}
}
