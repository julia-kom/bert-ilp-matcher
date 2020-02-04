package bpm.matcher;

import bpm.profile.DirectlyFollowsLogProfile;
import bpm.profile.EventuallyFollowsLogProfile;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.id.XID;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Place;
import org.jbpt.petri.Transition;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class LogProfileTest {
    private XLog log1;
    private XLog log2;

    private NetSystem model1;
    private NetSystem model2;

    Transition t1 = new Transition("t1");
    Transition t2 = new Transition("t2");
    Transition t3 = new Transition("t3");
    Transition t4 = new Transition("t4");
    Transition t5 = new Transition("t5");
    Transition t6 = new Transition("t6");
    Transition t7 = new Transition("t7");
    Transition t8 = new Transition("t8");
    Transition t9 = new Transition("t9");
    Transition t10 = new Transition("t10");

    Place p1 = new Place();
    Place p2 = new Place();
    Place p3 = new Place();
    Place p4 = new Place();
    Place p5 = new Place();
    Place p6 = new Place();
    Place p7 = new Place();
    Place p8 = new Place();
    Place p9 = new Place();
    Place p10 = new Place();
    Place p11 = new Place();
    Place p12 = new Place();

    @BeforeClass
    private void createArtificialLogs(){

       XFactoryNaiveImpl factory = new XFactoryNaiveImpl();
       XEvent e1 = factory.createEvent(XID.parse("t1"), null);
       XEvent e2 = factory.createEvent(XID.parse("t2"), null);
       XEvent e3 = factory.createEvent(XID.parse("t3"), null);
       XEvent e4 = factory.createEvent(XID.parse("t4"), null);
       XEvent e5 = factory.createEvent(XID.parse("t5"), null);
       XEvent e6 = factory.createEvent(XID.parse("t6"), null);
       XEvent e7 = factory.createEvent(XID.parse("t7"), null);
       XEvent e8 = factory.createEvent(XID.parse("t8"), null);
       XEvent e9 = factory.createEvent(XID.parse("t9"), null);
       XEvent e10 = factory.createEvent(XID.parse("t10"), null);

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
        log1 = factory.createLog();
        for (int i = 0; i < 39; i++) {
            log2.add(t3);
        }
    }

    @BeforeClass
    private void createArtificialModels(){


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
    public void eventuallyFollowsTest(){
        EventuallyFollowsLogProfile profile = new EventuallyFollowsLogProfile(model1,log1);
        //Assert.assertTrue(profile.getRelationSimilarity());


    }

    @Test
    public void directlyFollowsTest(){
        DirectlyFollowsLogProfile profile = new DirectlyFollowsLogProfile(model1,log1);

    }


}
