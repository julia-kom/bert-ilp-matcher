package bpm.profile;

import org.jbpt.bp.BehaviouralProfile;
import org.jbpt.bp.RelSetType;
import org.jbpt.bp.construct.BPCreatorNet;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;

public class BP extends AbstractProfile {
    BehaviouralProfile<NetSystem, Node> relations;


    public BP(NetSystem net){
        BPCreatorNet creator = BPCreatorNet.getInstance();
        relations = creator.deriveRelationSet(net);
    }

    @Override
    public Relation getRelationBetween(Node n1, Node n2) {
        RelSetType rel = relations.getRelationForEntities(n1, n2);
        switch(rel){
            case Order:
            case Exclusive:
                return Relation.BP_EXCLUSIVE;
            case Interleaving:
                return Relation.BP_INTERLEAVING;
            case ReverseOrder:
                return Relation.BP_REVERSE_ORDER;
            case None:
                return Relation.NONE;
        }
        return null;
    }
}
