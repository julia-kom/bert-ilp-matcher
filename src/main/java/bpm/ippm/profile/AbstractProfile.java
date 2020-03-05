package bpm.ippm.profile;

import bpm.ippm.alignment.Result;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.deckfour.xes.model.XLog;
import org.jbpt.petri.NetSystem;
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

    public static AbstractProfile createProfile(NetSystem net, Profile profile, XLog log){
        AbstractProfile r;
        switch(profile){
            case BP:
                r = new BP(net);
                break;
            case BPP:
                r = new BPPlusOwn(net);
                break;
            case ARP:
                r = new AlphaRelations(net);
                break;
            case LOG_DF:
                r = new DirectlyFollowsLogProfile(net,log);
                break;
            case LOG_EF:
                r = new EventuallyFollowsLogProfile(net,log);
                break;
            default:
                throw new UnsupportedOperationException("Operator not yet implemented: " + profile.toString());
        }
        return r;
    }

    public abstract Relation getRelationForEntities(Node n1, Node n2);

    public abstract double getRelationSimilarity(Relation r1, Relation r2);

    public abstract Result filterTemporaryTransitions(Result result);

}
