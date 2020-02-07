package bpm.profile;

import bpm.alignment.Result;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.jbpt.bp.BehaviouralProfile;
import org.jbpt.bp.RelSetType;
import org.jbpt.bp.construct.BPCreatorNet;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;
import org.jbpt.petri.Transition;

import java.util.HashMap;
import java.util.HashSet;

public abstract class AbstractLogProfile extends AbstractProfile {

    public static final String LOG_ID_ATTRIBUTE = "action_code";

    NetSystem net;
    XLog log;
    HashMap<Transition,Integer> transitionFrequency = new HashMap<>();
    HashMap<ImmutablePair<Transition,Transition>, Integer> followFrequency = new HashMap<>();



    @Override
    public double getRelationSimilarity(Relation r1, Relation r2){
        double nRelativeFreq = r1.getFrequency();
        double mRelativeFreq = r2.getFrequency();

        //min-max-sim: min(mRelativeFreq, nRelativeFreq)/min(mRelativeFreq, nRelativeFreq)
        double min = Math.min(nRelativeFreq,mRelativeFreq);
        double max = Math.max(nRelativeFreq,mRelativeFreq);

        // when they are never in a relation in both cases, this relation is satisfied too
        if(min == 0 && max == 0){
            return 1;
        }

        return min/max;

    }

    @Override
    public Result filterTemporaryTransitions(Result result) {
        // nothing added, therefore nothing to filter out
        return result;
    }


    int getTransitionFrequency(Transition t){
        if(transitionFrequency.containsKey(t)){
            return transitionFrequency.get(t);
        }else{
            return 0;
        }
    }

    int getFollowsFrequency(ImmutablePair edge){
        if(followFrequency.containsKey(edge)){
            return followFrequency.get(edge);
        }else{
            return 0;
        }
    }


    /**
     * Calculates the relative frequency that n1 followed by n2
     * @param n1
     * @param n2
     * @return
     */
    double relativeFollowFrequencies(Node n1, Node n2){
        if(!(n1 instanceof Transition) || !(n2 instanceof Transition)){
            return 0;
        }
        ImmutablePair edge = new ImmutablePair<>(n1,n2);

        // if no entry was found
        if(!followFrequency.containsKey(edge) || !transitionFrequency.containsKey(n1)){
            return 0;
        }

        int freqFollow = followFrequency.get(edge);
        int freqN1 = transitionFrequency.get(n1);

        // special case that a transition is not occuring in the log for some reasons
        if(freqN1 == 0){
            return 0;
        }

        return ((double) freqFollow)/freqN1;
    }

}
