package classify;


import java.io.IOException;

import libsvm.*;

public class SVMClassifier {
	
	private svm_parameter param;
	private svm_problem problem;
	private svm_model model;
	private int trainset_size = 0;
	
	public static boolean VERBOSE = false;
	
	
	public SVMClassifier() {
		param = new svm_parameter();

		// default values
		param.svm_type = svm_parameter.C_SVC;
		param.kernel_type = svm_parameter.RBF;
		param.degree = 3;
		param.gamma = 0.5;
		param.coef0 = 0;
		param.nu = 0.5;
		param.cache_size = 40;
		param.C = 1;
		param.eps = 1e-3;
		param.p = 0.1;
		param.shrinking = 1;
		param.probability = 0;
		param.nr_weight = 0;
		param.weight_label = new int[0];
		param.weight = new double[0];
		
		if(!VERBOSE)
		svm.svm_set_print_string_function(new svm_print_interface() { 
			public void print(String s) {
			}
		});
	}
	
	public void setParam(double C, double gamma) {
		param.C = C;
		param.gamma = gamma;
	}
	
	
	public SVMClassifier(String file) throws Exception {
		model = svm.svm_load_model(file);
	}
	
	
	public void optimizeParams(double[][] data, double[] labels) {
		double best, bestC = 0, bestGamma = 0;
		best = Double.MIN_VALUE;
		double cont = 0;
		for(int i=-10;i<11;i++) {
			cont ++;
			for(int j=-10;j<11;j++) {
				
				double C = Math.pow(2, i);
				double gamma = Math.pow(2, j);
				this.setParam(C, gamma);
				trainSVM(data,labels);
				
				
				double[] cross = new double[labels.length];
				
				for(int c=0; c<cross.length;c++)
					cross[c] = classify(data[c]);
				
				// run cross validation 
				//svm.svm_cross_validation(problem, param, 5, cross);
				
				double correct = 0;
				for(int e=0; e<problem.l;e++)
					if(cross[e] == problem.y[e])
						correct++;
				double accuracy = correct/cross.length;
				if(accuracy > best) {
					best = accuracy;
					bestC = C;
					bestGamma = gamma;
				}
			}
			System.out.println((int)(100.0*cont/21.0)+"% completed.... best accuracy so far = "+best);
		}
		
		System.out.println("Best Accuracy = "+best);
		System.out.println("Best Params (C,gamma) = ("+bestC+","+bestGamma+")");
		this.setParam(bestC, bestGamma);
		trainSVM(data,labels);
	}

	
	
	public void trainSVM(double[][] data, double[] labels) {
		// build problem
		trainset_size = labels.length;
		problem = new svm_problem();
		problem.l = labels.length;
		problem.y = labels;
		problem.x = new svm_node[problem.l][];
		
		for(int i=0;i<problem.l;i++) 
			problem.x[i] = cast2svm(data[i]);
		
		model = svm.svm_train(problem, param);
	}
	
	public void save(String file) {
		try {
			svm.svm_save_model(file, model);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public double classify(double[] data) {
		svm_node[] x = cast2svm(data);
		return svm.svm_predict(model, x);
	}
	
	
	
	private svm_node[] cast2svm(double [] x) {
		svm_node[] y = new svm_node[x.length];
		for(int i=0; i<x.length;i++) {
			y[i] = new svm_node();
			y[i].index = i+1;
			y[i].value = x[i];
		}
		return y;
	}
	
	public static void main(String[] args) {
		//double[] labels = new double[]{1,-1,1,-1};
		//double[][] data = new double[][]{{1,0,1},{-1,0,-1},{1,1,1},{-1,-1,-1}};
		
		
		double[] labels = new double[]{1,2,3,4,5,6,7,8,9};
		double[][] data = new double[][]{ {1,0,0,0,0,0,0,0,0},
									   {0,1,0,0,0,0,0,0,0},
									   {0,0,1,0,0,0,0,0,0},
									   {0,0,0,1,0,0,0,0,0},
									   {0,0,0,0,1,0,0,0,0},
									   {0,0,0,0,0,1,0,0,0},
									   {0,0,0,0,0,0,1,0,0},
									   {0,0,0,0,0,0,0,1,0},
									   {0,0,0,0,0,0,0,0,1}
		};
		
		
		SVMClassifier test = new SVMClassifier();
		//test.optimizeParams(data, labels);
		test.trainSVM(data, labels);
		
		/*
		double[] cross = test.crossValidation(3);
		for(int i=0;i<cross.length;i++)
			System.out.println(cross[i]);
		*/
		
		
		double clazz = test.classify(new double[]{0,0,0,0,0,0,1,0,0});
		System.out.println(clazz);
	}
}
