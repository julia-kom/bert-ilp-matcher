package bpm.matcher;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.junit.Assert;
import org.junit.Test;
import java.util.Arrays;
import java.util.Collections;

/**
 * Unit test for simple App.
 */
public class LabelSimilarityTest
{
    @Test
    public void wordSimilarityTests(){
        System.out.println();
        Assert.assertTrue(LabelSimilarity.LinWordSim("house","lodge") >= 0.9);
        Assert.assertTrue(LabelSimilarity.LinWordSim("house","car") <1.0);
        Assert.assertTrue(LabelSimilarity.JiangWordSim("house","lodge") >= 0.75);
        Assert.assertTrue(LabelSimilarity.JiangWordSim("house","car") <= 1.0);
        Assert.assertTrue( LabelSimilarity.LevenshteinWordSim("house","house") == 1.0);
        Assert.assertTrue( LabelSimilarity.LevenshteinWordSim("house","houte") == 0.8);
    }

    @Test
    public void bagOfWordsTests(){
        // Order test
        String s1 = "house mouse klaus";
        String s2 = "klaus house mouse";
        LabelSimilarity sim = new LabelSimilarity();
        Assert.assertTrue(sim.BagOfWordSim(s1,s2) == 1);

        // Lin-Lev-Max Similarity Bag of Words Test
        double val1Lin = LabelSimilarity.LinWordSim("house","lodge");
        double val2Lin = LabelSimilarity.LinWordSim("house","car");
        double val3Lin = LabelSimilarity.LinWordSim("house","elephant");
        double val4Lin = LabelSimilarity.LinWordSim("lion","lodge");
        double val5Lin = LabelSimilarity.LinWordSim("lion","car");
        double val6Lin = LabelSimilarity.LinWordSim("lion","elephant");

        double val1Lev = LabelSimilarity.LevenshteinWordSim("house","lodge");
        double val2Lev = LabelSimilarity.LevenshteinWordSim("house","car");
        double val3Lev = LabelSimilarity.LevenshteinWordSim("house","elephant");
        double val4Lev = LabelSimilarity.LevenshteinWordSim("lion","lodge");
        double val5Lev = LabelSimilarity.LevenshteinWordSim("lion","car");
        double val6Lev = LabelSimilarity.LevenshteinWordSim("lion","elephant");

        double bow  = max(val1Lev,val1Lin,val2Lev,val2Lin,val3Lev,val3Lin); //house
        bow += max(val4Lev,val4Lin,val5Lev,val5Lin,val6Lev,val6Lin); // lion
        bow += max(val1Lev,val1Lin,val4Lev,val4Lin); // lodge
        bow += max(val2Lev,val2Lin,val5Lev,val5Lin); // car
        bow += max(val3Lev, val3Lin, val6Lev,val6Lin); //elephant
        bow = bow / (2+3);
        Assert.assertTrue(Math.abs(sim.BagOfWordSim("house lion", "lodge car elephant") - bow) <0.01);

    }

    private static double max(Double... vals) {
        return Collections.max(Arrays.asList(vals));
    }
}