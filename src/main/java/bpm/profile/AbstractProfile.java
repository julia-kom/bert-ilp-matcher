package bpm.profile;

import org.jbpt.petri.Node;



public abstract class AbstractProfile {

    public enum Profile{
        BP, // Behavioral Profile
        BPP, // Causal Behavioral Profile
        ARP // Alpha Relational Profile
    }

    public enum Relation{
        BP_ORDER,
        BP_EXCLUSIVE,
        BP_INTERLEAVING,
        BP_REVERSE_ORDER,
        NONE,
        BPP_DIRECT_CAUSAL,
        BPP_REVERSE_DIRECT_CAUSAL,
        BPP_INDIRECT_CAUSAL,
        BPP_REVERSE_INDIRECT_CAUSAL,
        BPP_CONFLICT,
        BPP_SOMETIMES_CONCURRENT,
        BPP_ALWAYS_CONCURRENT,
        ALPHA_ORDER,
        ALPHA_EXCLUSIVE,
        ALPHA_INTERLEAVING,
        ALPHA_REVERSE_ORDER,
    }

    public abstract Relation getRelationForEntities(Node n1, Node n2);

    public abstract double getRelationSimilarity(Relation r1, Relation r2);

}
