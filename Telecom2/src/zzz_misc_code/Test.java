package zzz_misc_code;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import utils.Config;

public class Test {
	
	static String[] arr = new String[]{"A","B","C","D","E","F","G","H","I","L","M"};
	static List<String> l = new ArrayList<String>();
	static File f = new File(Config.getInstance().base_folder+"/test.txt");
	static PrintWriter out = null;
	
	public static void main(String[] args) throws Exception {
		int N_THREAD = 3;
	
		out = new PrintWriter(new FileWriter(f));
		
		
		int size = arr.length / N_THREAD;
		Worker[] w = new Worker[N_THREAD];
		for(int t = 0; t < N_THREAD;t++) {
			int start = t*size;
			int end = t == (N_THREAD-1) ? arr.length : (t+1)*size;
			w[t] = new Worker(arr,start,end);		
		}
		
		for(int t = 0; t < N_THREAD;t++) 
			w[t].start();

		for(int t = 0; t < N_THREAD;t++) 
			w[t].join();
		
		System.out.println("All thread completed!");
		
		out.close();
		
		for(String x: l) 
			System.out.println(x);
	}
	
	public static synchronized void process(String x) {
		l.add(x);
		out.print("-- ");
		out.print(x);
		try {
			Thread.currentThread().sleep(10);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		out.println(" --");
	}
	

}

class Worker extends Thread {
	String[] arr;
	int start;
	int end;
	Worker(String[] arr,int start,int end) {
		this.arr = arr;
		this.start = start;
		this.end = end;
	}
	public void run() {
		System.out.println("Thread "+start+"-"+end+" starting!");
		for(int i=start;i<end;i++) {
			Test.process(arr[i]);
		}
		System.out.println("Thread "+start+"-"+end+" completed!");
	}
}

