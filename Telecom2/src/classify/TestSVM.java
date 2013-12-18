package classify;

import java.util.List;


public class TestSVM {
	
	public static void main(String[] args) throws Exception {
		
		int[] labels = MNISTReader.readLabels("G:/DATASET/MNIST/train-labels.idx1-ubyte");
		List<int[]> imgs = MNISTReader.readImages("G:/DATASET/MNIST/train-images.idx3-ubyte");
				
		int subsize = 50;
		List<int[]> sub = imgs.subList(0, subsize);
		double[][] data = convert(sub);
		int[] sublabels = new int[subsize];
		System.arraycopy(labels, 0, sublabels, 0, subsize);
		double[] y = convert(sublabels);
		MNISTReader.createImg(sub);
		normalize(data);
		System.out.println("Input data read!");
		
		
		for(int i=0; i<data.length;i++) {
			System.out.print(y[i]+" = ");
			for(int j=0; j<data[i].length;j++)
				System.out.print((int)data[i][j]+"\t");
			System.out.println();
		}
		
		
		SVMClassifier test = new SVMClassifier();
		test.optimizeParams(data, y);
		
		for(int i=0; i<subsize;i++) {
			double clazz = test.classify(convert(imgs.get(i)));
			System.out.println(clazz+" VS. "+labels[i]);
		}
	}
	
	
	public static void normalize(double[][] x) {
		// get max
		double max = 0;
		for(int i=0; i<x.length;i++)
		for(int j=0; j<x[i].length;j++)
			max = Math.max(max, x[i][j]);
		for(int i=0; i<x.length;i++)
		for(int j=0; j<x[i].length;j++)
			x[i][j] = x[i][j] / max;
			
	}
	
	
	public static double[][] convert(List<int[]> imgs) {
		double[][] data = new double[imgs.size()][];
		for(int i=0; i<data.length;i++)
			data[i] = convert(imgs.get(i));
		return data;
	}
	
	public static double[] convert(int[] x) {
		double[] y = new double[x.length];
		for(int i=0; i<y.length;i++)
			y[i] = x[i];
		return y;
	}
	
}
