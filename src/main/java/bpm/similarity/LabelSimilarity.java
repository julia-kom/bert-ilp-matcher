package bpm.similarity;

import org.apache.commons.lang3.NotImplementedException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.net.URISyntaxException;
import java.util.*;
import javax.xml.parsers.*;
import java.io.*;

import static java.lang.System.exit;

public class LabelSimilarity {

    private Word.Similarities wordSimilarity;

    public static final HashSet<String> STOPWORDS = loadStopWords();

    /**
     * Construct a Label Similarity Function with specific word similarity measure.
     * @param wordSimilarity
     */
    public LabelSimilarity(Word.Similarities wordSimilarity){
        this.wordSimilarity = wordSimilarity;
    }

    /**
     * Implementation of the Basic Bag-of Word Similarity with simFunction as word similarity between two strings according to
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
                // calculate similarity
                double tmp = wordSimilarity(bag1.at(i),bag2.at(j));
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
                double tmp = wordSimilarity(bag2.at(i),bag1.at(j));
                if (max2 < tmp){
                    max2 =  tmp;
                }
            }
            sum2+=max2;
        }

        //if(PRINT_ENABLED) System.out.println("A: " + label1 + ", B: " + label2 + ":" +(sum1+sum2)/(bag1.size() + bag2.size()));
        //aggregate
        return (sum1+sum2)/(bag1.size() + bag2.size());
    }

    private static HashSet<String> loadStopWords(){
        // Create HashSet
        HashSet<String> set = new HashSet<>();

        // Get File Path
       // File file = new File(LabelSimilarity.class.getClassLoader().getFile("stopwords.xml").getFile());

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream("stopwords.xml");


        // Create Builder
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        // Parse XML File
        Document document = null;
        try {
            document = builder.parse(is);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Element root = document.getDocumentElement();
        NodeList stopWord = root.getElementsByTagName("StopWord");

        // Fill the HashSet
        for(int i = 0; i < stopWord.getLength(); i++)
            set.add(stopWord.item(i).getTextContent());

        return set;
    }

    private class BagOfWords{
        List<String> words;

        /**
         * Create a Bag of Words.
         * Duplicated Words are only regarded once.
         * Case insensitive
         * @param label the label of a node
         */
        private BagOfWords(String label){
            HashSet<String> set = new HashSet<>(Arrays.asList(label.split("\\s+")));
            words = new ArrayList<String>(set);
            for (int i = 0; i< words.size(); i++){
                words.set(i,words.get(i).toLowerCase());
            }
        }

        /**
         * Get word at position i in the bag for iteration
         * @param i
         * @return
         */
        private String at(int i){
            return words.get(i);
        }

        /**
         * Get number of words inside the bag
          * @return
         */
        private int size(){
            return words.size();
        }

        /**
         * Remove Stop Words which are defined in the stopword file from resources.
         * Stopwords are taken from "Increasing Recall of Process Model Matching by Improved Activity Label Matching" (2014), for comparability reasons.
         */
        private void removeStopWords(){
            //iterate through all words and delete those which are in the stopword hashmap
            Iterator<String> it = words.listIterator();
            String w;
            while(it.hasNext()){
                w = it.next();
                if (STOPWORDS.contains(w)){
                    it.remove();
                    //if(PRINT_ENABLED) System.out.println("Stopword Removed: " + w);
                }
            }
        }
    }

    private double wordSimilarity(String s1, String s2){
        switch(wordSimilarity){
            case LIN:
                return Word.LinSimilarity(s1,s2);
            case JIANG:
                return Word.JiangSimilarity(s1,s2);
            case LEVENSHTEIN:
                return Word.LevenshteinSimilarity(s1,s2);
            case LEVENSHTEIN_JIANG_MAX:
                return Word.LevenshteinJiangMaxSimialrity(s1,s2);
            case LEVENSHTEIN_LIN_MAX:
                return Word.LevenshteinLinMaxSimialrity(s1,s2);
                default:
                    throw new NotImplementedException("Word Similarity Function is not in switch.");
        }
    }
}