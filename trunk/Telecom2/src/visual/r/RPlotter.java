package visual.r;

import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.rosuda.REngine.Rserve.RConnection;

import utils.Config;

public class RPlotter {
	
	private static RConnection c = null;
	
	
	/************************************************************************************************************/
	/************************************************************************************************************/
	/* 													BAR										 				*/
	/************************************************************************************************************/
	/************************************************************************************************************/
	
	
	public static void drawBar(String[] x, double [] y, String xlab, String ylab, String file, String opts) {
		try {
            	
            c = new RConnection();// make a new local connection on default port (6311)
            
            c.assign("x", x);
            c.assign("y", y);
            
            String end = opts==null || opts.length()==0 ? ";" : " + "+opts+";";
            String code = 
            		   "library(ggplot2);"
            	     + "z <- data.frame(x,y);"
            	     + "ggplot(z,aes(x=x,y=y)) + geom_bar(stat='identity') + theme_bw() +xlab('"+xlab+"') + ylab('"+ylab+"')"+end
            	     + "ggsave('"+file+"');"
            	     + "dev.off();";
            
            //System.out.println(code);
            c.eval(code);
            c.close();
            Desktop.getDesktop().open(new File(file));
        } catch (Exception e) {
        	c.close();
        	if(e.getMessage().startsWith("Cannot connect")) {
             	System.err.println("You must launch the following code in R");
             	System.err.println("library(Rserve)");
             	System.err.println("Rserve()");
            }
            else e.printStackTrace();
        }      
	}
	
	
	public static void drawBar(String[] x, List<double []> y, List<String> names, String kind, String xlab, String ylab, String file, String opts) {
		try {
            	
            c = new RConnection();// make a new local connection on default port (6311)
            
            c.assign("x", x);
            for(int i=0; i<y.size();i++)
            	c.assign("y"+i, y.get(i));
            
           
            
            StringBuffer sby= new StringBuffer();
            for(int i=0; i<y.size();i++)
            	sby.append(",y"+i);
            
            StringBuffer sbn= new StringBuffer("'x'");
            for(int i=0; i<names.size();i++)
            	sbn.append(",'"+names.get(i)+"'");
            
            
            String end = opts==null || opts.length()==0 ? ";" : " + "+opts+";";
            String code = 
            		   "library(ggplot2);"
            		 + "library(reshape2);"
            	     + "z <- data.frame(x"+sby+");"
            	     + "names(z) <- c("+sbn+");"
            	     + "z <- melt(z,id.vars=c('x'));"
            	     + "names(z) <- c('x','"+kind+"','value');"
            	     + "ggplot(z,aes(x=x, y=value, fill="+kind+")) + geom_bar(stat='identity', position='dodge') + theme_bw() +xlab('"+xlab+"') + ylab('"+ylab+"')"+end
            	     + "ggsave('"+file+"');"
            	     + "dev.off();";
            
            //System.out.println(code);
            c.eval(code);
            c.close();
            Desktop.getDesktop().open(new File(file));
        } catch (Exception e) {
        	c.close();
        	if(e.getMessage().startsWith("Cannot connect")) {
             	System.err.println("You must launch the following code in R");
             	System.err.println("library(Rserve)");
             	System.err.println("Rserve()");
            }
            else e.printStackTrace();
        }      
	}
	
	
	/************************************************************************************************************/
	/************************************************************************************************************/
	/* 												LINE										 				*/
	/************************************************************************************************************/
	/************************************************************************************************************/
	
	
	
	public static void drawLine(double[] x, double [] y, String xlab, String ylab, String file, String opts) {
		try {
            	
            c = new RConnection();// make a new local connection on default port (6311)
            
            c.assign("x", x);
            c.assign("y", y);
            
            String end = opts==null || opts.length()==0 ? ";" : " + "+opts+";";
            String code = 
            		   "library(ggplot2);"
            	     + "z <- data.frame(x,y);"
            	     + "ggplot(z,aes(x=x,y=y)) + geom_line() + geom_point() + theme_bw() +xlab('"+xlab+"') + ylab('"+ylab+"')"+end
            	     + "ggsave('"+file+"');"
            	     + "dev.off();";
            //System.out.println(code);
            c.eval(code);
            c.close();
            Desktop.getDesktop().open(new File(file));
        } catch (Exception e) {
        	c.close();
        	if(e.getMessage().startsWith("Cannot connect")) {
             	System.err.println("You must launch the following code in R");
             	System.err.println("library(Rserve)");
             	System.err.println("Rserve()");
            }
            else e.printStackTrace();
        }      
	}
	
	
	public static void drawLine(String[] x, List<double []> y, List<String> names, String kind, String xlab, String ylab, String file, String opts) {
		try {
            	
            c = new RConnection();// make a new local connection on default port (6311)
            
            c.assign("x", x);
            for(int i=0; i<y.size();i++)
            	c.assign("y"+i, y.get(i));
            
           
            
            StringBuffer sby= new StringBuffer();
            for(int i=0; i<y.size();i++)
            	sby.append(",y"+i);
            
            StringBuffer sbn= new StringBuffer("'x'");
            for(int i=0; i<names.size();i++)
            	sbn.append(",'"+names.get(i)+"'");
            
            
            String end = opts==null || opts.length()==0 ? ";" : " + "+opts+";";
            String code = 
            		   "library(ggplot2);"
            		 + "library(reshape2);"
            	     + "z <- data.frame(x"+sby+");"
            	     + "names(z) <- c("+sbn+");"
            	     + "z <- melt(z,id.vars=c('x'));"
            	     + "names(z) <- c('x','"+kind+"','value');"
            	     + "ggplot(z,aes(x=x, y=value, linetype="+kind+", group="+kind+", shape="+kind+")) + geom_line() + geom_point() + theme_bw() +xlab('"+xlab+"') + ylab('"+ylab+"')"+end
            	     + "ggsave('"+file+"');"
            	     + "dev.off();";
            
            //System.out.println(code);
            c.eval(code);
            c.close();
            Desktop.getDesktop().open(new File(file));
        } catch (Exception e) {
        	c.close();
        	if(e.getMessage().startsWith("Cannot connect")) {
             	System.err.println("You must launch the following code in R");
             	System.err.println("library(Rserve)");
             	System.err.println("Rserve()");
            }
            else e.printStackTrace();
        }      
	}
	
	
	/************************************************************************************************************/
	/************************************************************************************************************/
	/* 												SCATTER										 				*/
	/************************************************************************************************************/
	/************************************************************************************************************/
	
	
	
	public static void drawScatter(double[] x, double [] y, String xlab, String ylab, String file, String opts) {
		try {
            	
            c = new RConnection();// make a new local connection on default port (6311)
            
            c.assign("x", x);
            c.assign("y", y);
            
            String end = opts==null || opts.length()==0 ? ";" : " + "+opts+";";
            String code = 
            		   "library(ggplot2);"
            	     + "z <- data.frame(x,y);"
            	     + "ggplot(z,aes(x=x,y=y)) + geom_point() + theme_bw() +xlab('"+xlab+"') + ylab('"+ylab+"')"+end
            	     + "ggsave('"+file+"');"
            	     + "dev.off();";
            //System.out.println(code);
            c.eval(code);
            c.close();
            Desktop.getDesktop().open(new File(file));
        } catch (Exception e) {
        	c.close();
        	if(e.getMessage().startsWith("Cannot connect")) {
             	System.err.println("You must launch the following code in R");
             	System.err.println("library(Rserve)");
             	System.err.println("Rserve()");
            }
            else e.printStackTrace();
        }      
	}
	
	
	public static void drawScatter(List<double[]> x, List<double []> y, List<String> names, String kind, String xlab, String ylab, String file, String opts) {
		try {
            	
            c = new RConnection();// make a new local connection on default port (6311)
            
            for(int i=0; i<x.size();i++)
            	c.assign("x"+i, x.get(i));
            
            for(int i=0; i<y.size();i++)
            	c.assign("y"+i, y.get(i));
                      
            
            String end = opts==null || opts.length()==0 ? ";\n" : " + "+opts+";\n";
            String code = 
            		   "library(ggplot2);\n"
            		 + "library(reshape2);\n";
            
            for(int i=0; i<x.size();i++) {
            	code += "z"+i+" <- data.frame(x"+i+",y"+i+");\n";
            	code += "names(z"+i+") <- c('x','"+names.get(i)+"');\n";
            	code += "z"+i+" <- melt(z"+i+",id.vars=c('x'));\n";
            }
            
            String zl = "";
            for(int i=0; i<x.size();i++)
            	zl += ",z"+i;
            zl = zl.substring(1);
            
            code +=  "z <- rbind("+zl+");\n"
            		 + "names(z) <- c('x','"+kind+"','value');"
            	     + "ggplot(z,aes(x=x, y=value, linetype="+kind+", group="+kind+", shape="+kind+")) + geom_point() + theme_bw() +xlab('"+xlab+"') + ylab('"+ylab+"')"+end
            	     + "ggsave('"+file+"');\n"
            	     + "dev.off();\n";
            
            System.out.println(code);
            c.eval(code);
            c.close();
            Desktop.getDesktop().open(new File(file));
        } catch (Exception e) {
        	c.close();
        	if(e.getMessage().startsWith("Cannot connect")) {
             	System.err.println("You must launch the following code in R");
             	System.err.println("library(Rserve)");
             	System.err.println("Rserve()");
            }
            else e.printStackTrace();
        }      
	}
	
	public static void main(String[] args) {
		
		//drawBar(new String[]{"a","b","c"},new double[]{5,6,7},"x","y",Config.getInstance().base_folder+"/Images/test.pdf",null);
		//drawLine(new double[]{1,2,3},new double[]{5,6,7},"x","y",Config.getInstance().base_folder+"/Images/test.pdf",null);
		//drawScatter(new double[]{1,2,9},new double[]{5,6,7},"x","y",Config.getInstance().base_folder+"/Images/test.pdf",null);
		
		/*
		List<double[]> l = new ArrayList<double[]>();
		l.add(new double[]{5,6,7});
		l.add(new double[]{1,2,7});
		l.add(new double[]{5,-1,4});
		
		List<String> names = new ArrayList<String>();
		names.add("ok1");
		names.add("ok2");
		names.add("ok3");
		
		//drawBar(new String[]{"a","b","c"},l,names,"x","y",Config.getInstance().base_folder+"/Images/test.pdf",null);
		//drawLine(new String[]{"a","b","c"},l,names,"types","x","y",Config.getInstance().base_folder+"/Images/test.pdf",null);
		*/
		
		
		List<double[]> lx = new ArrayList<double[]>();
		lx.add(new double[]{5,6,7});
		lx.add(new double[]{1,2,7});
		lx.add(new double[]{5,-1,4});
		
		
		List<double[]> ly = new ArrayList<double[]>();
		ly.add(new double[]{5,6,7});
		ly.add(new double[]{1,2,7});
		ly.add(new double[]{5,-1,4});
		
		List<String> names = new ArrayList<String>();
		names.add("stadium1");
		names.add("stadium2");
		names.add("stadium3");
		
		drawScatter(lx,ly,names,"types","x","y",Config.getInstance().base_folder+"/Images/test.pdf",null);
	}
}
