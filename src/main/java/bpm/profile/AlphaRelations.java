package bpm.profile;

import bpm.alignment.Alignment;
import bpm.alignment.Result;
import bpm.matcher.Preprocessor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;
import org.jbpt.petri.PetriNet;
import org.jbpt.petri.Transition;
import org.jbpt.petri.behavior.ConcurrencyRelation;

import java.util.*;

public class AlphaRelations extends AbstractProfile {

    private NetSystem net;
    private HashMap<Transition, HashSet<Transition>> directlyFollows = new HashMap<>(); // contains the directly follows set of each transition
    private ConcurrencyRelation concurrencyRelation;

    public AlphaRelations(NetSystem net){
        if (!PetriNet.STRUCTURAL_CHECKS.isWorkflowNet(net)) throw new IllegalArgumentException();
        this.concurrencyRelation = new ConcurrencyRelation(net);
        for(Transition t : net.getTransitions()){
            directlyFollows.put(t,computeDirectlyFollows(t,net));
        }

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
        ImmutablePair<Node,Node> nodePair = new ImmutablePair<>(n1,n2);
        if(super.computedRelations.containsKey(nodePair)){
            return computedRelations.get(nodePair);
        }
        Relation rel;
        if(concurrencyRelation.areConcurrent(n1,n2) || (directlyFollows.get(n1).contains(n2) && directlyFollows.get(n2).contains(n1))){
            //either two transitions are concurrent or they are in a 1-loop
            rel =  Relation.ALPHA_INTERLEAVING;
        }else if(directlyFollows.get(n1).contains(n2) && !directlyFollows.get(n2).contains(n1)) {
            //n1 can follow n2 but n2 never follow n1
            rel = Relation.ALPHA_ORDER;
        }else if(!directlyFollows.get(n1).contains(n2) && directlyFollows.get(n2).contains(n1)) {
            //n2 can follow n1 but never the other way around
            rel = Relation.ALPHA_REVERSE_ORDER;
        }else {
            rel = Relation.ALPHA_EXCLUSIVE;
        }
        super.computedRelations.put(nodePair,rel);
        return rel;

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
     * @param net
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


    /**
     * Computes the directly follows set of transitions for each transition in net
     * If there is a path of only tau transitions between these transitions then this is added too
     *
     * @param n1
     * @param net
     * @return
     */
    private HashSet<Transition> computeDirectlyFollows(Node n1, NetSystem net) {
        // places relation is irrelevant
        if (!(n1 instanceof Transition)) {
            return new HashSet<>();
        }

        //tau transitions relation is irrelevant
        if (Preprocessor.isTau((Transition) n1)) {
            return new HashSet<>();
        }
        HashSet<Transition> directlyFollowing = new HashSet<>();
        HashSet<Transition> visited = new HashSet<>();
        LinkedList<Transition> pending = new LinkedList<>();
        pending.add((Transition) n1);
        while (!pending.isEmpty()) {
            // get first from linked list and put it into visited list
            Transition current = pending.poll();
            visited.add(current);

            //fetch directly following transitions
            Set<Transition> tmpFollowing = net.getPostsetTransitions(net.getPostset(current));

            // add tau transitions in the follow up set to the pending set
            for (Transition t : tmpFollowing) {
                if (Preprocessor.isTau(t) && !visited.contains(t)) {
                    pending.add(t);
                }else{
                    directlyFollowing.addAll(tmpFollowing);
                }

            }
        }

        return directlyFollowing;
    }


}

