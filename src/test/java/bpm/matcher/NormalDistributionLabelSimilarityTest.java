package bpm.matcher;

import bpm.alignment.Result;
import bpm.profile.AbstractProfile;
import bpm.similarity.NormalDistributionLabelSimilarity;
import bpm.similarity.Word;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class NormalDistributionLabelSimilarityTest {
    @Test
    public void functionalityTest(){

        NormalDistributionLabelSimilarity l = new NormalDistributionLabelSimilarity(Word.Similarities.LIN);

        String a = "Label A";
        String b = "Label B";
        double sim1 = l.BagOfWords(a,b);
        double sim2 = l.BagOfWords(a,b);
        double sim3 = l.BagOfWords(b,a);
        double sim4 = l.BagOfWords(a,b);

        Assert.assertTrue("1) Sim is not equal: " + sim1 +" " + sim2,sim1 == sim2);
        Assert.assertTrue("2) Sim is not equal: " + sim1 +" " + sim3,sim1 == sim3);
        Assert.assertTrue("3) Sim is not equal: " + sim1 +" " + sim4,sim1 == sim4);

        // test if sims are same over several executions!
        l = new NormalDistributionLabelSimilarity(Word.Similarities.LIN);
        double sim5 = l.BagOfWords(a,b);

        Assert.assertTrue("1) Sim is not equal: " + sim1 +" " + sim5,sim1 == sim2);


    }


    @Test
    public void simTest(){
        File path = new File("eval-data/pnml/sim-comp");

        for(File f1 : path.listFiles()) {
            for(File f2 : path.listFiles()) {
                Pipeline matcher = new Pipeline.Builder()
                        .atSimilarityWeight(0.5)
                        .withWordSimilarity("NOISE")
                        .withProfile(AbstractProfile.Profile.BP)
                        .atPostprocessThreshold(0.0)
                        .withILP("BASIC2").Build();

                //System.out.println(f1.toString() +f2.toString());
                Result res = matcher.run(f1,f2);
                System.out.println(matcher.toString());
                System.out.println(res);
            }
        }


    }


}
