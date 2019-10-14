package bpm.similarity;

import java.util.*;


public class LabelSimilarity {


    /**
     * Implementation of the Basic Bag-of Word Similarity with Lev-Lin Max between two strings according to
     * "Increasing Recall of Process Model Matching by Improved Activity Label Matching" (2014)
     * @param label1 first string
     * @param label2 second string
     * @return
     */
    public double BagOfWords(String label1, String label2){

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
                    lin = Word.LinSimilarity(bag1.at(i),bag2.at(j));
                }
                //calculate lev
                double lev = Word.LevenshteinSimilarity(bag1.at(i),bag2.at(j));
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
                    lin = Word.LinSimilarity(bag2.at(i),bag1.at(j));
                }
                double lev = Word.LevenshteinSimilarity(bag2.at(i),bag1.at(j));
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






