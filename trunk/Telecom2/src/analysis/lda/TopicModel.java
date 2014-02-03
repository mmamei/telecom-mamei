package analysis.lda;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.regex.Pattern;

import utils.FileUtils;
import utils.Sort;
import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.LabelSequence;

public class TopicModel {
	
	static boolean TEST = false;
	
	public static final int numTopics = 3;
	public static final int numIterations = 50;
	
	
	static final DecimalFormat F = new DecimalFormat("#.##",new DecimalFormatSymbols(Locale.US));
    
	
    public static void main(String[] args) throws Exception {
    	
    	String file = FileUtils.getFileS("Topic")+"/ap.txt";
    	String stop = FileUtils.getFileS("Topic")+"/en.txt";
    	run(file,new String[]{stop});
    	System.exit(0);
    	
    	String user = "d5c63b3017f6a17ded9de49750b3297bfdedbc695f5b1853331a10639b7156";
    	process(CreateBagOfWords.city,CreateBagOfWords.bow.getClass().getSimpleName(),user);
    }
    
    
    public static void process(String city, String bow, String user) throws Exception {
    	String file = TEST ?  FileUtils.getFileS("Topic/test.txt") : FileUtils.getFileS("Topic/"+city+"_"+bow)+"/"+user+".txt";
    	String[] stop = getStopWords(file);
    	run(file,stop);
    }
    
    
    public static String[] getStopWords(String file) throws Exception {
    	List<String> stopw = new ArrayList<String>();
    	
    	BufferedReader br = new BufferedReader(new FileReader(new File(file)));
    	String line;
    	Map<String,Double> wc = new HashMap<String,Double>();
    	int n_docs = 0;
    	while((line=br.readLine())!=null) {
    		Set<String> w = new HashSet<String>();
    		String[] e = line.split("\t| ");
    		for(int i=2; i<e.length;i++)
    			w.add(e[i]);
    		for(String word : w)
    			wc.put(word, wc.get(word) == null ? 1 : 1+wc.get(word));
    		n_docs++;
    	}
    	
    	br.close();
    	
    	for(String w: wc.keySet()) {
       	 if(wc.get(w) < 3 || wc.get(w) / n_docs > 0.9) 
       		 stopw.add(w.toLowerCase());
        }
    	 
    	LinkedHashMap<String,Integer> o = Sort.sortHashMapByValuesD(wc,Collections.reverseOrder());
    	System.out.println("Word Histogram:");
    	for(String w: o.keySet())
    		System.out.print(w+" = "+o.get(w)+", ");
         System.out.println();
    	
         
         
         System.out.println("Stop Words = "+stopw.size());
         
    	String[] sw = new String[stopw.size()];
    	return stopw.toArray(sw);
    }
    
    
    
    public static void run(String file,String[] stop) throws Exception {

        // Begin by importing documents from text to feature sequences
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\S+")) );
        
        
        TokenSequenceRemoveStopwords sstop = new TokenSequenceRemoveStopwords(false,false);
        sstop.addStopWords(stop);
        pipeList.add(sstop);
        
        
        //if(stop != null) pipeList.add( new TokenSequenceRemoveStopwords(new File(stop[0]), "UTF-8", false, false, false) );
        
        pipeList.add( new TokenSequence2FeatureSequence() );

        InstanceList instances = new InstanceList (new SerialPipes(pipeList));
        
       
        
        Reader fileReader = new InputStreamReader(new FileInputStream(new File(file)), "UTF-8");
        instances.addThruPipe(new CsvIterator (fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
                                               3, 2, 1)); // data, label, name fields

        System.out.println("Num Documents = "+instances.size());
        Alphabet dataAlphabet = instances.getDataAlphabet();
        
        /*
        System.out.println("Word Histogram:");
       
        Map<String,Integer> wHist = new HashMap<String,Integer>();
        
        for(int i=0; i<instances.size();i++) {
        	FeatureSequence ts = (FeatureSequence) instances.get(i).getData(); 
        	for (int position = 0; position < ts.getLength(); position++) {
        		String w = (String) dataAlphabet.lookupObject(ts.getIndexAtPosition(position));
        		Integer c = wHist.get(w);
        		if(c == null) c = 0;
        		wHist.put(w,c+1);
        	}
        }
        
        LinkedHashMap<String,Integer> o = Sort.sortHashMapByValuesD(wHist,Collections.reverseOrder());
        for(String w: o.keySet())
        	System.out.print(w+" = "+o.get(w)+", ");
        
        System.out.println();
        */
        
        // Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
        //  Note that the first parameter is passed as the sum over topics, while
        //  the second is the parameter for a single dimension of the Dirichlet prior.
       
        ParallelTopicModel.logger.setLevel(Level.OFF);
        ParallelTopicModel model = new ParallelTopicModel(numTopics, 50/numTopics, 0.1);
        
        
        model.addInstances(instances);

        // Use two parallel samplers, which each look at one half the corpus and combine
        //  statistics after every iteration.
        model.setNumThreads(2);

        // Run the model for 50 iterations and stop (this is for testing only, 
        //  for real applications, use 1000 to 2000 iterations)
        model.setNumIterations(numIterations);
        model.estimate();
        
        System.out.println("Should be proportional to topic importance p(z) ??");
        System.out.print("ALPHA = "); 
        for(double a: model.alpha) 
        	System.out.print(F.format(a/model.alphaSum)+" ");
        System.out.println();
        
        //model.printTopWords(new File("G:/BASE/Topic/printTopWords.txt"),4,true);
        //model.printTopicWordWeights(new File("G:/BASE/Topic/printTopicWordWeights.txt")); 
        model.printDocumentTopics(new File("G:/BASE/Topic/printDocumentTopics.txt"));
        
        
        System.out.println("\n-----------------------------------------------\n");
        System.out.println("getTopicProbabilities (this is equvalent to printDocumentTopics)");
        System.out.println("p(z_j|d_i)   sum_j(p(z_j|d_i)) = 1");
        for(int i=0; i<instances.size();i++) {
        	double[] prob = model.getTopicProbabilities(i);
        	StringBuffer sb = new StringBuffer();
        	sb.append(instances.get(i).getName());
        	for(double p: prob) {
        		sb.append(","+F.format(p));
        	}
        	System.out.println(sb);
        }
        
        System.out.println("\n-----------------------------------------------\n");
        System.out.println("Probablity of topics. p(z_j) = sum_i(p(z_j|d_i))");
        double[] alpha2 = new double[model.numTopics];
        
        double alpha2_sum = 0;
        for(int i=0; i<instances.size();i++) {
        	double[] prob = model.getTopicProbabilities(i);
        	for(int j=0; j<alpha2.length;j++) {
        		alpha2[j] += prob[j]; 
        		alpha2_sum += prob[j]; // normalization
        	}	
        }
        System.out.print("ALPHA2 = ");
        for(double a: alpha2) 
        	System.out.print(F.format(a/alpha2_sum)+" ");
        
        
        System.out.println("\n-----------------------------------------------\n");
        System.out.println("getSortedWords (this is equvalent to printTopicWordWeights)");
        System.out.println("p(w_j|z_i)   sum_j(p(w_j|z_i)) = 1");
        
        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
        for(int i=0; i<model.numTopics;i++) {
        	System.out.print("topic "+i+": ");
        	TreeSet<IDSorter> words_x_topic = topicSortedWords.get(i);
        	
        	double sum = 0;
        	for(IDSorter ids: words_x_topic) 
        		sum += ids.getWeight();
        	
        	for(IDSorter ids: words_x_topic) {
        		String w = (String)dataAlphabet.lookupObject(ids.getID()); 
        		double p = ids.getWeight() / sum;
        		System.out.print(w+" = "+F.format(p)+", ");
        	}
        	System.out.println();
        }
        
        System.out.println("\n-----------------------------------------------\n");
        System.out.println("Show the words and topics in the document 0");
        // Show the words and topics in the first instance
        
        FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();
        LabelSequence topics = model.getData().get(0).topicSequence;
        
        for (int position = 0; position < tokens.getLength(); position++) {
        	System.out.print("("+dataAlphabet.lookupObject(tokens.getIndexAtPosition(position))+" -> "+topics.getIndexAtPosition(position)+")");
        }
        System.out.println();
        
        
        
       
        
        System.out.println("\n-----------------------------------------------\n");
        System.out.println("Create a new instance with high probability of topic 0");
        // Create a new instance with high probability of topic 0
        StringBuilder topicZeroText = new StringBuilder();
        Iterator<IDSorter> iterator = topicSortedWords.get(0).iterator();

        int rank = 0;
        while (iterator.hasNext() && rank < 5) {
            IDSorter idCountPair = iterator.next();
            topicZeroText.append(dataAlphabet.lookupObject(idCountPair.getID()) + " ");
            rank++;
        }
        
        System.out.println("doc = "+topicZeroText);
        System.out.println("p(topic_j|doc)");
        
        // Create a new instance named "test instance" with empty target and source fields.
        InstanceList testing = new InstanceList(instances.getPipe());
        testing.addThruPipe(new Instance(topicZeroText.toString(), null, "test instance", null));

        TopicInferencer inferencer = model.getInferencer();
        double[] testProbabilities = inferencer.getSampledDistribution(testing.get(0), 10, 1, 5);
        for(int i=0; i<model.numTopics;i++)
        	System.out.println(i+"\t" + testProbabilities[i]);
     
    }

}