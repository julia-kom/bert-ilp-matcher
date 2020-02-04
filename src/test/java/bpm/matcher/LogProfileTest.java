package bpm.matcher;

import bpm.profile.DirectlyFollowsLogProfile;
import bpm.profile.EventuallyFollowsLogProfile;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.*;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Place;
import org.jbpt.petri.Transition;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import static bpm.profile.AlphaRelations.directlyFollows;

public class LogProfileTest {
    private static XLog log1;
    private static XLog log2;

    private static NetSystem model1;
    private static NetSystem model2;

    static Transition t1 = new Transition("t1","Transition 1");
    static Transition t2 = new Transition("t2","Transition 2");
    static Transition t3 = new Transition("t3","Transition 3");
    static Transition t4 = new Transition("t4","Transition 4");
    static Transition t5 = new Transition("t5","Transition 5");
    static Transition t6 = new Transition("t6","Transition 6");
    static Transition t7 = new Transition("t7","Transition 7");
    static Transition t8 = new Transition("t8","Transition 8");
    static Transition t9 = new Transition("t9","Transition 9");
    static Transition t10 = new Transition("t10","Transition 10");

    static Place p1 = new Place();
    static Place p2 = new Place();
    static Place p3 = new Place();
    static Place p4 = new Place();
    static Place p5 = new Place();
    static Place p6 = new Place();
    static Place p7 = new Place();
    static Place p8 = new Place();
    static Place p9 = new Place();
    static Place p10 = new Place();
    static Place p11 = new Place();
    static Place p12 = new Place();

    @BeforeClass
    public static void createArtificialLogs(){
       XEvent e1 = createEvent("t1");
       XEvent e2 = createEvent("t2");
       XEvent e3 = createEvent("t3");
       XEvent e4 = createEvent("t4");
       XEvent e5 = createEvent("t5");
       XEvent e6 = createEvent("t6");
       XEvent e7 = createEvent("t7");
       XEvent e8 = createEvent("t8");
       XEvent e9 = createEvent("t9");
       XEvent e10 = createEvent("t10");


       XFactoryNaiveImpl factory = new XFactoryNaiveImpl();
       XTrace t1 = factory.createTrace();
       t1.insertOrdered(e1);
       t1.insertOrdered(e2);
       t1.insertOrdered(e3);
       t1.insertOrdered(e6);

       XTrace t2 = factory.createTrace();
       t2.insertOrdered(e1);
       t2.insertOrdered(e4);
       t2.insertOrdered(e5);
       t2.insertOrdered(e6);

       //LOG1: <e1,e2,e3,e6>^80, <e1,e4,e5,e6>^20
       log1 = factory.createLog();
       for (int i = 0; i < 80; i++) {
            log1.add(t1);
       }

       for (int i = 0; i < 20; i++) {
            log1.add(t2);
       }


        XTrace t3 = factory.createTrace();
        t3.insertOrdered(e7);
        t3.insertOrdered(e8);
        t3.insertOrdered(e9);
        t3.insertOrdered(e10);

        //LOG2: <e1,e2,e3,e6>^39
        log2 = factory.createLog();
        for (int i = 0; i < 39; i++) {
            log2.add(t3);
        }
    }

    @BeforeClass
    public static void createArtificialModels(){

        t1.setId("t1");
        t2.setId("t2");
        t3.setId("t3");
        t4.setId("t4");
        t5.setId("t5");
        t6.setId("t6");
        t7.setId("t7");
        t8.setId("t8");
        t9.setId("t9");
        t10.setId("t10");

        model1 = new NetSystem();

        model1.addTransition(t1);
        model1.addTransition(t2);
        model1.addTransition(t3);
        model1.addTransition(t4);
        model1.addTransition(t5);
        model1.addTransition(t6);

        model1.addPlace(p1);
        model1.addPlace(p2);
        model1.addPlace(p3);
        model1.addPlace(p4);
        model1.addPlace(p5);
        model1.addPlace(p6);
        model1.addPlace(p7);

        model1.addEdge(p1,t1);
        model1.addEdge(t1,p2);
        model1.addEdge(p2,t2); //xor branch at p2
        model1.addEdge(p2,t4);
        model1.addEdge(t2,p3);
        model1.addEdge(t4,p4);
        model1.addEdge(p3,t3);
        model1.addEdge(p4,t5);
        model1.addEdge(t3,p6); // xor join at p6
        model1.addEdge(t5,p6);
        model1.addEdge(p6,t6);
        model1.addEdge(t6,p7);

        model1.putTokens(p1,1);


        model2 = new NetSystem();

        model2.addTransition(t7);
        model2.addTransition(t8);
        model2.addTransition(t9);
        model2.addTransition(t10);

        model2.addPlace(p8);
        model2.addPlace(p9);
        model2.addPlace(p10);
        model2.addPlace(p11);
        model2.addPlace(p12);

        model2.addEdge(p8,t7);
        model2.addEdge(t7,p9);
        model2.addEdge(p9,t8);
        model2.addEdge(t8,p10);
        model2.addEdge(p10,t9);
        model2.addEdge(t9,p11);
        model2.addEdge(p11,t10);
        model2.addEdge(t10,p12);

        model2.putTokens(p8,1);

    }



    @Test
    public  void eventuallyFollowsTest(){
        EventuallyFollowsLogProfile profile = new EventuallyFollowsLogProfile(model1,log1);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t1,t1).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t1,t2).getFrequency() -0.8) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t1,t4).getFrequency() -0.2) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t1,t3).getFrequency() -0.8) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t1,t5).getFrequency() -0.2) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t1,t6).getFrequency() -1.0) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t2,t1).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t2,t2).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t2,t3).getFrequency() -1.0) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t2,t4).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t2,t5).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t2,t6).getFrequency() -1.0) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t3,t2).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t3,t3).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t3,t4).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t3,t5).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t3,t6).getFrequency() -1.0) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t4,t1).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t4,t2).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t4,t3).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t4,t4).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t4,t5).getFrequency() -1.0) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t4,t6).getFrequency() -1.0) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t5,t1).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t5,t2).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t5,t3).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t5,t4).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t5,t5).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t5,t6).getFrequency() -1.0) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t6,t6).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t6,t5).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t6,t3).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t6,t1).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t6,t2).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t6,t4).getFrequency()) < 0.0001);
    }



    @Test
    public void directlyFollowsTest(){
        DirectlyFollowsLogProfile profile = new DirectlyFollowsLogProfile(model1,log1);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t1,t1).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t1,t2).getFrequency() -0.8) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t1,t4).getFrequency() -0.2) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t1,t3).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t1,t5).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t1,t6).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t2,t1).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t2,t2).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t2,t3).getFrequency() -1.0) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t2,t4).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t2,t5).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t2,t6).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t3,t2).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t3,t3).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t3,t4).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t3,t5).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t3,t6).getFrequency() -1.0) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t4,t1).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t4,t2).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t4,t3).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t4,t4).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t4,t5).getFrequency() -1.0) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t5,t1).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t5,t2).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t5,t3).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t5,t4).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t5,t5).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t5,t6).getFrequency() -1.0) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t6,t6).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t6,t5).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t6,t3).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t6,t1).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t6,t2).getFrequency()) < 0.0001);
        Assert.assertTrue(Math.abs(profile.getRelationForEntities(t6,t4).getFrequency()) < 0.0001);

    }

    @Test
    public void compareTest(){
        DirectlyFollowsLogProfile profile1 = new DirectlyFollowsLogProfile(model1,log1);
        DirectlyFollowsLogProfile profile2 = new DirectlyFollowsLogProfile(model2,log2);

        Assert.assertTrue(profile1.getRelationSimilarity(profile1.getRelationForEntities(t1,t6), profile2.getRelationForEntities(t7,t10))==1);
        Assert.assertTrue(profile1.getRelationSimilarity(profile1.getRelationForEntities(t1,t2), profile2.getRelationForEntities(t7,t8))==0.8);
        Assert.assertTrue(profile1.getRelationSimilarity(profile1.getRelationForEntities(t1,t4), profile2.getRelationForEntities(t7,t8))==0.2);
        Assert.assertTrue(profile1.getRelationSimilarity(profile1.getRelationForEntities(t2,t4), profile2.getRelationForEntities(t7,t8))==0);
        Assert.assertTrue(profile1.getRelationSimilarity(profile1.getRelationForEntities(t5,t6), profile2.getRelationForEntities(t7,t8))==1.0);

    }


    @Test
    public void LogNoLogDirectlyFollowsTest(){
        DirectlyFollowsLogProfile profile1 = new DirectlyFollowsLogProfile(model1,log1);
        DirectlyFollowsLogProfile profile2 = new DirectlyFollowsLogProfile(model2,null);
        Assert.assertTrue(profile1.getRelationSimilarity(profile1.getRelationForEntities(t1,t6), profile2.getRelationForEntities(t7,t10))==1);
        Assert.assertTrue(profile1.getRelationSimilarity(profile1.getRelationForEntities(t1,t2), profile2.getRelationForEntities(t7,t8))==0.8);
        Assert.assertTrue(profile1.getRelationSimilarity(profile1.getRelationForEntities(t1,t4), profile2.getRelationForEntities(t7,t8))==0.2);
        Assert.assertTrue(profile1.getRelationSimilarity(profile1.getRelationForEntities(t2,t4), profile2.getRelationForEntities(t7,t8))==0);
        Assert.assertTrue(profile1.getRelationSimilarity(profile1.getRelationForEntities(t5,t6), profile2.getRelationForEntities(t7,t8))==1.0);

        profile1 = new DirectlyFollowsLogProfile(model1,null);
        profile2 = new DirectlyFollowsLogProfile(model2,log2);
        Assert.assertTrue(profile1.getRelationSimilarity(profile1.getRelationForEntities(t1,t6), profile2.getRelationForEntities(t7,t10))==1);
        Assert.assertTrue(profile1.getRelationSimilarity(profile1.getRelationForEntities(t1,t2), profile2.getRelationForEntities(t7,t8))==1);
        Assert.assertTrue(profile1.getRelationSimilarity(profile1.getRelationForEntities(t1,t4), profile2.getRelationForEntities(t7,t8))==1);
        Assert.assertTrue(profile1.getRelationSimilarity(profile1.getRelationForEntities(t2,t4), profile2.getRelationForEntities(t7,t8))==0);
        Assert.assertTrue(profile1.getRelationSimilarity(profile1.getRelationForEntities(t5,t6), profile2.getRelationForEntities(t7,t8))==1.0);
    }

    @Test
    public void NoLogNoLogDirectlyFollowsTest(){
        DirectlyFollowsLogProfile profile1 = new DirectlyFollowsLogProfile(model1,null);
        DirectlyFollowsLogProfile profile2 = new DirectlyFollowsLogProfile(model2,null);
        Assert.assertTrue(profile1.getRelationSimilarity(profile1.getRelationForEntities(t1,t6), profile2.getRelationForEntities(t7,t10))==1);
        Assert.assertTrue(profile1.getRelationSimilarity(profile1.getRelationForEntities(t1,t2), profile2.getRelationForEntities(t7,t8))==1);
        Assert.assertTrue(profile1.getRelationSimilarity(profile1.getRelationForEntities(t1,t4), profile2.getRelationForEntities(t7,t8))==1);
        Assert.assertTrue(profile1.getRelationSimilarity(profile1.getRelationForEntities(t2,t4), profile2.getRelationForEntities(t7,t8))==0);
        Assert.assertTrue(profile1.getRelationSimilarity(profile1.getRelationForEntities(t5,t6), profile2.getRelationForEntities(t7,t8))==1.0);
    }

    private static XEvent createEvent(String name){
        XFactoryNaiveImpl factory = new XFactoryNaiveImpl();
        XAttribute a = factory.createAttributeLiteral("action_code",name,null);
        XAttributeMap map = factory.createAttributeMap();
        map.put("action_code",a);
        return factory.createEvent(map);
    }



}
