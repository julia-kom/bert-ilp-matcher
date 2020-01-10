package bpm.matcher;

import bpm.profile.AbstractProfile;
import bpm.profile.BPPlus;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Place;
import org.jbpt.petri.Transition;
import org.junit.Assert;

public class BPPlusTest {

    //@Test
    public void testBPPlus(){
        NetSystem net1 = new NetSystem();
        Transition t1 = new Transition();
        t1.setId("t1");
        t1.setName("t1");
        Transition t2 = new Transition();
        t2.setId("t2");
        t2.setName("t2");
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
}
