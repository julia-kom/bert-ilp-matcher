package bpm.ippm;

import bpm.ippm.ilp.AbstractILP;
import bpm.ippm.matcher.Pipeline;
import bpm.ippm.profile.AbstractProfile;
import bpm.ippm.profile.BPPlusOwn;
import bpm.ippm.profile.Relation;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Place;
import org.jbpt.petri.Transition;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class BPPlusTest {



    @Test
    public void testBPPlus(){
        NetSystem net1 = new NetSystem();
        Transition t1 = new Transition();
        t1.setId("t1");
        t1.setName("t1");
        t1.setLabel("Transition 1");
        Transition t2 = new Transition();
        t2.setId("t2");
        t2.setName("t2");
        t2.setLabel("Transition 2");
        net1.addTransition(t1);
        net1.addTransition(t2);



        Place p1 = new Place("p1");
        Place p2 = new Place("p2");
        Place p3 = new Place("p3");

        net1.addPlace(p1);
        net1.addPlace(p2);
        net1.addPlace(p3);

        net1.putTokens(p1,1);

        net1.addEdge(p1,t1);
        net1.addEdge(t1,p2);
        net1.addEdge(p2,t2);
        net1.addEdge(t2,p3);
        BPPlusOwn plus = new BPPlusOwn(net1);

        Assert.assertSame(plus.getRelationForEntities(t1,t2).getType(), Relation.RelationType.BPP_DIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t1,t1).getType(), Relation.RelationType.BPP_CONFLICT);
        Assert.assertSame(plus.getRelationForEntities(t2,t2).getType(), Relation.RelationType.BPP_CONFLICT);
        Assert.assertSame(plus.getRelationForEntities(t2,t1).getType(), Relation.RelationType.BPP_REVERSE_DIRECT_CAUSAL);
    }


    @Test
    public void testBPPlus2(){
        NetSystem net1 = new NetSystem();
        Transition t1 = new Transition();
        t1.setId("t1");
        t1.setName("A");
        t1.setLabel("A");

        Transition t2 = new Transition();
        t2.setId("t2");
        t2.setName("B");
        t2.setLabel("B");


        Transition t3 = new Transition();
        t3.setId("t3");
        t3.setName("C");
        t3.setLabel("C");





        net1.addTransition(t1);
        net1.addTransition(t2);
        net1.addTransition(t3);



        Place p1 = new Place("p1");
        Place p2 = new Place("p2");
        Place p3 = new Place("p3");
        Place p4 = new Place("p4");

        net1.addPlace(p1);
        net1.addPlace(p2);
        net1.addPlace(p3);

        net1.putTokens(p1,1);

        net1.addEdge(p1,t1);
        net1.addEdge(t1,p2);
        net1.addEdge(p2,t2);
        net1.addEdge(t2,p3);
        net1.addEdge(p3,t3);
        net1.addEdge(t3,p4);



        BPPlusOwn plus = new BPPlusOwn(net1);

        // nodes in the net
        Assert.assertSame(plus.getRelationForEntities(t1,t2).getType(), Relation.RelationType.BPP_DIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t1,t3).getType(), Relation.RelationType.BPP_INDIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t2,t3).getType(), Relation.RelationType.BPP_DIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t2,t1).getType(), Relation.RelationType.BPP_REVERSE_DIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t3,t1).getType(), Relation.RelationType.BPP_REVERSE_INDIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t3,t2).getType(), Relation.RelationType.BPP_REVERSE_DIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t1,t1).getType(), Relation.RelationType.BPP_CONFLICT);
        Assert.assertSame(plus.getRelationForEntities(t2,t2).getType(), Relation.RelationType.BPP_CONFLICT);
        Assert.assertSame(plus.getRelationForEntities(t3,t3).getType(), Relation.RelationType.BPP_CONFLICT);

        // artificial nodes
        Transition ts = plus.getArtificialInitialTransition();
        Transition te = plus.getArtificialFinalTransition();
        Assert.assertSame(plus.getRelationForEntities(ts,t1).getType(), Relation.RelationType.BPP_DIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(ts,t2).getType(), Relation.RelationType.BPP_INDIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(ts,t3).getType(), Relation.RelationType.BPP_INDIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(ts,te).getType(), Relation.RelationType.BPP_INDIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t1,ts).getType(), Relation.RelationType.BPP_REVERSE_DIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t2,ts).getType(), Relation.RelationType.BPP_REVERSE_INDIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t3,ts).getType(), Relation.RelationType.BPP_REVERSE_INDIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(te,ts).getType(), Relation.RelationType.BPP_REVERSE_INDIRECT_CAUSAL);

        Assert.assertSame(plus.getRelationForEntities(te,t1).getType(), Relation.RelationType.BPP_REVERSE_INDIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(te,t2).getType(), Relation.RelationType.BPP_REVERSE_INDIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(te,t3).getType(), Relation.RelationType.BPP_REVERSE_DIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t1,te).getType(), Relation.RelationType.BPP_INDIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t2,te).getType(), Relation.RelationType.BPP_INDIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t3,te).getType(), Relation.RelationType.BPP_DIRECT_CAUSAL);
    }

    @Test
    public void testBPPlusLoopTest(){

        NetSystem net1 = new NetSystem();
        Transition t1 = new Transition();
        t1.setId("t1");
        t1.setName("A");
        t1.setLabel("A");

        Transition t2 = new Transition();
        t2.setId("t2");
        t2.setName("B");
        t2.setLabel("B");


        Transition t3 = new Transition();
        t3.setId("t3");
        t3.setName("C");
        t3.setLabel("C");

        Transition t4 = new Transition();
        t4.setId("t4");
        t4.setName("D");
        t4.setLabel("D");


        net1.addTransition(t1);
        net1.addTransition(t2);
        net1.addTransition(t3);
        net1.addTransition(t4);

        Place p1 = new Place("p1");
        Place p2 = new Place("p2");
        Place p3 = new Place("p3");
        Place p4 = new Place("p4");

        net1.addPlace(p1);
        net1.addPlace(p2);
        net1.addPlace(p3);
        net1.addPlace(p4);

        net1.putTokens(p1,1);

        net1.addEdge(p1,t1);
        net1.addEdge(t1,p2);
        net1.addEdge(p2,t2);
        net1.addEdge(t2,p3);
        net1.addEdge(p3,t3);
        net1.addEdge(t3,p4);
        net1.addEdge(p3,t4);
        net1.addEdge(t4,p2);

        BPPlusOwn plus = new BPPlusOwn(net1);
        Assert.assertSame(plus.getRelationForEntities(t1,t1).getType(), Relation.RelationType.BPP_CONFLICT);
        Assert.assertSame(plus.getRelationForEntities(t1,t2).getType(), Relation.RelationType.BPP_DIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t1,t3).getType(), Relation.RelationType.BPP_INDIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t1,t4).getType(), Relation.RelationType.BPP_INDIRECT_CAUSAL);

        Assert.assertSame(plus.getRelationForEntities(t2,t1).getType(), Relation.RelationType.BPP_REVERSE_DIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t2,t2).getType(), Relation.RelationType.BPP_INDIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t2,t3).getType(), Relation.RelationType.NONE); //ALWAYS CONCURRENT
        Assert.assertSame(plus.getRelationForEntities(t2,t4).getType(), Relation.RelationType.BPP_DIRECT_CAUSAL);

        Assert.assertSame(plus.getRelationForEntities(t3,t1).getType(), Relation.RelationType.BPP_REVERSE_INDIRECT_CAUSAL); //ALWAYS CONCURRENT
        Assert.assertSame(plus.getRelationForEntities(t3,t2).getType(), Relation.RelationType.NONE); // ALWAYS CONCURRENT
        Assert.assertSame(plus.getRelationForEntities(t3,t3).getType(), Relation.RelationType.BPP_CONFLICT);
        Assert.assertSame(plus.getRelationForEntities(t3,t4).getType(), Relation.RelationType.NONE); // ALWAYS CONCURRENT

        Assert.assertSame(plus.getRelationForEntities(t4,t1).getType(), Relation.RelationType.BPP_REVERSE_INDIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t4,t2).getType(), Relation.RelationType.BPP_DIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t4,t3).getType(), Relation.RelationType.NONE); // ALWAYS CONCURRENT
        Assert.assertSame(plus.getRelationForEntities(t4,t4).getType(), Relation.RelationType.BPP_INDIRECT_CAUSAL);

    }


    @Test
    public void testBPPlusParallelTest(){

        // this test does not terminate. Loop in process model leads to loop in BP+. Failure in RefinedOrderMatrix line 288 while(!queue.isEmpty()) {
        // Queue gets larger before it becomes empty.

        NetSystem net1 = new NetSystem();
        Transition t1 = new Transition();
        t1.setId("t1");
        t1.setName("A");
        t1.setLabel("A");

        Transition t2 = new Transition();
        t2.setId("t2");
        t2.setName("B");
        t2.setLabel("B");


        Transition t3 = new Transition();
        t3.setId("t3");
        t3.setName("C");
        t3.setLabel("C");

        Transition t4 = new Transition();
        t4.setId("t4");
        t4.setName("D");
        t4.setLabel("D");


        net1.addTransition(t1);
        net1.addTransition(t2);
        net1.addTransition(t3);
        net1.addTransition(t4);

        Place p1 = new Place("p1");
        Place p2 = new Place("p2");
        Place p3 = new Place("p3");
        Place p4 = new Place("p4");
        Place p5 = new Place("p5");
        Place p6 = new Place("p6");

        net1.addPlace(p1);
        net1.addPlace(p2);
        net1.addPlace(p3);
        net1.addPlace(p4);

        net1.putTokens(p1,1);

        net1.addEdge(p1,t1);
        net1.addEdge(t1,p2);
        net1.addEdge(t1,p3);
        net1.addEdge(p2,t2);
        net1.addEdge(p3,t3);
        net1.addEdge(t2, p4);
        net1.addEdge(t3, p5);
        net1.addEdge(p4,t4);
        net1.addEdge(p5,t4);
        net1.addEdge(t4,p6);


        BPPlusOwn plus = new BPPlusOwn(net1);
        Assert.assertSame(plus.getRelationForEntities(t1,t1).getType(), Relation.RelationType.BPP_CONFLICT);
        Assert.assertSame(plus.getRelationForEntities(t1,t2).getType(), Relation.RelationType.BPP_DIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t1,t3).getType(), Relation.RelationType.BPP_DIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t1,t4).getType(), Relation.RelationType.BPP_INDIRECT_CAUSAL);

        Assert.assertSame(plus.getRelationForEntities(t2,t1).getType(), Relation.RelationType.BPP_REVERSE_DIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t2,t2).getType(), Relation.RelationType.BPP_CONFLICT);
        Assert.assertSame(plus.getRelationForEntities(t2,t3).getType(), Relation.RelationType.BPP_ALWAYS_CONCURRENT);
        Assert.assertSame(plus.getRelationForEntities(t2,t4).getType(), Relation.RelationType.BPP_DIRECT_CAUSAL);

        Assert.assertSame(plus.getRelationForEntities(t3,t1).getType(), Relation.RelationType.BPP_REVERSE_DIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t3,t2).getType(), Relation.RelationType.BPP_ALWAYS_CONCURRENT);
        Assert.assertSame(plus.getRelationForEntities(t3,t3).getType(), Relation.RelationType.BPP_CONFLICT);
        Assert.assertSame(plus.getRelationForEntities(t3,t4).getType(), Relation.RelationType.BPP_DIRECT_CAUSAL);

        Assert.assertSame(plus.getRelationForEntities(t4,t1).getType(), Relation.RelationType.BPP_REVERSE_INDIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t4,t2).getType(), Relation.RelationType.BPP_REVERSE_DIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t4,t4).getType(), Relation.RelationType.BPP_CONFLICT);
    }





    @Test
    public void failedSAP(){
        File f1 = new File(getClass().getClassLoader().getResource("./pnml/sap/sap12s.pnml").getFile());
        File f2 = new File(getClass().getClassLoader().getResource("./pnml/sap/sap12t.pnml").getFile());

        //Behavior only test
        Pipeline p1 = new Pipeline.Builder().withILP(AbstractILP.ILP.BASIC5).atSimilarityWeight(0.3).withILPTimeLimit(1).withProfile(AbstractProfile.Profile.BPP).Build();
        p1.run(f1,f2);
    }

    @Test
    public void failedUni(){
        File f1 = new File(getClass().getClassLoader().getResource("./pnml/uni/Cologne.pnml").getFile());
        File f2 = new File(getClass().getClassLoader().getResource("./pnml/uni/Wuerzburg.pnml").getFile());

        //Behavior only test
        Pipeline p1 = new Pipeline.Builder().withILP(AbstractILP.ILP.BASIC5).atSimilarityWeight(0.3).withILPTimeLimit(1).withProfile(AbstractProfile.Profile.BPP).Build();
        p1.run(f1,f2);
    }


}
