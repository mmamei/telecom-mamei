package classify;

import java.util.List;


public class TestSVM {
	
	public static void main(String[] args) throws Exception {
		
		int[] labels = MNISTReader.readLabels("G:/DATASET/MNIST/train-labels.idx1-ubyte");
		List<int[]> imgs = MNISTReader.readImages("G:/DATASET/MNIST/train-images.idx3-ubyte");
		
		
		int[] train_range = new int[]{0,50};
		int train_size = train_range[1] - train_range[0];
		int[] test_range = new int[]{51,100};
		int test_size = test_range[1] - test_range[0];
		
		
		MNISTReader.createImg("train set",imgs.subList(train_range[0], train_range[1]));
		MNISTReader.createImg("test set",imgs.subList(test_range[0], test_range[1]));
		System.out.println("Input data read!");
		
		// prepare training set
		
		double[][] train_x = convert(imgs.subList(train_range[0], train_range[1]));
		int[] temp = new int[train_size];
		System.arraycopy(labels, train_range[0], temp, 0, train_size);
		double[] train_y = convert(temp);
		normalize(train_x);
		
		// prepare testing set
		
		double[][] test_x = convert(imgs.subList(test_range[0], test_range[1]));
		temp = new int[test_size];
		System.arraycopy(labels, test_range[0], temp, 0, test_size);
		double[] test_y = convert(temp);
		normalize(test_x);
		
		
		SVMClassifier svm = new SVMClassifier();
		svm.optimizeParams(train_x, train_y);
		
		double accuracy = 0;
		for(int i=0; i<test_size;i++) {
			double c = svm.classify(test_x[i]);
			System.out.println(c+" == "+test_y[i]);
			if(c == test_y[i]) accuracy++;
		}
		System.out.println("accuracy = "+accuracy/test_size);
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
