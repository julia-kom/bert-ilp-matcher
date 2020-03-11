package bpm.ippm.similarity;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * This Class is for evaluation purpose only!
 * Same labeled transitions do not get a label sim score of one. Instead:
 *
 * If same labeled => draw similarity from matching-Gaussian
 * If differently labeled => draw similarity from non matching Gaussian
 */
public class NormalDistributionLabelSimilarity implements LabelSimilarity {

    private double nonMatchingMean = 0.0;
    private double nonMatchingVariance = 0.35;

    private double matchingMean = 1.0;
    private double matchingVariance = 0.35;

    private static final File CACHE = new File("cache.json");
    private HashMap<String, Double> cache;
    private NormalDistribution nonMatchingND = new NormalDistribution(nonMatchingMean,nonMatchingVariance);
    private NormalDistribution matchingND = new NormalDistribution(matchingMean,matchingVariance);


    /**
     * Set the Gaussian for non matching pairs of transitions
     * @param mean Mean
     * @param variance Variance
     */
    public void setNonMatchingDistribution(double mean, double variance) {
        nonMatchingMean = mean;
        nonMatchingVariance = variance;

        nonMatchingND = new NormalDistribution(nonMatchingMean,nonMatchingVariance);
    }

    /**
     * Set the gaussian for matching pairs of transition labels.
     * @param mean Mean
     * @param variance Variance
     */
    public void setMatchingDistribution(double mean, double variance) {
        matchingMean = mean;
        matchingVariance = variance;

        matchingND = new NormalDistribution(matchingMean,matchingVariance);
    }

    /**
     * Compute the similarity of two labels.
     * To gain consistency over several iterations we use caching for once drawn similarities.
     * Those are stored in ./
     * @param label1
     * @param label2
     * @return
     */
    @Override
    public double sim(String label1, String label2) {

        //get cached first
        try {
            if (checkCache(label1, label2) != null) {
                return checkCache(label1, label2);
            }
        } catch (Exception e) {
            System.err.println("It was not possible to read from cache, due to unexpected exception: " + e.toString());
        }

        //if non in cache compute:
        double sim;
        if(label1.equals(label2)){
            sim = matchingND.sample();
        }else{
            sim = nonMatchingND.sample();
        }

        //cut to [0,1] by rounding >1 to 1 and <0 to 0
        if(sim > 1 ){
            sim = sim(label1, label2);
        }

        if(sim < 0){
            sim = sim(label1, label2);
        }

        //update cache
        try {
            updateCache(label1, label2, sim);
        }catch(Exception e){
            System.err.println("it was not possible to update the cache. Continue without cache. Reason: " + e.toString());
        }

        return sim;
    }



    /**
     * return the cached value of label1 vs label2
     * this is symmetric and stores over several executions
     * USES ; AS DELIMITER!
     * @param label1
     * @param label2
     * @return
     */
    private Double checkCache(String label1, String label2) throws ParseException, IOException {
        //load cache if not yet done
        if(cache == null){
           if(!CACHE.exists()){
               cache = new HashMap<>();
           }else{
               cache = new HashMap<>();
               FileReader read = new FileReader(CACHE);
               JSONParser parser = new JSONParser();
               JSONObject list = (JSONObject) parser.parse(read);
               Set keySet = list.keySet();
               Iterator<String> it = keySet.iterator();
               while (it.hasNext()){
                   String key = it.next();
                   double val = (Double)list.get(key);
                   cache.put(key,val);
               }
           }
        }

        // check label 1 vs label 2
        String labels = label1+";"+label2;
        if(cache.containsKey(labels)){
            return cache.get(labels);
        }


        // check label 2 vs label 1
        labels = label2+";"+label1;
        if(cache.containsKey(labels)){
            return cache.get(labels);
        }

        // return null if not exist in cache
        return null;

    }

    /**
     * Write to cache map and to json file.
     * @param label1
     * @param label2
     * @param sim
     * @throws IOException
     */
    private void updateCache(String label1, String label2, double sim) throws IOException {
        //update cache and store
        cache.put(label1+";"+label2,sim);

        JSONObject list = new JSONObject();
        for( String labels : cache.keySet()){
            sim = cache.get(labels);
            list.put(labels,sim);
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(CACHE));
        writer.write(list.toString());
        writer.close();
    }

}
