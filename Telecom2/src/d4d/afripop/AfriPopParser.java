package d4d.afripop;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;

import javax.imageio.ImageIO;



public class AfriPopParser {
	
	
	public static final String AFRIPOP_FILE = "G:/DATASET/CENSUS/afripop/apciv10v3.flt";
	
	
	public static void main(String[] args) throws Exception {
		
		String title = "IvoryCoast";
		double west = -8.5998017426885;
		double south = 4.3570288111761;
		double east = -2.4942126426885;
		double north = 10.736773611176101;
		AfriPopMap m = run(title,south,west,north,east);
		m.createKMLOverlay();
		System.out.println("Done!");
	}
	
	
	public static final int ncols = 7327;
	public static final int nrows = 7656;
	public static final double xllcorner = -8.5998017426885;
	public static final double yllcorner = 4.3570288111761;
	public static final double cellsize = 0.0008333;
	
	public static final double xurcorner = xllcorner + (double)ncols * cellsize; 
	public static final double yurcorner = yllcorner + (double)nrows * cellsize;
	
	static {
		System.out.println("west = "+xllcorner);
		System.out.println("south = "+yllcorner);
		System.out.println("east = "+xurcorner);
		System.out.println("north = "+yurcorner);
	}
	
	public static AfriPopMap run(String title, double south, double west, double north, double east) throws Exception {	
	
		DataInputStream d = new DataInputStream(new BufferedInputStream(new FileInputStream(AFRIPOP_FILE)));
		
		int n_rows_to_be_skipped = (int)((yurcorner - north) / cellsize);
		long points_to_be_skipped = (long)n_rows_to_be_skipped * (long)ncols;
		d.skip(4l * points_to_be_skipped); 
		int height = (int)(Math.abs(north-south)/cellsize);
		
		int n_cols_before = (int)(Math.abs(xllcorner - west) / cellsize);
		int width = (int)(Math.abs(west - east) / cellsize);
		int n_cols_after = ncols - width - n_cols_before;	
		
		
		
		float[] data = new float[width * height];
		
		int c = 0;
		for(int j=0; j<height;j++) {
			int skipped = d.skipBytes(4 * n_cols_before);
			for(int i=0; i<width;i++) {
				data[c] = readFloatLittleEndian(d); //d.readFloat();
				c ++;
			}
			skipped = d.skipBytes(4 * n_cols_after);
		}
		d.close();
		
		return new AfriPopMap(title,data,width,height,south,west,north,east);
		
	}
	
	

	
	private static float readFloatLittleEndian(DataInputStream d) throws Exception {
	   // get 4 unsigned raw byte components, accumulating to an int,
	   // then convert the 32-bit pattern to a float.
	   int accum = 0;
	   for ( int shiftBy=0; shiftBy<32; shiftBy+=8 )
	      {
	      accum |= ( d.readByte () & 0xff ) << shiftBy;
	      }
	   return Float.intBitsToFloat( accum );
	 }
}
