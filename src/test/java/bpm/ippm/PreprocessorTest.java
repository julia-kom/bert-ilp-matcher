package bpm.ippm;

import bpm.ippm.alignment.Alignment;
import bpm.ippm.matcher.Preprocessor;
import bpm.ippm.profile.AbstractProfile;
import bpm.ippm.profile.Relation;
import bpm.ippm.similarity.Matrix;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Transition;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static bpm.ippm.profile.AbstractProfile.createProfile;
import static java.lang.Math.abs;

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
        Alignment a = Preprocessor.prematch(net1.getTransitions(), net2.getTransitions(), sim);
        Assert.assertTrue(a.getCorrespondences().size() == 0);

        //find two prematches
        t1.setLabel("L1");
        t2.setLabel("L2");
        t3.setLabel("L1");
        t4.setLabel("L2");
        sim = new Matrix.Builder().build(net1.getTransitions(), net2.getTransitions());
        a = Preprocessor.prematch(net1.getTransitions(), net2.getTransitions(), sim);
        Assert.assertTrue(a.getCorrespondences().size() == 2);

        //find a complex match and delete it right away
        t1.setLabel("L1");
        t2.setLabel("L2");
        t3.setLabel("L1");
        t4.setLabel("L1");
        sim = new Matrix.Builder().build(net1.getTransitions(), net2.getTransitions());
        a = Preprocessor.prematch(net1.getTransitions(), net2.getTransitions(), sim);
        Assert.assertTrue(a.getCorrespondences().size() == 0);

    }

    @Test
    public void ProfileSymmetryTest(){
        File folder = new File(getClass().getClassLoader().getResource("./pnml/app_store/").getFile());
        for(File file1 : folder.listFiles()) {
            NetSystem net1 = Preprocessor.parseFile(file1);
            net1.setName(file1.getName());
            AbstractProfile relNet1 = createProfile(net1, AbstractProfile.Profile.BP, null);
            for(Transition t1 : net1.getTransitions()){
                for(Transition t2 : net1.getTransitions()){
                    Relation r = relNet1.getRelationForEntities(t1,t2);
                    switch(r.getType()){
                        case BP_EXCLUSIVE:
                            Assert.assertTrue(relNet1.getRelationForEntities(t2,t1).getType().equals(Relation.RelationType.BP_EXCLUSIVE));
                            break;
                        case BP_INTERLEAVING:
                            Assert.assertTrue(relNet1.getRelationForEntities(t2,t1).getType().equals(Relation.RelationType.BP_EXCLUSIVE));
                            break;
                        case BP_ORDER:
                            Assert.assertTrue(relNet1.getRelationForEntities(t2,t1).getType().equals(Relation.RelationType.BP_REVERSE_ORDER));
                            break;
                        case BP_REVERSE_ORDER:
                            Assert.assertTrue(relNet1.getRelationForEntities(t2,t1).getType().equals(Relation.RelationType.BP_ORDER));
                            break;
                    }

                }
            }
        }
    }
}
