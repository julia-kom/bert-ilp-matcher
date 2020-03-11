package bpm.ippm.profile;

import bpm.ippm.alignment.Result;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.deckfour.xes.model.XLog;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;

import java.util.HashMap;


public abstract class AbstractProfile {

    /**
     * Profiles to choose from
     */
    public enum Profile{
        BP, // Behavioral Profile
        BPP, // Causal Behavioral Profile, execute with ILP BASIC 5
        ARP, // Alpha Relational Profile
        LOG_DF, // Log based Directly Follows, execute with ilp BASIC 5
        LOG_EF // Log based Eventually follows, exectue with ilp BASIC 5
    }

    HashMap<ImmutablePair<Node,Node>, Relation> computedRelations = new HashMap<>();

    /**
     * Creates the profile for the given net with optional log support.
     * Note only the LOG_DF and LOG_EF actually make use of the log. And even there it is optionally.
     * @param net net to compute the profile for
     * @param profile profile tye to compute
     * @param log log to support
     * @return
     */
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
