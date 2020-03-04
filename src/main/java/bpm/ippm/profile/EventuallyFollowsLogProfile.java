package bpm.ippm.profile;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.jbpt.bp.BehaviouralProfile;
import org.jbpt.bp.RelSetType;
import org.jbpt.bp.construct.BPCreatorNet;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;
import org.jbpt.petri.Transition;
import java.util.HashSet;


public class EventuallyFollowsLogProfile extends AbstractLogProfile{

    public EventuallyFollowsLogProfile(NetSystem net, XLog log){
        super();
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


    /**
     * Calculates the frequencies (attributes) from the given log
     */
    private void calculateFrequencies(){
        //if no log information, then the frequency is always 1 if directly follows relation is satisfied in the model
        if(log == null || log.isEmpty()){
            //MODEL WITHOUT LOG
            //transition frequency is 1 for every transition
            for(Transition t : net.getTransitions()){
                this.transitionFrequency.put(t,1);
            }

            // Add a 1 if s and t are in a eventually follows relation s > t (help of BP implementation)
            BPCreatorNet creator = BPCreatorNet.getInstance();
            BehaviouralProfile<NetSystem, Node> relations = creator.deriveRelationSet(net);
            // if it is in order, or interleaving, then eventually follows relation is satisfied.
            HashSet<RelSetType> eventuallyFollows = new HashSet<RelSetType>();
            eventuallyFollows.add(RelSetType.Order);
            eventuallyFollows.add(RelSetType.Interleaving);

            for(Transition s : net.getTransitions()){
                for(Transition t : net.getTransitions()){
                    if(eventuallyFollows.contains(relations.getRelationForEntities(s,t))) { //if in eventually follows relation
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
}
