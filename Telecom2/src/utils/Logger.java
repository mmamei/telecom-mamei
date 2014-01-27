package utils;

public class Logger {
	public static void logln(String x) {
		System.out.println(x);
	}
	
	public static void log(String x) {
		System.out.print(x);
	}
	
	public static void printStackTrace() {
		Exception e = new Exception("");
		e.printStackTrace(System.out);
	}
}
