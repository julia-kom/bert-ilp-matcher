package bpm.ippm.similarity;

import edu.cmu.lti.ws4j.WS4J;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;
import org.apache.commons.text.similarity.LevenshteinDistance;

public class Word {

    /**
     * Overview of all implemented word similarity functions
     */
    public enum Similarities{
        LIN,
        JIANG,
        LEVENSHTEIN,
        LEVENSHTEIN_LIN_MAX,
        NOISY,
        LEVENSHTEIN_JIANG_MAX
    }

    /**
     * Compute Lin Similarity with wordnet via WS4J Lib
     * @param s1 Word 1
     * @param s2 Word 2
     * @return Similarity Score between 0 and 1
     */
    public static double LinSimilarity(String s1, String s2){
        WS4JConfiguration.getInstance().setMFS(false);
        if (s1.isEmpty() || s2.isEmpty()){
            return 0.0;
        }
        if(s1.equals(s2)){
            return 1.0;
        }else{
            return WS4J.runLIN(s1, s2);
        }
    }

    /**
     * Compute Jiang Similarity with wordnet via WS4J Lib
     * @param s1 Word 1
     * @param s2 Word 2
     * @return Similarity Score between 0 and 1
     */
    public static double JiangSimilarity(String s1, String s2){
        WS4JConfiguration.getInstance().setMFS(false);
        Double res = WS4J.runJCN(s1,s2);
        // in case that two words are exactly the same, we devide by 0, resulting in a score larger than 1
        if(res > 1.0){
            return 1.0;
        }
        return res;
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

    /**
     * Compute the Levenshtein Jiang Max Word Similarity
     * @param s1 Word 1
     * @param s2 Word 2
     * @return Similarity Score between 0 and 1
     */
    public static double LevenshteinJiangMaxSimialrity(String s1, String s2){
        return Math.max(LevenshteinSimilarity(s1,s2),JiangSimilarity(s1,s2));
    }

    /**
     * Compute the Levenshtein Lin Max Word Similarity
     * @param s1 Word 1
     * @param s2 Word 2
     * @return Similarity Score between 0 and 1
     */
    public static double LevenshteinLinMaxSimialrity(String s1, String s2){
        return Math.max(LevenshteinSimilarity(s1,s2),LinSimilarity(s1,s2));
    }
}
