package bpm.matcher;

import bpm.alignment.Alignment;
import bpm.alignment.Correspondence;
import bpm.similarity.Matrix;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Transition;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class PreprocessorTest {
    @Test
    public void isTauTest() {

        Transition t = new Transition();
        Set transitions = new HashSet<Transition>();
        transitions.add(t);

        // s_1 test positive test
        t.setLabel("s_1");
        t.setId("id-1");
        Assert.assertTrue(!Preprocessor.reduceTauTransitions(transitions).isEmpty());

        // t_1 test REGEX
        t.setLabel("t_1");
        t.setId("id-1");
        Assert.assertTrue(Preprocessor.reduceTauTransitions(transitions).isEmpty());

        //t_12 REGEX
        t.setLabel("t_12");
        t.setId("id-1");
        Assert.assertTrue(Preprocessor.reduceTauTransitions(transitions).isEmpty());

        //t_12 Empty Label
        t.setLabel("");
        t.setId("id-1");
        Assert.assertTrue(Preprocessor.reduceTauTransitions(transitions).isEmpty());

        //t_12 Label = Id
        t.setLabel("id-1");
        t.setId("id-1");
        Assert.assertTrue(Preprocessor.reduceTauTransitions(transitions).isEmpty());
    }

    @Test
    public void preMatchTest() {
        // set up net system with two transitions each
        NetSystem net1 = new NetSystem();
        Transition t1 = new Transition();
        Transition t2 = new Transition();
        net1.addTransition(t1);
        net1.addTransition(t2);

        NetSystem net2 = new NetSystem();
        Transition t3 = new Transition();
        Transition t4 = new Transition();
        net2.addTransition(t3);
        net2.addTransition(t4);

        // find no prematch
        t1.setLabel("L1");
        t2.setLabel("L2");
        t3.setLabel("L3");
        t4.setLabel("L4");
        Matrix sim = new Matrix.Builder().build(net1.getTransitions(), net2.getTransitions());
        Alignment a = Preprocessor.prematch(net1, net2, sim);
        Assert.assertTrue(a.getCorrespondences().size() == 0);

        //find two prematches
        t1.setLabel("L1");
        t2.setLabel("L2");
        t3.setLabel("L1");
        t4.setLabel("L2");
        sim = new Matrix.Builder().build(net1.getTransitions(), net2.getTransitions());
        a = Preprocessor.prematch(net1, net2, sim);
        Assert.assertTrue(a.getCorrespondences().size() == 2);

        //find a complex match and delete it right away
        t1.setLabel("L1");
        t2.setLabel("L2");
        t3.setLabel("L1");
        t4.setLabel("L1");
        sim = new Matrix.Builder().build(net1.getTransitions(), net2.getTransitions());
        a = Preprocessor.prematch(net1, net2, sim);
        Assert.assertTrue(a.getCorrespondences().size() == 0);

    }
}
