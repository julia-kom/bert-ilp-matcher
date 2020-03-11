package bpm.ippm.profile;

import bpm.ippm.alignment.Result;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.deckfour.xes.model.XLog;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;
import org.jbpt.petri.Transition;

import java.util.HashMap;

/**
 * Abstract Log Profile. All Log based profiles derive this.
 */
public abstract class AbstractLogProfile extends AbstractProfile {

    // attribute that is mapped to the transition ID in the NetSystem in combination with the lifecycle
    public static final String LOG_ID_ATTRIBUTE = "action_code";
    public static final String LOG_LIFECYCLE_ATTRIBUTE = "lifecycle:transition";

    NetSystem net;
    XLog log;
    HashMap<Transition,Integer> transitionFrequency = new HashMap<>();
    HashMap<ImmutablePair<Transition,Transition>, Integer> followFrequency = new HashMap<>();


    /**
     * Min/Max Similarity of relative frequencies.
     * @param r1
     * @param r2
     * @return
     */
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

    /**
     * Get number of times an event/transition was executed
     * @param t Transition to check
     * @return number of times t was executed in the log
     */
    int getTransitionFrequency(Transition t){
        if(transitionFrequency.containsKey(t)){
            return transitionFrequency.get(t);
        }else{
            return 0;
        }
    }

    /**
     * Get number of times a certain tradition t was executed after a certain other transition s
     * @param edge (s,t) the two transitions  to check
     * @return number of times t was executed after s in the log
     */
    int getFollowsFrequency(ImmutablePair edge){
        if(followFrequency.containsKey(edge)){
            return followFrequency.get(edge);
        }else{
            return 0;
        }
    }


    /**
     * Calculates the relative frequency that n1 followed by n2
     * @param n1 Node n1
     * @param n2 Node n2
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

        // special case that a transition is not occurring in the log for some reasons
        if(freqN1 == 0){
            return 0;
        }

        return ((double) freqFollow)/freqN1;
    }

}
