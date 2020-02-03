package bpm.profile;

import bpm.alignment.Alignment;
import bpm.alignment.Result;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jbpt.petri.Node;

import java.util.HashMap;
import java.util.HashSet;


public abstract class AbstractProfile {



    public enum Profile{
        BP, // Behavioral Profile
        BPP, // Causal Behavioral Profile
        ARP, // Alpha Relational Profile
        LOG_DF, // Log based Directly Follows
        LOG_EF // Log based Eventually follows
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
        LOG_DIRECTLY_FOLLOWS,
        LOG_EVENTUALLY_FOLLOWS
    }

    HashMap<ImmutablePair<Node,Node>,Relation> computedRelations = new HashMap<>();

    public abstract Relation getRelationForEntities(Node n1, Node n2);

    public abstract double getRelationSimilarity(Relation r1, Relation r2);

    public abstract double getRelationSimilarity(Relation r1, Relation r2, Node n1, Node n2, Node m1, Node m2);

    public abstract Result filterTemporaryTransitions(Result result);

}
