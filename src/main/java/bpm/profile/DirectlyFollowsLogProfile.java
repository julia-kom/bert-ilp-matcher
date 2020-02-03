package bpm.profile;

import bpm.alignment.Result;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.deckfour.xes.model.XTrace;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;
import org.jbpt.petri.Transition;

import java.util.HashMap;

import static bpm.profile.AlphaRelations.directlyFollows;

public class DirectlyFollowsLogProfile extends EventuallyFollowsLogProfile{
    HashMap<Transition,Integer> transitionFrequency;
    HashMap<ImmutablePair<Transition,Transition>, Integer> followFrequency;

    public DirectlyFollowsLogProfile(NetSystem net){
        calculateFrequencies();
    }


    @Override
    public Relation getRelationForEntities(Node n1, Node n2) {
        return Relation.LOG_DIRECTLY_FOLLOWS;
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
                    //update transition freq
                    transitionFrequency.put(trace.get(i),transitionFrequency.get(trace.get(i)));
                    //update follow freq
                    ImmutablePair edge = new ImmutablePair<Transition, Transition>(trace.get(i),trace.get(i+1));
                    followFrequency.put(edge,followFrequency.get(edge)+1);
                }
                transitionFrequency.put(trace.get(i),transitionFrequency.get(trace.get(i))+1);

            }
        }

    }
}
