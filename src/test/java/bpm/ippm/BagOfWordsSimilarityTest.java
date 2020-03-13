package bpm.ippm;

import bpm.ippm.similarity.BagOfWordsSimilarity;
import bpm.ippm.similarity.Matrix;
import bpm.ippm.similarity.Word;
import org.jbpt.petri.Transition;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Unit test
 */
public class BagOfWordsSimilarityTest
{
    /**
     * Test Word sim
     */
    @Test
    public void wordSimilarityTests(){
        System.out.println();
        Assert.assertTrue(Word.LinSimilarity("house", "lodge") >= 0.9);
        Assert.assertTrue(Word.LinSimilarity("house","car") <1.0);
        Assert.assertTrue(Word.JiangSimilarity("house","lodge") >= 0.75);
        Assert.assertTrue(Word.JiangSimilarity("house","car") <= 1.0);
        Assert.assertTrue(Word.LevenshteinSimilarity("house","house") == 1.0);
        Assert.assertTrue(Word.LevenshteinSimilarity("house","houte") == 0.8);
    }

    /**
     * Test bag of words
     */
    @Test
    public void bagOfWordsTests(){
        // Order test
        String s1 = "house mouse klaus";
        String s2 = "klaus house mouse";
        BagOfWordsSimilarity sim = new BagOfWordsSimilarity(Word.Similarities.LEVENSHTEIN_LIN_MAX);
        Assert.assertTrue(sim.sim(s1,s2) == 1);

        // Lin-Lev-Max Similarity Bag of Words Test
        double val1Lin = Word.LinSimilarity("house","lodge");
        double val2Lin = Word.LinSimilarity("house","car");
        double val3Lin = Word.LinSimilarity("house","elephant");
        double val4Lin = Word.LinSimilarity("lion","lodge");
        double val5Lin = Word.LinSimilarity("lion","car");
        double val6Lin = Word.LinSimilarity("lion","elephant");

        double val1Lev = Word.LevenshteinSimilarity("house","lodge");
        double val2Lev = Word.LevenshteinSimilarity("house","car");
        double val3Lev = Word.LevenshteinSimilarity("house","elephant");
        double val4Lev = Word.LevenshteinSimilarity("lion","lodge");
        double val5Lev = Word.LevenshteinSimilarity("lion","car");
        double val6Lev = Word.LevenshteinSimilarity("lion","elephant");

        double bow  = max(val1Lev,val1Lin,val2Lev,val2Lin,val3Lev,val3Lin); //house
        bow += max(val4Lev,val4Lin,val5Lev,val5Lin,val6Lev,val6Lin); // lion
        bow += max(val1Lev,val1Lin,val4Lev,val4Lin); // lodge
        bow += max(val2Lev,val2Lin,val5Lev,val5Lin); // car
        bow += max(val3Lev, val3Lin, val6Lev,val6Lin); //elephant
        bow = bow / (2+3);
        Assert.assertTrue(Math.abs(sim.sim("house lion", "lodge car elephant") - bow) <0.01);

    }

    private static double max(Double... vals) {
        return Collections.max(Arrays.asList(vals));
    }

    /**
     * Check Similarity Matrix
     */
    @Test
    public void matrixTests(){
        // Create two sets of nodes
        String[] s1 = new String[]{"Car", "House", "Child"};
        String[] s2 = new String[]{"Bus", "Flat", "Cat"};
        Set<Transition> n1 = new HashSet<>();
        for (String s : s1){
            n1.add(new Transition(s));
        }
        Set<Transition> n2 = new HashSet<>();
        for (String s : s2){
            n2.add(new Transition(s));
        }

        //Compute the matrix
        Matrix matrix = new Matrix.Builder().withWordSimilarity(Word.Similarities.LEVENSHTEIN_LIN_MAX).build(n1,n2);

        // Label Simialarity
        BagOfWordsSimilarity sim = new BagOfWordsSimilarity(Word.Similarities.LEVENSHTEIN_LIN_MAX);

        for (Transition t1 : n1){
            for (Transition t2 : n2){
                Assert.assertTrue(matrix.between(t1,t2) == sim.sim(t1.getLabel(),t2.getLabel()));
            }
        }
    }

    /**
     * Check stopwords
     */
    @Test
    public void stopWordTest(){
        String s1 = "A call from mom";
        String s2 = "call mom";
        BagOfWordsSimilarity labelSim = new BagOfWordsSimilarity(Word.Similarities.LEVENSHTEIN_LIN_MAX);
        Assert.assertTrue(labelSim.sim(s1,s2) == 1.0);
    }
}