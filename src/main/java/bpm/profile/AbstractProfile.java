package bpm.profile;

import bpm.alignment.Result;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jbpt.petri.Node;

import java.util.HashMap;


public abstract class AbstractProfile {



    public enum Profile{
        BP, // Behavioral Profile
        BPP, // Causal Behavioral Profile
        ARP, // Alpha Relational Profile
        LOG_DF, // Log based Directly Follows
        LOG_EF // Log based Eventually follows
    }

    HashMap<ImmutablePair<Node,Node>, Relation> computedRelations = new HashMap<>();

    public abstract Relation getRelationForEntities(Node n1, Node n2);

    public abstract double getRelationSimilarity(Relation r1, Relation r2);

    public abstract Result filterTemporaryTransitions(Result result);

}
