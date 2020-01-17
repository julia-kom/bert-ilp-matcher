package bpm.matcher;

import bpm.profile.AbstractProfile;
import bpm.profile.BPPlus;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Place;
import org.jbpt.petri.Transition;
import org.jbpt.petri.unfolding.Condition;
import org.junit.Assert;
import org.junit.Test;

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
        BPPlus plus = new BPPlus(net1);
        // Problem here: The compiled jar and the project use different versions of the same class + probably different compiler verisions (BP+ 1.8, this 1.7)
        Assert.assertSame(plus.getRelationForEntities(t1,t2), AbstractProfile.Relation.BPP_DIRECT_CAUSAL);
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

        Transition t4 = new Transition();
        t4.setId("t4");
        t4.setName("");
        t4.setLabel("");

        Transition t5 = new Transition();
        t5.setId("t5");
        t5.setName("");
        t5.setLabel("");

        Transition t6 = new Transition();
        t6.setId("t6");
        t6.setName("");
        t6.setLabel("");




        net1.addTransition(t1);
        net1.addTransition(t2);
        net1.addTransition(t3);
        net1.addTransition(t4);
        net1.addTransition(t5);
        net1.addTransition(t6);


        Place p1 = new Place("p1");
        Place p2 = new Place("p2");
        Place p3 = new Place("p3");
        Place p4 = new Place("p4");
        Place p5 = new Place("p5");

        net1.addPlace(p1);
        net1.addPlace(p2);
        net1.addPlace(p3);
        net1.addPlace(p4);
        net1.addPlace(p5);

        net1.putTokens(p1,1);

        net1.addEdge(p1,t1);
        net1.addEdge(p1,t4);
        net1.addEdge(t1,p2);
        net1.addEdge(t4,p3);
        net1.addEdge(p2,t5);
        net1.addEdge(t5,p3);
        net1.addEdge(p3,t2);
        net1.addEdge(p2,t6);
        net1.addEdge(t6,p4);
        net1.addEdge(t2,p4);
        net1.addEdge(p4,t3);
        net1.addEdge(t3,p5);


        BPPlus plus = new BPPlus(net1);

        // nodes in the net
        Assert.assertSame(plus.getRelationForEntities(t1,t2), AbstractProfile.Relation.BPP_DIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t1,t3), AbstractProfile.Relation.BPP_DIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t2,t3), AbstractProfile.Relation.BPP_DIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t2,t1), AbstractProfile.Relation.BPP_REVERSE_DIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t3,t1), AbstractProfile.Relation.BPP_REVERSE_DIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t3,t2), AbstractProfile.Relation.BPP_REVERSE_DIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t1,t1), AbstractProfile.Relation.BPP_CONFLICT);
        Assert.assertSame(plus.getRelationForEntities(t2,t2), AbstractProfile.Relation.BPP_CONFLICT);
        Assert.assertSame(plus.getRelationForEntities(t3,t3), AbstractProfile.Relation.BPP_CONFLICT);

        // artificial nodes
        Transition ts = plus.getArtificialInitialTransition();
        Transition te = plus.getArtificialFinalTransition();
        Assert.assertSame(plus.getRelationForEntities(ts,t1), AbstractProfile.Relation.BPP_DIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(ts,t2), AbstractProfile.Relation.BPP_DIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(ts,t3), AbstractProfile.Relation.BPP_INDIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(ts,te), AbstractProfile.Relation.BPP_INDIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t1,ts), AbstractProfile.Relation.BPP_REVERSE_DIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t2,ts), AbstractProfile.Relation.BPP_REVERSE_DIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t3,ts), AbstractProfile.Relation.BPP_REVERSE_INDIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(te,ts), AbstractProfile.Relation.BPP_REVERSE_INDIRECT_CAUSAL);

        Assert.assertSame(plus.getRelationForEntities(te,t1), AbstractProfile.Relation.BPP_REVERSE_INDIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(te,t2), AbstractProfile.Relation.BPP_REVERSE_INDIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(te,t3), AbstractProfile.Relation.BPP_REVERSE_DIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t1,te), AbstractProfile.Relation.BPP_INDIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t2,te), AbstractProfile.Relation.BPP_INDIRECT_CAUSAL);
        Assert.assertSame(plus.getRelationForEntities(t3,te), AbstractProfile.Relation.BPP_DIRECT_CAUSAL);
    }


}
