package bpm.profile;

import bpm.alignment.Result;
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
                return new Relation(Relation.RelationType.BP_ORDER);
            case Exclusive:
                return new Relation(Relation.RelationType.BP_EXCLUSIVE);
            case Interleaving:
                return new Relation(Relation.RelationType.BP_INTERLEAVING);
            case ReverseOrder:
                return new Relation(Relation.RelationType.BP_REVERSE_ORDER);
            case None:
                return new Relation(Relation.RelationType.NONE);
        }
        return null;
    }

    @Override
    public Result filterTemporaryTransitions(Result result) {
        // no temporary transitions were added, therefore nothing needs to be removed
        return result;
    }


    @Override
    public double getRelationSimilarity(Relation rel1, Relation rel2) {
        Relation.RelationType r1 = rel1.getType();
        Relation.RelationType r2 = rel2.getType();
        if (r1 == r2) {
            return 1.0;
        }else if(r1.equals(Relation.RelationType.BP_ORDER) || (r1.equals(Relation.RelationType.BP_REVERSE_ORDER) && r2.equals(Relation.RelationType.BP_INTERLEAVING))){
            return 0.5; // when it is order instead of interleaving we can argue that half of the assumption is true
        }else if(r1.equals(Relation.RelationType.BP_INTERLEAVING) && (r2.equals(Relation.RelationType.BP_ORDER) || r2.equals(Relation.RelationType.BP_REVERSE_ORDER))){
            return 0.5; // when it is order instead of interleaving we can argue that half of the assumption is true
        }else {
            return 0.0;
        }
    }

    @Override
    public String toString(){
        return relations.toString();
    }


}
