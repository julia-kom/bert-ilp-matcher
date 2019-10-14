package bpm.similarity;

import edu.cmu.lti.ws4j.WS4J;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import org.apache.commons.text.similarity.LevenshteinDistance;

public class Word {

    /**
     * Compute Lin Similarity with wordnet via WS4J Lib
     * @param s1 Word 1
     * @param s2 Word 2
     * @return Similarity Score between 0 and 1
     */
    public static double LinSimilarity(String s1, String s2){
        WS4JConfiguration.getInstance().setMFS(false);
        return WS4J.runLIN(s1,s2);
    }

    /**
     * Compute Jiang Similarity with wordnet via WS4J Lib
     * @param s1 Word 1
     * @param s2 Word 2
     * @return Similarity Score between 0 and 1
     */
    public static double JiangSimilarity(String s1, String s2){
        WS4JConfiguration.getInstance().setMFS(false);
        return WS4J.runJCN(s1,s2);
    }


    /**
     * Compute Levenshtein Similarity with help of apache commons text Lib
     * @param s1 Word 1
     * @param s2 Word 2
     * @return Similarity Score between 0 and 1
     */
    public static double LevenshteinSimilarity(String s1, String s2){
        // get levenshtein distance
        LevenshteinDistance lev = LevenshteinDistance.getDefaultInstance();
        double dist = lev.apply(s1,s2);

        // convert to similarity by normalizing and inverting
        return 1.0 - (dist / Math.max(s2.length(),s1.length()));
    }
}
