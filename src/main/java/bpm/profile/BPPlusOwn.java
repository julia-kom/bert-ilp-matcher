package bpm.profile;

import bpm.alignment.Alignment;
import bpm.alignment.Correspondence;
import bpm.alignment.Result;
import org.jbpt.algo.graph.ReflexiveTransitiveClosure;
import org.jbpt.algo.graph.TransitiveClosure;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;
import org.jbpt.petri.Place;
import org.jbpt.petri.Transition;
import java.util.*;
import static bpm.matcher.Preprocessor.isTau;
import static bpm.profile.AlphaRelations.directlyFollows;


/**
 * This class implements the BP+ profile from  "BP+: An Improved Behavioral Profile Metric for Process Models"
 */
public class BPPlusOwn extends AbstractProfile {

    private NetSystem net;
    private HashMap<Node, HashSet<Node>> preSet = new HashMap<>(); // contains the pre set of each transition
    private TransitiveClosure reachabilty;
    private Transition ARTIFICIAL_START = new Transition("ARTIFICIAL-START");
    private Transition ARTIFICIAL_END = new Transition("ARTIFICIAL-END");

    /**
     * Extends the petri net by one transition in the front (NEW_TS) and one in the back (NEW_TE)
     * @param net
     */
    public BPPlusOwn(NetSystem net){
        this.net = net;
        addArtificalStartEndTransitions();
        this.reachabilty  = new ReflexiveTransitiveClosure(net);
        computePre();
    }

    /**
     * Returns that node which is artificially added by BP+ to guarantee uniqueness property of the profile
     * @return
     */
    public Transition getArtificialInitialTransition(){
        return ARTIFICIAL_START;
    }

    /**
     * Returns that node which is artificially added by BP+ to guarentee uniquness property of the profile
     * @return
     */
    public Transition getArtificialFinalTransition(){
        return ARTIFICIAL_END;
    }


    @Override
    public Relation getRelationForEntities(Node n1, Node n2) {
        //Causality
        if(areCausal(n1,n2)){
            if(directlyFollows(n1,n2,net)){
                return Relation.BPP_DIRECT_CAUSAL;
            }else{
                return Relation.BPP_INDIRECT_CAUSAL;
            }
        //Reverse Causality
        }else if(areReverseCausal(n1,n2)){
            if(directlyFollows(n2,n1,net)){
                return Relation.BPP_REVERSE_DIRECT_CAUSAL;
            }else{
                return Relation.BPP_REVERSE_INDIRECT_CAUSAL;
            }
        //Concurrent
        }else if(areConcurrent(n1,n2)){
            if(!conflictWithTau(n1,n2)){
                return Relation.BPP_ALWAYS_CONCURRENT;
            }else{
                return Relation.BPP_SOMETIMES_CONCURRENT;
            }
        //Conflict
        }else if(areConflict(n1,n2)){
            return Relation.BPP_CONFLICT;
        }
        return Relation.NONE;
    }


    private boolean areCausal(Node n1, Node n2){
       return  (!inLoop(n1,n2) && getLeCmPre(n1,n2).contains(n1) && !n1.equals(n2)) ||
               (inLoop(n1,n2) && !getLeCmPre(n1,n2).contains(n1) && !getLeCmPre(n1,n2).contains(n2));
    }

    private boolean areReverseCausal(Node n1, Node n2){
        return  (!inLoop(n1,n2) && getLeCmPre(n1,n2).contains(n2) && !n1.equals(n2));
    }

    private boolean areConcurrent(Node n1, Node n2){
        Set<Node> leCmPre = getLeCmPre(n1, n2);

        /*if(leCmPre.size() == 0){
            return false;
        }*/

        //nodes are not equal
        if(n1.equals(n2)){
            return false;
        }

        // n1,n2 not in leCmPre
        if(leCmPre.contains(n1) || leCmPre.contains(n2)){
            return false;
        }

        // lcPre subset of Transitions
        for (Node n : leCmPre){
            if( n instanceof Place){
                return false;
            }
        }
        return true;
    }

    private boolean areConflict(Node n1, Node n2){

        //n1#n1 if not in a loop
        if(n1.equals(n2) && inLoop(n1,n1)){
            return true;
        }

        Set<Node> leCmPre = getLeCmPre(n1, n2);
        // lcPre subset of Places
        boolean onlyP = true;
        for (Node n : leCmPre){
            if( n instanceof Transition){
                onlyP = false;
                break;
            }
        }

        return ((!n1.equals(n2) && onlyP) || (n1.equals(n2) && !inLoop(n1,n2)));
    }

    /**
     * Returns true if there exists a tau transition which is in conflict with one of the given nodes
     * @param n1
     * @param n2
     * @return
     */
    private boolean conflictWithTau(Node n1, Node n2){
        for(Transition t : net.getTransitions()){
            if(isTau(t)){
               if(areConflict(n1,t) ||areConflict(n2,t)){
                   return true;
               }
            }
        }
        return false;
    }

    /**
     * Compute the Pre Set of the net
     */
    private void computePre(){
        for(Node n1 : net.getNodes()){
            HashSet<Node> preN1 = new HashSet<>();
            for(Node n2 : net.getNodes()) {
                if(reachabilty.hasPath(n2,n1)){ // n2 in preset of n1?
                    preN1.add(n2);
                }
            }
            preSet.put(n1,preN1);
        }
        //System.out.print(reachabilty.toString());
    }


    /**
     * Get Common Pre Set of the two given transitions
     * @param t1
     * @param t2
     * @return
     */
    private Set<Node> getCmPre(Node t1, Node t2){
        Set<Node> result;
        Set<Node> preT1 = getPre(t1);
        Set<Node> preT2 = getPre(t2);
        result = new HashSet<>(preT1);
        result.retainAll(preT2);
        return result;
    }

    /**
     * Derive the least common preset from of given preset
     * @param nodes preset of two transitions
     */
    private Set<Node> getLeCmPre(Set<Node> nodes){
        HashSet<Node> leCmPre = new HashSet<>(nodes);
        for(Node n : nodes){
            Set<Node> nPre = new HashSet<>(getPre(n));
            nPre.remove(n);
            leCmPre.removeAll(nPre);
        }
        return leCmPre;
    }

    /**
     * Get least Common Pre Set of the two given transitions
     * @param n1
     * @param n2
     * @return
     */
    private Set<Node> getLeCmPre(Node n1, Node n2){
       return getLeCmPre(getCmPre(n1,n2));

    }

    /**
     * Check if two transitions are in the same loop or not
     * @param t1
     * @param t2
     * @return
     */
    private boolean inLoop( Node t1, Node t2){
        //boolean loop = getPre(t1).contains(t2) && getPre(t2).contains(t1);
        boolean loop = reachabilty.hasPath(t1,t2) && reachabilty.hasPath(t2,t1);
        return loop;
    }


    private Set<Node> getPre(Node t){
        return preSet.get(t);
    }

    /**
     * Add artificial start and end transitions to the regarded net
     */
    private void addArtificalStartEndTransitions(){
        // insert new transition at the front of the WF net
        net.addTransition(ARTIFICIAL_START);
        Place pInitOld = net.getMarkedPlaces().iterator().next();
        Place pInitNew = new Place();
        net.addPlace(pInitNew);

        // remove current marking
        Set<Place> marked = net.getMarkedPlaces();
        for(Place p : marked){
            net.putTokens(p,0);
        }
        // add token to new init place and connect transition and places
        net.putTokens(pInitNew,1);
        net.addEdge(pInitNew,ARTIFICIAL_START);
        net.addEdge(ARTIFICIAL_START,pInitOld);

        // append new transition at the end of the WF net
        net.addTransition(ARTIFICIAL_END);
        Place pEndOld = net.getSinkPlaces().iterator().next();
        Place pEndNew = new Place();
        net.addPlace(pEndNew);
        net.addEdge(pEndOld,ARTIFICIAL_END);
        net.addEdge(ARTIFICIAL_END,pEndNew);

    }

    /**
     * Matching Penalty for BP+ acc to "BP+: An Improved Behavioral Profile Metric for Process Models"
     * @param r1 Relation 1
     * @param r2 Relation 2
     * @return double similarity of the two relations
     */
    @Override
    public double getRelationSimilarity(Relation r1, Relation r2) {
        if(r1 == r2){
            return 1.0;
        }else if((r1 == Relation.BPP_DIRECT_CAUSAL && r2 == Relation.BPP_INDIRECT_CAUSAL) ||
                (r2 == Relation.BPP_DIRECT_CAUSAL && r1 == Relation.BPP_INDIRECT_CAUSAL) ||
                (r1 == Relation.BPP_REVERSE_DIRECT_CAUSAL && r2 == Relation.BPP_REVERSE_INDIRECT_CAUSAL) ||
                (r2 == Relation.BPP_REVERSE_DIRECT_CAUSAL && r1 == Relation.BPP_REVERSE_INDIRECT_CAUSAL)){
            return 0.75;
        }else if((r1 == Relation.BPP_DIRECT_CAUSAL && r2 == Relation.BPP_ALWAYS_CONCURRENT) ||
                (r2 == Relation.BPP_DIRECT_CAUSAL && r1 == Relation.BPP_ALWAYS_CONCURRENT) ||
                (r1 == Relation.BPP_REVERSE_DIRECT_CAUSAL && r2 == Relation.BPP_ALWAYS_CONCURRENT) ||
                (r2 == Relation.BPP_REVERSE_DIRECT_CAUSAL && r1 == Relation.BPP_ALWAYS_CONCURRENT)){
            return 0.5;
        }else if((r1 == Relation.BPP_INDIRECT_CAUSAL && r2 == Relation.BPP_ALWAYS_CONCURRENT) ||
                (r2 == Relation.BPP_INDIRECT_CAUSAL && r1 == Relation.BPP_ALWAYS_CONCURRENT) ||
                (r1 == Relation.BPP_REVERSE_INDIRECT_CAUSAL && r2 == Relation.BPP_ALWAYS_CONCURRENT) ||
                (r2 == Relation.BPP_REVERSE_INDIRECT_CAUSAL && r1 == Relation.BPP_ALWAYS_CONCURRENT)){
            return 0.25;
        }else if((r1 == Relation.BPP_ALWAYS_CONCURRENT && r2 == Relation.BPP_SOMETIMES_CONCURRENT) ||
                (r2 == Relation.BPP_ALWAYS_CONCURRENT && r1 == Relation.BPP_SOMETIMES_CONCURRENT)){
            return 0.9;
        }else if((r1 == Relation.BPP_DIRECT_CAUSAL && r2 == Relation.BPP_SOMETIMES_CONCURRENT) ||
            (r2 == Relation.BPP_DIRECT_CAUSAL && r1 == Relation.BPP_SOMETIMES_CONCURRENT) ||
            (r1 == Relation.BPP_REVERSE_DIRECT_CAUSAL && r2 == Relation.BPP_SOMETIMES_CONCURRENT) ||
            (r2 == Relation.BPP_REVERSE_DIRECT_CAUSAL && r1 == Relation.BPP_SOMETIMES_CONCURRENT)){
        return 0.49;
        }else if((r1 == Relation.BPP_INDIRECT_CAUSAL && r2 == Relation.BPP_SOMETIMES_CONCURRENT) ||
            (r2 == Relation.BPP_INDIRECT_CAUSAL && r1 == Relation.BPP_SOMETIMES_CONCURRENT) ||
            (r1 == Relation.BPP_REVERSE_INDIRECT_CAUSAL && r2 == Relation.BPP_SOMETIMES_CONCURRENT) ||
            (r2 == Relation.BPP_REVERSE_INDIRECT_CAUSAL && r1 == Relation.BPP_SOMETIMES_CONCURRENT)){
        return 0.24;
    }
        return 0.0;
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

    /**
     * Filters out those correspondences which contain at least one temporary transition
     * @param result
     * @return
     */
    @Override
    public Result filterTemporaryTransitions(Result result){
        Alignment alignment = result.getAlignment();
        Iterator<Correspondence> iterator = alignment.getCorrespondences().iterator();

        while(iterator.hasNext()){
            boolean containsArtificalTransition = false;
            Correspondence c = iterator.next();
            //remove if NEW_TS or NEW_TE is part of the correspondence (net one)
            for(Node n : c.getNet1Nodes()){
                if(n.equals(ARTIFICIAL_START)|| n.equals(ARTIFICIAL_END)){
                    containsArtificalTransition = true;
                    continue;
                }
            }
            //same for net two
            for(Node n : c.getNet2Nodes()){
                if(n.equals(ARTIFICIAL_START)|| n.equals(ARTIFICIAL_END)){
                    containsArtificalTransition = true;
                    continue;
                }
            }

            if(containsArtificalTransition){
                iterator.remove();
            }
        }
        return new Result(result.getSimilarity(), alignment, result.getGAP());
    }

}
