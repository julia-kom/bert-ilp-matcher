package bpm.profile;

import bpm.alignment.Result;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;
import org.jbpt.petri.Transition;
import org.deckfour.xes.in.XesXmlParser;
import sun.nio.ch.Net;

import java.util.HashMap;

import static bpm.profile.AlphaRelations.directlyFollows;

public class EventuallyFollowsLogProfile extends AbstractProfile{
    NetSystem net;
    XLog log;
    HashMap<Transition,Integer> transitionFrequency;
    HashMap<ImmutablePair<Transition,Transition>, Integer> followFrequency;

    public EventuallyFollowsLogProfile(NetSystem net, XLog log){
        this.log = log;
        this.net = net;
        calculateFrequencies();
    }


    // So comparison of equals
    @Override
    public Relation getRelationForEntities(Node n1, Node n2) {
        return Relation.LOG_EVENTUALLY_FOLLOWS;
    }

    @Override
    public double getRelationSimilarity(Relation r1, Relation r2) {
        System.err.println("Get relation needs to be called with node information to calculate similarity");
        return -1;
    }

    @Override
    public double getRelationSimilarity(Relation r1, Relation r2, Node n1, Node n2, Node m1, Node m2){
       double nRelativeFreq = relativeFollowFrequencies(n1,n2);
       double mRelativeFreq = relativeFollowFrequencies(m1,m2);

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
        // nothing added, therfore nothing to filter out
        return result;
    }

    /**
     * Calculates the freuquencies (attributes) from the given log
     */
    private void calculateFrequencies(){
        //if no log information, then the frequency is always 1 if directly follows relation is satisfied in the model
        if(log == null || log.isEmpty()){
            //MODEL WITHOUT LOG
            //transition frequency is 1 for every transition
            for(Transition t : net.getTransitions()){
                this.transitionFrequency.put(t,1);
            }

            // Add a 1 if s and t are in a directly follows relation s > t
            for(Transition s : net.getTransitions()){
                for(Transition t : net.getTransitions()){
                    if(directlyFollows(s,t,net)) {
                        this.transitionFrequency.put(t, 1);
                    }
                }
            }

        }else{
            //traverse all events and count their frequency
            for(XTrace trace : log){
                int i;
                for(i = 0; i<trace.size()-1; i++){
                    //update transition frequency
                    transitionFrequency.put(trace.get(i), transitionFrequency.get(trace.get(i)));
                    for(int j = i+1; j<trace.size();j++) {
                        //update edge frequency
                        ImmutablePair edge = new ImmutablePair<Transition, Transition>(trace.get(i), trace.get(j));
                        followFrequency.put(edge, followFrequency.get(edge) + 1);
                    }
                }
                transitionFrequency.put(trace.get(i),transitionFrequency.get(trace.get(i))+1);

            }
        }

    }


    /**
     * Calculates the relative frequency that n1 followed by n2
     * @param n1
     * @param n2
     * @return
     */
    private double relativeFollowFrequencies(Node n1, Node n2){
        if(!(n1 instanceof Transition) || !(n2 instanceof Transition)){
            return 0;
        }
        ImmutablePair edge = new ImmutablePair<>(n1,n2);

        // if no entry was found
        if(!followFrequency.containsKey(edge) && !transitionFrequency.containsKey(n1)){
            return 0;
        }

        int freqTransition = followFrequency.get(edge);
        int freqN1 = transitionFrequency.get(n1);

        // special case that a transition is not occuring in the log for some reasons
        if(freqN1 == 0){
            return 0;
        }

        return ((double) freqTransition)/freqN1;
    }
}
