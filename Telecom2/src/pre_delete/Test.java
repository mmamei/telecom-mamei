package pre_delete;

import java.io.File;

import utils.FileUtils;

public class Test {
	
	
	static final int problem = 3;
	
	public static void main(String[] args) throws Exception {
		
		String d = FileUtils.create("SVM").getAbsolutePath();
		
		svm_scale.main(new String[]{"-l","-1","-u","1","-s",d+"/"+problem+".scaling.parms","G:/DATASET/SVM/GUIDE/train."+problem,d+"/train."+problem+".scaled"});
		double[] bestp = gridSerach(d+"/train."+problem+".scaled");
		svm_train.main(new String[]{"-c",""+bestp[0],"-g",""+bestp[1],d+"/train."+problem+".scaled",d+"/train."+problem+".scaled.model"});
		
		svm_scale.main(new String[]{"-l","-1","-u","1","-r",d+"/"+problem+".scaling.parms","G:/DATASET/SVM/GUIDE/test."+problem,d+"/test."+problem+".scaled"});
		svm_predict.main(new String[]{d+"/test."+problem+".scaled",d+"/train."+problem+".scaled.model",d+"/"+problem+".predict"});
	}
	
	
	
	public static double[] gridSerach(String file) throws Exception {
		
		double[] best = new double[2];
		double best_acc = 0;
		
		for(int i=-5;i<=5;i++) 
		for(int j=-5;j<=5;j++) {
			double C = Math.pow(2, i);
			double gamma = Math.pow(2, j);
			double acc = svm_train.main2(new String[]{"-q","-v","5","-c",""+C,"-g",""+gamma,file});
			if(acc > best_acc){
				best_acc = acc;
				best[0] = C;
				best[1] = gamma;
			}
		}
		
		System.out.println("best accuracy = "+best_acc);
		System.out.println("best C = "+best[0]+" best gamma = "+best[1]);
		
		return best;
	}
}
