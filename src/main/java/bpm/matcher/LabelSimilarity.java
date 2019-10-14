package bpm.matcher;

import edu.cmu.lti.ws4j.WS4J;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.*;


public class LabelSimilarity {


    /**
     * Implementation of the Basic Bag-of Word Similarity with Lev-Lin Max between two strings according to
     * "Increasing Recall of Process Model Matching by Improved Activity Label Matching" (2014)
     * @param label1 first string
     * @param label2 second string
     * @return
     */
    public double BagOfWordSim(String label1, String label2){

        //tokenizaiton
        BagOfWords bag1 = new BagOfWords(label1);
        BagOfWords bag2 = new BagOfWords(label2);

        //stop word removal
        bag1.removeStopWords();
        bag2.removeStopWords();

        //max computation word1
        double sum1 = 0;
        double max1;
        for(int i = 0; i<bag1.size(); i++){
            max1 = 0;
            for(int j = 0; j < bag2.size(); j++){
                // calculate lin
                double lin;
                if(bag1.at(i).equals(bag2.at(j))){
                    lin = 1;
                }else{
                    lin = LinWordSim(bag1.at(i),bag2.at(j));
                }
                //calculate lev
                double lev = LevenshteinWordSim(bag1.at(i),bag2.at(j));
                //get max of both
                double tmp = Math.max(lin,lev);
                if (max1 < tmp){
                    max1 =  tmp;
                }
            }
            sum1+=max1;
        }

        //max computation word2
        double sum2 = 0;
        double max2;
        for(int i = 0; i<bag2.size(); i++){
            max2 = 0;
            for(int j = 0; j < bag1.size(); j++){
                double lin = 0;
                if(bag2.at(i).equals(bag1.at(j))){
                    lin = 1;
                }else{
                    lin = LinWordSim(bag2.at(i),bag1.at(j));
                }
                double lev = LevenshteinWordSim(bag2.at(i),bag1.at(j));
                double tmp = Math.max(lin,lev);
                if (max2 < tmp){
                    max2 =  tmp;
                }
            }
            sum2+=max2;
        }

        System.out.println("A: " + label1 + ", B: " + label2 + ":" +(sum1+sum2)/(bag1.size() + bag2.size()));
        //aggregate
        return (sum1+sum2)/(bag1.size() + bag2.size());
    }

    /**
     * Compute Lin Similarity with wordnet via WS4J Lib
     * @param s1 Word 1
     * @param s2 Word 2
     * @return Similarity Score between 0 and 1
     */
    public static double LinWordSim(String s1, String s2){
        WS4JConfiguration.getInstance().setMFS(false);
        return WS4J.runLIN(s1,s2);
    }

    /**
     * Compute Jiang Similarity with wordnet via WS4J Lib
     * @param s1 Word 1
     * @param s2 Word 2
     * @return Similarity Score between 0 and 1
     */
    public static double JiangWordSim(String s1, String s2){
        WS4JConfiguration.getInstance().setMFS(false);
        return WS4J.runJCN(s1,s2);
    }


    /**
     * Compute Levenshtein Similarity with help of apache commons text Lib
     * @param s1 Word 1
     * @param s2 Word 2
     * @return Similarity Score between 0 and 1
     */
    public static double LevenshteinWordSim(String s1, String s2){
        // get levenshtein distance
        LevenshteinDistance lev = LevenshteinDistance.getDefaultInstance();
        double dist = lev.apply(s1,s2);

        // convert to similarity by normalizing and inverting
        return 1.0 - (dist / Math.max(s2.length(),s1.length()));
    }

    private class BagOfWords{
        List<String> words;

        private BagOfWords(String label){
            HashSet<String> set = new HashSet<>(Arrays.asList(label.split("\\s+")));
            words = new ArrayList<String>(set);
        }

        private String at(int i){
            return words.get(i);
        }

        private int size(){
            return words.size();
        }

        private void removeStopWords(){
            // todo
        }
    }
}






