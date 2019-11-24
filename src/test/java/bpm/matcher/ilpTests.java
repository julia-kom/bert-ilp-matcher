package bpm.matcher;

import bpm.alignment.Result;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

import static java.lang.Math.abs;

public class ilpTests {

    @Test
    public void relaxed3Test(){
        Pipeline p1 = new Pipeline.Builder().withILP("RELAXED").Build();
        Pipeline p2 = new Pipeline.Builder().withILP("RELAXED3").Build();
        File f1 = new File(getClass().getClassLoader().getResource("./pnml/app_store/app_create_account_comp1.pnml").getFile());
        File f2 = new File(getClass().getClassLoader().getResource("./pnml/app_store/app_purchase_comp1.pnml").getFile());

        Result r1 =  p1.run(f1,f2);
        Result r2 = p2.run(f1,f2);

        System.out.println(r1.getAlignment().toString()+ "\n" +r2.getAlignment().toString());

        //equal similarity
        Assert.assertTrue(""+abs(r1.getSimilarity()- r2.getSimilarity()),abs(r1.getSimilarity() - r2.getSimilarity()) <0.001);

        //equal matching
        Assert.assertTrue(r1.getAlignment().toString()+ "\n" +r2.getAlignment().toString(),r1.getAlignment().equals(r2.getAlignment()));

    }

    @Test
    public void maxSimTest(){
        Pipeline p1 = new Pipeline.Builder().withILP("BASIC").atSimilarityWeight(1).Build();
        Pipeline p2 = new Pipeline.Builder().withILP("RELAXED").atSimilarityWeight(1).Build();
        Pipeline p3 = new Pipeline.Builder().withILP("RELAXED3").atSimilarityWeight(1).Build();
        File f1 = new File(getClass().getClassLoader().getResource("./pnml/app_store/app_purchase_comp1.pnml").getFile());
        File f2 = new File(getClass().getClassLoader().getResource("./pnml/app_store/app_purchase_comp1.pnml").getFile());

        Assert.assertTrue("BASIC Similarity is not 1: " + p1.run(f1,f2).getSimilarity(), abs(p1.run(f1,f2).getSimilarity() -1.0) <= 0.0001);
        Assert.assertTrue("RELAXED Similarity is not 1: " + p2.run(f1,f2).getSimilarity(), abs(p2.run(f1,f2).getSimilarity() -1.0) <= 0.0001);
        Assert.assertTrue("RELAXED3 Similarity is not 1: " + p3.run(f1,f2).getSimilarity(),abs(p3.run(f1,f2).getSimilarity() -1.0) <= 0.0001);
    }


    @Test
    public void useCase(){
        Pipeline p1 = new Pipeline.Builder().withILP("BASIC").atSimilarityWeight(1).atPostprocessThreshold(0.0).Build();
        Pipeline p2 = new Pipeline.Builder().withILP("RELAXED").atSimilarityWeight(1).atPostprocessThreshold(0.0).Build();
        File f1 = new File(getClass().getClassLoader().getResource("./pnml/app_store/app_create_account_comp1.pnml").getFile());
        File f2 = new File(getClass().getClassLoader().getResource("./pnml/app_store/app_create_account_comp2.pnml").getFile());
        Result r1 =  p1.run(f1,f2);
        Result r2 = p2.run(f1,f2);

        System.out.println(r1.getSimilarity());
        System.out.println(r2.getSimilarity());

        //equal matching
        Assert.assertTrue(r1.getAlignment().toString()+ "\n" +r2.getAlignment().toString(),r1.getAlignment().equals(r2.getAlignment()));

        //equal similarity
        Assert.assertTrue(""+r1.getSimilarity()+ "vs."+ r2.getSimilarity(),abs(r1.getSimilarity() - r2.getSimilarity()) <0.001);

    }
}
