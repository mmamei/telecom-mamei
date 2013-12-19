package classify;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;


public class TestSvm {
	
	public static final String trainf = "G:/DATASET/SVM/GUIDE/train.3";
	public static final String testf = "G:/DATASET/SVM/GUIDE/test.3";
	public static final String svmf = "guide3.svm";
	
	public static void main(String[] args) throws Exception {
		// read training 
		Object[] xy = read(trainf);
		double[][] x = (double[][])xy[0];
		double[] y = (double[])xy[1];
		SVMClassifier.scale(x,-1,1);
		
		// create or restore the trained svm
		SVMClassifier svm = null;

	    File f = new File(svmf);
	    if(f.exists()) 
		    svm = new SVMClassifier(svmf);
		 else {
	 		svm = new SVMClassifier();
 			svm.optimizeParams(x, y);
 			svm.save(svmf);
 		}
	    
	    svm = new SVMClassifier();
	    svm.setParam(128, 0.125);
	    svm.trainSVM(x, y);
	    
	    if(testf.equals(""))
	    	System.exit(0);
	    
	    // read testing
	    xy = read(testf);
		x = (double[][])xy[0];
		y = (double[])xy[1];
		SVMClassifier.scale(x,-1,1);
		
		// test accuracy
		double accuracy = 0;
		for(int i=0; i<x.length;i++) {
			double c = svm.classify(x[i]);
			if(c == y[i]) accuracy++;
		}		
		System.out.println("accuracy = "+accuracy/x.length);
		System.out.println("accuracy = "+(int)(100*accuracy/x.length)+"%");
	}
	
	
	public static Object[] read(String file) throws Exception {
		
		List<Integer> labels = new ArrayList<Integer>();
		List<double[]> vectors = new ArrayList<double[]>();
		
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while((line = br.readLine())!=null) {
			String[] e = line.split(" ");
			int l = Integer.parseInt(e[0]);
			double[] fv = new double[e.length-1];
			for(int i=1; i<e.length;i++) {
				fv[i-1] = Double.parseDouble(e[i].substring(e[i].indexOf(":")+1));
			}
			labels.add(l);
			vectors.add(fv);
		}
		br.close();
		
		// convert into arrays
		double[] y = new double[labels.size()];
		double[][] x = new double[vectors.size()][];
		for(int i=0; i<y.length;i++) {
			y[i] = labels.get(i);
			x[i] = vectors.get(i);
		}
		return new Object[]{x,y};	
	}
	
	public static String print(double[] v) {
		StringBuffer sb = new StringBuffer();
		for(double vi : v)
			sb.append(","+vi);
		return sb.substring(1);
	}
	
	
}
