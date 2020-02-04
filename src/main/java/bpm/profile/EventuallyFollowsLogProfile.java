package bpm.profile;

import bpm.alignment.Result;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;
import org.jbpt.petri.Transition;

import java.util.HashMap;

import static bpm.profile.AlphaRelations.directlyFollows;


public class EventuallyFollowsLogProfile extends AbstractProfile{

    public static final String LOG_ID_ATTRIBUTE = "action_code";

    NetSystem net;
    XLog log;
    HashMap<Transition,Integer> transitionFrequency = new HashMap<>();
    HashMap<ImmutablePair<Transition,Transition>, Integer> followFrequency = new HashMap<>();

    public EventuallyFollowsLogProfile(){

    }

    public EventuallyFollowsLogProfile(NetSystem net, XLog log){
        this.log = log;
        this.net = net;
        calculateFrequencies();
    }


    // So comparison of equals
    @Override
    public Relation getRelationForEntities(Node n1, Node n2) {
        double freq = relativeFollowFrequencies(n1,n2);
        Relation r = new Relation(Relation.RelationType.LOG_EVENTUALLY_FOLLOWS, freq);
        return r;
    }

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
                    if(directlyFollows(s,t,net)) { //TODO change to eventually follows
                        ImmutablePair<Transition,Transition> edge = new ImmutablePair(s,t);
                        this.followFrequency.put(edge, 1);
                    }
                }
            }

        }else{
            //traverse all events and count their frequency
            for(XTrace trace : log){
                int i;
                for(i = 0; i<trace.size()-1; i++){
                    Transition t1 = new Transition(trace.get(i).getAttributes().get(LOG_ID_ATTRIBUTE).toString());
                    t1.setId(trace.get(i).getAttributes().get(LOG_ID_ATTRIBUTE).toString());
                    //update transition frequency
                    transitionFrequency.put(t1, getTransitionFrequency(t1) + 1);
                    for(int j = i+1; j<trace.size();j++) {
                        Transition t2 = new Transition(trace.get(j).getAttributes().get(LOG_ID_ATTRIBUTE).toString());
                        t2.setId(trace.get(j).getAttributes().get(LOG_ID_ATTRIBUTE).toString());
                        //update edge frequency
                        ImmutablePair edge = new ImmutablePair<Transition, Transition>(t1, t2);
                        followFrequency.put(edge, getFollowsFrequency(edge) + 1);
                    }
                }
                Transition tLast = new Transition(trace.get(trace.size()-1).getAttributes().get(LOG_ID_ATTRIBUTE).toString());
                tLast.setId(trace.get(trace.size()-1).getAttributes().get(LOG_ID_ATTRIBUTE).toString());
                transitionFrequency.put(tLast,getTransitionFrequency(tLast) + 1);
            }
        }

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

        int freqTransition = followFrequency.get(edge);
        int freqN1 = transitionFrequency.get(n1);

        // special case that a transition is not occuring in the log for some reasons
        if(freqN1 == 0){
            return 0;
        }

        return ((double) freqTransition)/freqN1;
    }
}
