package classify;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class TestSvmMNIST {
	
	public static final boolean PLOT = false;
	public static final int[] train_range = new int[]{0,1000};
	public static final int[] test_range = new int[]{1001,1500};
	
	public static void main(String[] args) throws Exception {
		
		// 60000 total images
		int[] labels = MNISTReader.readLabels(MNISTReader.labels_f);
		List<int[]> imgs = MNISTReader.readImages(MNISTReader.img_f);
		
		int train_size = train_range[1] - train_range[0];
		int test_size = test_range[1] - test_range[0];
		
		if(PLOT) {
			MNISTReader.createImg("train set",imgs.subList(train_range[0], train_range[1]));
			MNISTReader.createImg("test set",imgs.subList(test_range[0], test_range[1]));
		}
		
		System.out.println("Input data read!");
		
		// prepare training set
		
		double[][] train_x = convert(imgs.subList(train_range[0], train_range[1]));
		int[] temp = new int[train_size];
		System.arraycopy(labels, train_range[0], temp, 0, train_size);
		double[] train_y = convert(temp);
		SVMClassifier.scale(train_x,0,1);
		
		// prepare testing set
		
		double[][] test_x = convert(imgs.subList(test_range[0], test_range[1]));
		temp = new int[test_size];
		System.arraycopy(labels, test_range[0], temp, 0, test_size);
		double[] test_y = convert(temp);
		SVMClassifier.scale(test_x,0,1);
		
		
		// create or restore the trained svm
		SVMClassifier svm = null;
		String file = "mnist_svm_"+train_range[0]+"_"+train_range[1]+".svm";
		File f = new File(file);
		if(f.exists()) 
			svm = new SVMClassifier(file);
		else {
			svm = new SVMClassifier();
			svm.optimizeParams(train_x, train_y);
			svm.save(file);
		}
		
		// test accuracy
		List<int[]> errors = new ArrayList<int[]>();
		StringBuffer err_labels = new StringBuffer();
		int[][] confusion = new int[10][10];
		double accuracy = 0;
		for(int i=0; i<test_size;i++) {
			double c = svm.classify(test_x[i]);
			confusion[(int)c][(int)test_y[i]]++;
			if(c == test_y[i]) accuracy++;
			else {
				errors.add(imgs.get(test_range[0]+i));
				err_labels.append(","+(int)c);
			}
		}
		MNISTReader.createImg(err_labels.substring(1),errors);
		System.out.println("accuracy = "+(int)(100*accuracy/test_size)+"%");
		System.out.println("confusion matrix (class_results \\ groundtruth)");
		for(int i=0; i<confusion.length;i++) {
			for(int j=0; j<confusion[i].length;j++)
				System.out.print(confusion[i][j]+"\t");
			System.out.println();
		}
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
