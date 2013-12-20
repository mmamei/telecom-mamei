package classify;

public class Test {

	public static void main(String[] args) throws Exception {
		int problem = 2;
		svm_scale.main(new String[]{"-l","-1","-u","1","-s",problem+".scaling.parms","G:/DATASET/SVM/GUIDE/train."+problem,"train."+problem+".scaled"});
		double[] bestp = gridSerach("train."+problem+".scaled");
		svm_train.main(new String[]{"-c",""+bestp[0],"-g",""+bestp[1],"train."+problem+".scaled"});
		
		//svm_scale.main(new String[]{"-l","-1","-u","1","-r",problem+".scaling.parms","G:/DATASET/SVM/GUIDE/test."+problem,"test."+problem+".scaled"});
		//svm_predict.main(new String[]{"test."+problem+".scaled","train."+problem+".scaled.model",problem+".predict"});
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
