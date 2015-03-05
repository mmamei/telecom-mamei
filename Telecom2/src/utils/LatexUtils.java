package utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.lang.System;
public class LatexUtils {
	
	/*
	
	\begin{tabular}{ | c | c | c | c | c | c | c |}
	\hline
	{\bf a}   &   {\bf b}   &  {\bf c}   &  {\bf d}   &  {\bf e}   &   {\bf$<$-- classified as} \\ \hline
	100\%  &  0  &  0  &  0  &  0 &  {\bf a = Resident}\\ \hline
	0 & 99\%  &  0  &  1\%  &  0  &  {\bf b = Tourist}\\ \hline
	0 &  0 & 100\%  &  0  &  0  &   {\bf c = Commuter}\\ \hline
	0 &  2\%  &  1\% & 53\% & 44\%  &   {\bf d = Transit}\\ \hline
	0 &   0  &  0 & 11\% & 89\%  & {\bf e = Excursionist}\\ \hline
	\end{tabular} 
	
	*/
	
	
	// rowh are the row headers. They are represented on the left
	// colh are the column headers. They are represented in the right
	// t00 is the element at the cros of rowh and colh
	private static final DecimalFormat F2 = new DecimalFormat("##.##",new DecimalFormatSymbols(Locale.US)); // F2 two digits after the comma
	public static void printTable(String t00, String[] rowh, String[] colh, double[][] m) {
		println("");
		print("\\begin{tabular}{ |");
		for(int i=0; i<colh.length+1;i++) print(" c |");
		println("}");
		println("\\hline");
		print("{\\bf "+t00+" }");
		for(int j=0; j<colh.length;j++) print(" & {\\bf "+colh[j]+"}");
		println(" \\\\ \\hline");
		for(int i=0; i<rowh.length;i++) {
			print("{\\bf "+rowh[i]+"}");
			for(int j=0; j<colh.length;j++) print(" & "+F2.format(m[i][j]));
			println(" \\\\ \\hline");
		}
		System.out.println("\\end{tabular}");
		println("");
	}
	
	public static void printTable(String t00, String[] rowh, String[] colh, int[][] m) {
		println("");
		print("\\begin{tabular}{ |");
		for(int i=0; i<colh.length+1;i++) print(" c |");
		println("}");
		println("\\hline");
		print("{\\bf "+t00+" }");
		for(int j=0; j<colh.length;j++) print(" & {\\bf "+colh[j]+"}");
		println(" \\\\ \\hline");
		for(int i=0; i<rowh.length;i++) {
			print("{\\bf "+rowh[i]+"}");
			for(int j=0; j<colh.length;j++) print(" & "+m[i][j]);
			println(" \\\\ \\hline");
		}
		System.out.println("\\end{tabular}");
		println("");
	}
	
	private static<T> void print(T arg) { System.out.print(arg); }
	private static<T> void println(T arg) { System.out.println(arg); }
	
	public static void main(String[] args) {
		String t00 = "X"; 
		String[] colh = new String[]{"A","B"}; 
		String[] rowh = new String[]{"1","2"};  
		int[][] m = new int[][]{{1,2},{3,4}};
		double[][] m2 = new double[][]{{671.2889765,2.0},{3.14,4.5}};
		printTable(t00,rowh,colh,m2);
	}
	
}
