package visual.text;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import utils.Config;
public class LatexTable {
	
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
	public static void printTable(String file, String t00, String[] rowh, String[] colh, String[][] m) {
		try {
			File f = new File(file.replaceAll("_", ""));
			f.getParentFile().mkdirs();
			PrintWriter out = new PrintWriter(new FileWriter(f));
			
			out.print("\\begin{tabular}{ |");
			for(int i=0; i<colh.length+1;i++) out.print(" c |");
			out.println("}");
			out.println("\\hline");
			out.print("{\\bf "+t00+" }");
			for(int j=0; j<colh.length;j++) out.print(" & {\\bf "+colh[j]+"}");
			out.println(" \\\\ \\hline");
			for(int i=0; i<rowh.length;i++) {
				out.print("{\\bf "+rowh[i]+"}");
				for(int j=0; j<colh.length;j++) out.print(" & "+m[i][j]);
				out.println(" \\\\ \\hline");
			}
			out.println("\\end{tabular}");
			
			out.close();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	public static void printTable(String file, String t00, String[] rowh, String[] colh, int[][] m) {
		String[][] ms = new String[m.length][m[0].length];
		for(int i=0; i<m.length;i++)
		for(int j=0; j<m[i].length;j++)
			ms[i][j] = String.valueOf(m[i][j]);
		printTable(file, t00, rowh, colh, ms);
	}
	
	private static final DecimalFormat F2 = new DecimalFormat("##.##",new DecimalFormatSymbols(Locale.US)); // F2 two digits after the comma
	public static void printTable(String file, String t00, String[] rowh, String[] colh, double[][] m) {
		String[][] ms = new String[m.length][m[0].length];
		for(int i=0; i<m.length;i++)
		for(int j=0; j<m[i].length;j++)
			ms[i][j] =F2.format(m[i][j]);
		printTable(file, t00, rowh, colh, ms);
	}

	
	public static void main(String[] args) {
		String t00 = "X"; 
		String[] colh = new String[]{"A","B"}; 
		String[] rowh = new String[]{"1","2"};  
		int[][] m = new int[][]{{1,2},{3,4}};
		double[][] m2 = new double[][]{{671.2889765,2.0},{3.14,4.5}};
		printTable(Config.getInstance().paper_folder+"/img/tables/test.tex",t00,rowh,colh,m2);
	}
	
}
