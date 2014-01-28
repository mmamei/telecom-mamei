package analysis.lda;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.regex.Pattern;

import utils.FileUtils;
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

    public static void main(String[] args) throws Exception {
    	//String file = FileUtils.getFileS("Topic")+"/ap.txt";
    	//String stop = FileUtils.getFileS("Topic")+"/en.txt";
    	
    	String user = "0a8a5cacbd29717cf1cf2a3e2b51b7457ad62b186d38c66fa9bbd4c348364";
    	String file = FileUtils.getFileS("Topic/Venezia")+"/"+user+".txt";
    	String stop = null;
    	run(file,stop);
    }
    
    
    
    public static void run(String file,String stop) throws Exception {

        // Begin by importing documents from text to feature sequences
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add( new CharSequenceLowercase() );
        pipeList.add( new CharSequence2TokenSequence(Pattern.compile("\\S+")) );
        if(stop != null) pipeList.add( new TokenSequenceRemoveStopwords(new File(stop), "UTF-8", false, false, false) );
        pipeList.add( new TokenSequence2FeatureSequence() );

        InstanceList instances = new InstanceList (new SerialPipes(pipeList));
        
       
        
        Reader fileReader = new InputStreamReader(new FileInputStream(new File(file)), "UTF-8");
        instances.addThruPipe(new CsvIterator (fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
                                               3, 2, 1)); // data, label, name fields

        System.out.println("Num Days = "+instances.size());
        
        // Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
        //  Note that the first parameter is passed as the sum over topics, while
        //  the second is the parameter for a single dimension of the Dirichlet prior.
        int numTopics = 10;
        ParallelTopicModel.logger.setLevel(Level.OFF);
        ParallelTopicModel model = new ParallelTopicModel(numTopics, 50/numTopics, 0.01);
        
        
        model.addInstances(instances);

        // Use two parallel samplers, which each look at one half the corpus and combine
        //  statistics after every iteration.
        model.setNumThreads(2);

        // Run the model for 50 iterations and stop (this is for testing only, 
        //  for real applications, use 1000 to 2000 iterations)
        model.setNumIterations(500);
        model.estimate();
        
        System.out.println("Should be proportional to topic importance p(z) ??");
        System.out.print("ALPHA = ");
        double s = 0;
        for(double a: model.alpha) {
        	System.out.print(a/model.alphaSum+" ");
        	s += a;
        }
        
        model.printTopWords(new File("G:/BASE/Topic/printTopWords.txt"),4,true);
        model.printTopicWordWeights(new File("G:/BASE/Topic/printTopicWordWeights.txt")); 
        model.printDocumentTopics(new File("G:/BASE/Topic/printDocumentTopics.txt"));
        
        DecimalFormat F = new DecimalFormat("#.##",new DecimalFormatSymbols(Locale.US));
        
        System.out.println("\n-----------------------------------------------\n");
        System.out.println("getTopicProbabilities (this is equvalent to printDocumentTopics)");
        System.out.println("p(z_j|d_i)   sum_j(p(z_j|d_i)) = 1");
        for(int i=0; i<instances.size();i++) {
        	double[] prob = model.getTopicProbabilities(i);
        	System.out.print("document "+i+": ");
        	for(double p: prob) {
        		System.out.print(F.format(p)+", ");
        	}
        	System.out.println();
        }
        
        System.out.println("\n-----------------------------------------------\n");
        System.out.println("getSortedWords (this is equvalent to printTopicWordWeights)");
        System.out.println("p(w_j|z_i)   sum_j(p(w_j|z_i)) = 1");
        Alphabet dataAlphabet = instances.getDataAlphabet();
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