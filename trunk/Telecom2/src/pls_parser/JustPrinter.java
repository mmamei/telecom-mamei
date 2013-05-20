package pls_parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

public class JustPrinter extends BufferAnalyzer {
	
	private PrintWriter out;
	
	public JustPrinter() {
		try {
		out = new PrintWriter(new BufferedWriter(new FileWriter(new File("output/test.csv"))));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void analyze(String line) {
		out.println(line);
	}
	
	public void finish() {
		out.close();
	}
}
