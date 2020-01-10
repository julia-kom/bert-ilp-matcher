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
    public Relation getRelationForEntities(Node n1, Node n2) {
        RelSetType rel = relations.getRelationForEntities(n1, n2);
        switch(rel){
            case Order:
                return Relation.BP_ORDER;
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

    @Override
    public double getRelationSimilarity(Relation r1, Relation r2) {
        if (r1 == r2) {
            return 1.0;
        }else {
            return 0.0;
        }
    }

    @Override
    public String toString(){
        return relations.toString();
    }


}
