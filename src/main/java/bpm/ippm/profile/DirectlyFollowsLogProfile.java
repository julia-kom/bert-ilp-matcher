package bpm.ippm.profile;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;
import org.jbpt.petri.Transition;

import static bpm.ippm.profile.AlphaRelations.directlyFollows;

/**
 * Directly Follows Profile with optional Log enhancement
 */
public class DirectlyFollowsLogProfile extends AbstractLogProfile{

    /**
     * Construct aDirectly Follows Matrix Profile for given log and model.
     * If no log is preferred, and only the net should be used to compute the follow relations then set log to null
     * @param net Net to compute the profile for
     * @param log Log or optionally null if no log is present.
     */
    public DirectlyFollowsLogProfile(NetSystem net, XLog log){
        super();
        this.log = log;
        this.net = net;
        calculateFrequencies();
    }

    /**
     * Get relation between two nodes
     * @param n1 node n1
     * @param n2 node n2
     * @return Relation from n1 to n2
     */
    @Override
    public Relation getRelationForEntities(Node n1, Node n2) {
        double freq = relativeFollowFrequencies(n1,n2);
        Relation r = new Relation(Relation.RelationType.LOG_DIRECTLY_FOLLOWS, freq);
        return r;
    }

    /**
     * Calculates the directly follows frequencies (attributes) from the given log
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
                    //create dummy transitions. Note that equals() is implemented based on ID only
                    String id1 = trace.get(i).getAttributes().get(LOG_ID_ATTRIBUTE).toString() +"+"+
                            trace.get(i).getAttributes().get(LOG_LIFECYCLE_ATTRIBUTE).toString();
                    Transition t1 = new Transition(id1);
                    t1.setId(id1);

                    String id2 = trace.get(i+1).getAttributes().get(LOG_ID_ATTRIBUTE).toString() +"+"+
                            trace.get(i+1).getAttributes().get(LOG_LIFECYCLE_ATTRIBUTE).toString();
                    Transition t2 = new Transition(id2);
                    t2.setId(id2);
                    //update transition freq
                    transitionFrequency.put(t1,getTransitionFrequency(t1)+1);
                    //update follow freq
                    ImmutablePair edge = new ImmutablePair<Transition, Transition>(t1,t2);
                    followFrequency.put(edge,getFollowsFrequency(edge) + 1);
                }
                String idLast = trace.get(trace.size()-1).getAttributes().get(LOG_ID_ATTRIBUTE).toString() +"+"+
                        trace.get(trace.size()-1).getAttributes().get(LOG_LIFECYCLE_ATTRIBUTE).toString();
                Transition tLast = new Transition(idLast);
                tLast.setId(idLast);
                transitionFrequency.put(tLast,getTransitionFrequency(tLast)+1);
            }
        }

    }
}
