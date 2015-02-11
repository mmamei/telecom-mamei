package zzz_misc_code;

import java.awt.Desktop;
import java.io.File;

import org.rosuda.REngine.Rserve.RConnection;

public class RServe {
	public static void main(String[] args) {
        try {
        	
        	
        	String file = "C:/x.pdf";
        	double[] x = new double[]{1,2,3};
        	double[] y = new double[]{4,5,7};
        	String xlab = "distance (m)";
        	String ylab = "CDF";
        	
            RConnection c = new RConnection();// make a new local connection on default port (6311)
            
            c.assign("x", x);
            c.assign("y", y);
            
            String code = 
            		   "library(ggplot2);"
            	     + "z <- data.frame(x,y);"
            	     + "ggplot(z,aes(x=x,y=y)) + geom_line() + geom_point() + theme_bw() +xlab('"+xlab+"') + ylab('"+ylab+"');"
            	     + "ggsave('"+file+"');"
            	     + "dev.off();";
            c.parseAndEval(code);
            c.close();
            Desktop.getDesktop().open(new File(file));
        } catch (Exception e) {
            if(e.getMessage().startsWith("Cannot connect")) {
            	System.err.println("You must launch the following code in R");
            	System.err.println("library(Rserve)");
            	System.err.println("Rserve()");
            }
            else e.printStackTrace();
        }       
    }
}
	