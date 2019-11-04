package bpm.profile;

import org.jbpt.petri.Node;



public abstract class AbstractProfile {
    public enum Relation{
        BP_ORDER,
        BP_EXCLUSIVE,
        BP_INTERLEAVING,
        BP_REVERSE_ORDER,
        NONE
    }

    public abstract Relation getRelationBetween(Node n1, Node n2);


}
