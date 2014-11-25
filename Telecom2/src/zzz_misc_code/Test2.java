package zzz_misc_code;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Test2 {
	public static void main(String[] args)  throws Exception {
		
		PrintWriter out = new PrintWriter(new FileWriter("C:/BASE/Topic/allusers.txt"));
		
		File dir = new File("C:/BASE/Topic");
		for(File f: dir.listFiles()) {
			if(f.isDirectory())
				out.println(f.getName());
		}
		out.close();
		
	}
}
