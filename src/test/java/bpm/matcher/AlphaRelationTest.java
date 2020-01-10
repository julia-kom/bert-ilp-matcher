package bpm.matcher;

import bpm.profile.AbstractProfile;

import bpm.profile.AlphaRelations;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Place;
import org.jbpt.petri.Transition;
import org.junit.Assert;
import org.junit.Test;


public class AlphaRelationTest {
    @Test
    public void AlphaRelationBasicTest(){
        NetSystem net1 = new NetSystem();

        Transition t1 = new Transition();
        t1.setId("t1");
        t1.setName("Name 1");
        t1.setLabel("Name 1");

        Transition t2 = new Transition();
        t2.setId("t2");
        t2.setName("Name 2");
        t2.setLabel("Name 2");

        Transition t3 = new Transition();
        t3.setId("t3");
        t3.setName("Name 3");
        t3.setLabel("Name 3");


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
        net1.addPlace(p4);

        net1.addEdge(p1,t1);
        net1.addEdge(t1,p2);
        net1.addEdge(p2,t2);
        net1.addEdge(t2,p3);
        net1.addEdge(p3,t3);
        net1.addEdge(t3,p4);

        net1.putTokens(p1,1);

        AlphaRelations alpha = new AlphaRelations(net1);
        Assert.assertSame(alpha.getRelationForEntities(t1,t2), AbstractProfile.Relation.ALPHA_ORDER);
        Assert.assertSame(alpha.getRelationForEntities(t2,t3), AbstractProfile.Relation.ALPHA_ORDER);
        Assert.assertSame(alpha.getRelationForEntities(t1,t3), AbstractProfile.Relation.ALPHA_EXCLUSIVE);
    }

    @Test
    public void AlphaRelationTauTest() {
        NetSystem net1 = new NetSystem();

        Transition t1 = new Transition();
        t1.setId("t1");
        t1.setName("Name 1");
        t1.setLabel("Name 1");


        //TAU transition.
        Transition t2 = new Transition();
        t2.setId("t2");
        t2.setName("t2");
        t2.setLabel("");

        //TAU transition.
        Transition t3 = new Transition();
        t3.setId("t3");
        t3.setName("t3");
        t3.setLabel("");

        Transition t4 = new Transition();
        t4.setId("t4");
        t4.setName("Name 4");
        t4.setLabel("Name 4");

        net1.addTransition(t1);
        net1.addTransition(t2);
        net1.addTransition(t3);
        net1.addTransition(t4);

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

        net1.addEdge(p1,t1);
        net1.addEdge(t1,p2);
        net1.addEdge(p2,t2);
        net1.addEdge(t2,p3);
        net1.addEdge(p3,t3);
        net1.addEdge(t3,p4);
        net1.addEdge(p4,t4);
        net1.addEdge(t4,p5);

        net1.putTokens(p1,1);

        AlphaRelations alpha = new AlphaRelations(net1);
        Assert.assertSame(alpha.getRelationForEntities(t1,t2), AbstractProfile.Relation.ALPHA_EXCLUSIVE);
        Assert.assertSame(alpha.getRelationForEntities(t2,t3), AbstractProfile.Relation.ALPHA_EXCLUSIVE);
        Assert.assertSame(alpha.getRelationForEntities(t1,t3), AbstractProfile.Relation.ALPHA_EXCLUSIVE);
        Assert.assertSame(alpha.getRelationForEntities(t1,t4), AbstractProfile.Relation.ALPHA_ORDER);
    }

}
