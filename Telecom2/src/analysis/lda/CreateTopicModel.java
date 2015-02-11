package analysis.lda;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.regex.Pattern;

import region.RegionMap;
import utils.Config;
import utils.CopyAndSerializationUtils;
import utils.Sort;
import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Alphabet;
import cc.mallet.types.IDSorter;
import cc.mallet.types.InstanceList;

public class CreateTopicModel {
	
	public static final boolean VERBOSE = false;
	
	public static final int numTopics = 6;
	public static final int burnin = 10000;
	public static final int numIterations = 500;
	public static final double alpha = 100; // the higher the more topic per document (1 unifrom distrib)
	public static final double beta = 0.01; // the higher the more word per topic (1 uniform distrib)
	
	
	static RegionMap RM = (RegionMap)CopyAndSerializationUtils.restore(new File(Config.getInstance().base_folder+"/RegionMap/TorinoArea.ser"));
	
	static final DecimalFormat F = new DecimalFormat("#.##",new DecimalFormatSymbols(Locale.US));
    
	// single thread main
	/*
    public static void main(String[] args) throws Exception {
    	
    	//String user = "1d8b3e9f864579645d3d7e165f956681fc5c4deb345d1145a2e93cee387d91f5";
    	
    	File maind = new File(Config.getInstance().base_folder+"/Topic");
		for(File d: maind.listFiles()) {
			System.out.println("Processing user "+d.getName()+" ...");
			processUser(d.getName());
		}
    	
    	System.out.println("Done!");
    }
    */
    
    // multi-thread main
    public static void main(String[] args) throws Exception {
    	File maind = new File(Config.getInstance().base_folder+"/Topic");
    	File[] files = maind.listFiles();
    	
    	int total_size = files.length;
		int n_thread = 8;
		int size = total_size / n_thread;
		Worker[] w = new Worker[n_thread];
		for(int t = 0; t < n_thread;t++) {
			int start = t*size;
			int end = t == (n_thread-1) ? total_size : (t+1)*size;
			w[t] = new Worker(files,start,end);		
		}
		
		for(int t = 0; t < n_thread;t++) 
			w[t].start();

		for(int t = 0; t < n_thread;t++) 
			w[t].join();
		
		System.out.println("All thread completed!");
    }
    
    
    public static void processUser(String user) {
    	System.out.println("Processing "+user);
    	try {
    		File file = new File(Config.getInstance().base_folder+"/Topic/"+user+"/"+user+".txt");
    		//if(file.exists()) return;
	    	String[] stop = new String[1]; //getStopWords(file);
	    	run(file,stop);
    	}catch(Exception e) {
    		System.out.println("ERROR in user: "+user);
    		e.printStackTrace();
    	}
    }
    
    
    private static String[] getStopWords(File file) throws Exception {
    	List<String> stopw = new ArrayList<String>();
    	BufferedReader br = new BufferedReader(new FileReader(file));
    	
    	Map<String,Double> wc = new HashMap<String,Double>();
    	int n_docs = 0;
    	
    	String line;
    	while((line=br.readLine())!=null) {
    		Set<String> w = new HashSet<String>();
    		String[] e = line.split("\t| ");
    		for(int i=2; i<e.length;i++)
    			w.add(e[i].substring(2));
    		for(String word : w)
    			wc.put(word, wc.get(word) == null ? 1 : 1+wc.get(word));
    		n_docs++;
    	}
    	
    	br.close();
    	
    	for(String w: wc.keySet()) {
    		double f = wc.get(w) / n_docs;
       	 	if(f < 0.01 || f > 0.9)
       	 		stopw.add(w.toLowerCase());
        }
    	
    	
    	if(VERBOSE) {
    		LinkedHashMap<String,Integer> o = Sort.sortHashMapByValuesD(wc,Collections.reverseOrder());
    		System.out.println("Word Histogram:");
    		for(String w: o.keySet())
    			System.out.print(w+" = "+o.get(w)+", ");
    		System.out.println();
    		System.out.println("Stop Words = "+stopw.size());
    	}
    	
    	return stopw.toArray(new String[stopw.size()]);
    }
    
    
    
    public static void run(File file,String[] stop) throws Exception {
        // Begin by importing documents from text to feature sequences
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\S+")) );
        TokenSequenceRemoveStopwords sstop = new TokenSequenceRemoveStopwords(false,false);
        sstop.addStopWords(stop);
        pipeList.add(sstop);
        
        pipeList.add( new TokenSequence2FeatureSequence() );
        InstanceList instances = new InstanceList (new SerialPipes(pipeList));
        Reader fileReader = new InputStreamReader(new FileInputStream(file), "UTF-8");
        instances.addThruPipe(new CsvIterator (fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"), 3, 2, 1)); // data, label, name fields

        if(VERBOSE) System.out.println("Num Documents = "+instances.size());
        Alphabet dataAlphabet = instances.getDataAlphabet();
        
          
        if(!VERBOSE) ParallelTopicModel.logger.setLevel(Level.OFF);
        ParallelTopicModel model = new ParallelTopicModel(numTopics,alpha,beta);
        model.addInstances(instances);
        model.setBurninPeriod(burnin);
        model.setNumThreads(2);
        model.setNumIterations(numIterations);
        model.estimate();
        
        if(VERBOSE) {
	        System.out.println("Should be proportional to topic importance p(z) ??");
	        System.out.print("ALPHA = "); 
	        for(double a: model.alpha) 
	        	System.out.print(F.format(a/model.alphaSum)+" ");
	        System.out.println();
        }
        
        
        Set<String> docs = new HashSet<String>(); // useful to avoid repetitions
        
     
 
        PrintWriter out = new PrintWriter(new FileWriter(new File(file.getParent()+"/p_z_d.txt")));
        
        
        if(VERBOSE) {
	        System.out.println("\n-----------------------------------------------\n");
	        System.out.println("getTopicProbabilities (this is equvalent to printDocumentTopics)");
	        System.out.println("p(z_j|d_i)   sum_j(p(z_j|d_i)) = 1");
        }
        for(int i=0; i<instances.size();i++) {
        	double[] prob = model.getTopicProbabilities(i);
        	String doc = (String)instances.get(i).getName();
        	if(docs.contains(doc)) continue;
        	docs.add(doc);
        	StringBuffer sb = new StringBuffer(doc);
        	for(double p: prob) 
        		sb.append(","+F.format(p));
        	if(VERBOSE)System.out.println(sb);
        	out.println(sb);
        }
        out.close();
        
        
        
        if(VERBOSE) {
	        System.out.println("\n-----------------------------------------------\n");
	        System.out.println("getSortedWords (this is equvalent to printTopicWordWeights)");
	        System.out.println("p(w_j|z_i)   sum_j(p(w_j|z_i)) = 1");
        }
        
        out = new PrintWriter(new FileWriter(new File(file.getParent()+"/p_w_z.txt")));
        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
        for(int i=0; i<model.numTopics;i++) {
        	if(VERBOSE) System.out.print("Topic"+i);
        	out.print("Topic_"+i);
        	TreeSet<IDSorter> words_x_topic = topicSortedWords.get(i);
        	
        	double sum = 0;
        	for(IDSorter ids: words_x_topic) 
        		sum += ids.getWeight();
        	
        	for(IDSorter ids: words_x_topic) {
        		String w = (String)dataAlphabet.lookupObject(ids.getID()); 
        		double p = ids.getWeight() / sum;
        		if(VERBOSE) System.out.print(", "+w+" = "+F.format(p));
        		out.print(","+w+","+F.format(p));
        	}
        	if(VERBOSE) System.out.println();
        	out.println();
        }
        
        out.close();
    }
}


class Worker extends Thread {
	File[] files;
	int start;
	int end;
	
	Worker(File[] files, int start, int end) {
		this.files = files;
		this.start = start;
		this.end = end;
	}
	
	public void run() {
		System.out.println("Thread "+start+"-"+end+" starting!");
		for(int i=start;i<end;i++) {
			try {
				File d = files[i];
				CreateTopicModel.processUser(d.getName());
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println("Thread "+start+"-"+end+" completed!");
	}
	
}