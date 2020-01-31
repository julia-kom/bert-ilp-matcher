package bpm.profile;

import bpm.alignment.Alignment;
import bpm.alignment.Result;
import bpm.matcher.Preprocessor;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;
import org.jbpt.petri.PetriNet;
import org.jbpt.petri.Transition;
import org.jbpt.petri.behavior.ConcurrencyRelation;

import java.util.*;

public class AlphaRelations extends AbstractProfile {

    private NetSystem net;

    public AlphaRelations(NetSystem net){
        if (!PetriNet.STRUCTURAL_CHECKS.isWorkflowNet(net)) throw new IllegalArgumentException();
        this.net = net;
    }


    @Override
    public double getRelationSimilarity(Relation r1, Relation r2) {
        if (r1 == r2) {
            return 1.0;
        }else {
            return 0.0;
        }
    }

    @Override
    public Result filterTemporaryTransitions(Result res) {
        // no temporary transitions were added, therefore nothing needs to be removed
        return res;
    }

    @Override
    public String toString(){
        String res = "";
        for(Transition t1 : net.getTransitions()){
            for(Transition t2 : net.getTransitions()){
                res += " | " + this.getRelationForEntities(t1,t2);
            }
            res += "| \n";
        }
        return res;
    }

    @Override
    public Relation getRelationForEntities(Node n1, Node n2) {
        // Implementation is similar to BP but this time, with directly follows instead of eventually follows as ground.
        ConcurrencyRelation concurrencyRelation = new ConcurrencyRelation(net);
        if(concurrencyRelation.areConcurrent(n1,n2) || (directlyFollows(n1,n2,net) && directlyFollows(n2,n1,net))){
            //either two transitions are concurrent or they are in a 1-loop
            return Relation.ALPHA_INTERLEAVING;
        }else if(directlyFollows(n1,n2,net) && !directlyFollows(n2,n1,net)) {
            //n1 can follow n2 but n2 never follow n1
            return Relation.ALPHA_ORDER;
        }else if(!directlyFollows(n1,n2,net) && directlyFollows(n2,n1,net)) {
            //n2 can follow n1 but never the other way around
            return Relation.ALPHA_REVERSE_ORDER;
        }else{
            return Relation.ALPHA_EXCLUSIVE;
        }
    }


    /**
     * Returns true if two transitions are directly following each other
     * Or there is a path of only tau transitions between these transitions.
     *
     * Idea check all transitions which are directly following. If one of the transitions is tau check the
     * follow up transitions of that tau transition too.
     * Break at already visited transitions to prevent infinite loops.
     *
     * @param n1
     * @param n2
     * @return
     */
     static boolean directlyFollows(Node n1, Node n2, NetSystem net) {
        // places relation is irrelevant
        if (!(n1 instanceof Transition && n2 instanceof Transition)) {
            return false;
        }

        //tau transitions relation is irrelevant
        if (Preprocessor.isTau((Transition) n1) || Preprocessor.isTau((Transition) n2)) {
            return false;
        }
        Set<Transition> visited = new HashSet<>();
        LinkedList<Transition> pending = new LinkedList<>();
        pending.add((Transition) n1);
        while (!pending.isEmpty()) {
            // get first from linked list and put it into visited list
            Transition current = pending.poll();
            visited.add(current);

            //fetch directly following transitions
            Set<Transition> following = net.getPostsetTransitions(net.getPostset(current));

            // check if n2 is found in the follow-up set
            if (following.contains(n2)) {
                return true;
            }

            // add tau transitions in the follow up set to the pending set
            for (Transition t : following) {
                if (Preprocessor.isTau(t) && !visited.contains(t)) {
                    pending.add(t);
                }
            }
        }

        return false;
    }

}

