package bpm.ippm.profile;

import bpm.ippm.alignment.Alignment;
import bpm.ippm.alignment.Correspondence;
import bpm.ippm.alignment.Result;
import com.iise.shudi.exroru.RefinedOrderingRelation;
import com.iise.shudi.exroru.RefinedOrderingRelationsMatrix;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;
import org.jbpt.petri.Transition;
import org.jbpt.petri.unfoldingBPP.CompletePrefixUnfoldingSetup;
import org.jbpt.petri.unfoldingBPP.order.AdequateOrderType;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.iise.shudi.exroru.RefinedOrderingRelationsMatrix.NEW_TE;
import static com.iise.shudi.exroru.RefinedOrderingRelationsMatrix.NEW_TS;

/**
 * Wrapper Class for BP+
 * This class wrapps the actual paper implementation of "BP+: An Improved Behavioral Profile Metric for Process Models"
 * The implementation was extended in the following way:
 *  - The package name of the changed jbpt package org.jbpt.petri.unfold was renamed so it is compatible with this project
 *  - Transitions are no longer addressed via their lables but their ID to allow for multiple same labeled transitions inside one net.
 */
@Deprecated
public class BPPlus extends AbstractProfile {
    RefinedOrderingRelationsMatrix rorm;
    NetSystem net;

    /**
     * Extends the petri net by one transition in the front (NEW_TS) and one in the back (NEW_TE)
     * @param net
     */
    public BPPlus(NetSystem net){
        RefinedOrderingRelation.IMPORTANCE = false;
        CompletePrefixUnfoldingSetup cpu = new CompletePrefixUnfoldingSetup();
        cpu.SAFE_OPTIMIZATION = true; // not that this parameter would change anything
        cpu.ADEQUATE_ORDER = AdequateOrderType.ESPARZA_FOR_SAFE_SYSTEMS;

        rorm = new RefinedOrderingRelationsMatrix(net,cpu);

        this.net = net;
    }

    /**
     * Returns that node which is artificially added by BP+ to guarantee uniqueness property of the profile
     * @return
     */
    public Transition getArtificialInitialTransition(){
        Set<Transition> transitions = net.getTransitions();
        for( Transition t : transitions){
            if(t.getLabel().equals(NEW_TS)){
                return t;
            }
        }
        throw new Error("No artificial initial transition in net " + net.getName());
    }

    /**
     * Returns that node which is artificially added by BP+ to guarentee uniquness property of the profile
     * @return
     */
    public Transition getArtificialFinalTransition(){
        Set<Transition> transitions = net.getTransitions();
        for( Transition t : transitions){
            if(t.getLabel().equals(NEW_TE)){
                return t;
            }
        }
        throw new Error("No artificial final transition in net " + net.getName());
    }


    @Override
    public Relation getRelationForEntities(Node n1, Node n2) {

        // convert Id to row/column number in the relational matrix
        List<String> tId1 = rorm.gettId();
        int i1 = tId1.indexOf(n1.getId());
        int i2 = tId1.indexOf(n2.getId());

        // fetch base relations
        RefinedOrderingRelation causalRel = rorm.getCausalMatrix()[i1][i2];
        RefinedOrderingRelation reverseCausalRel = rorm.getInverseCausalMatrix()[i2][i1]; // TODO this is a workaround as adjacency relation is not properly included in the inversecausal relation. In RefinedOrderingRelationClass switch key and value in line 674
        RefinedOrderingRelation invCausalRel = rorm.getInverseCausalMatrix()[i1][i2];
        RefinedOrderingRelation concurrentRel = rorm.getConcurrentMatrix()[i1][i2];

        // derive matrix entry from base relations
        if(causalRel.getRelation() != com.iise.shudi.exroru.Relation.NEVER) {
            if(causalRel.isAdjacency()){
                return new Relation(Relation.RelationType.BPP_DIRECT_CAUSAL);
            }else{
                return new Relation(Relation.RelationType.BPP_INDIRECT_CAUSAL);
            }
        }else if(invCausalRel.getRelation() != com.iise.shudi.exroru.Relation.NEVER){
            if(invCausalRel.isAdjacency() || reverseCausalRel.isAdjacency() ){
                return new Relation(Relation.RelationType.BPP_REVERSE_DIRECT_CAUSAL);
            }else{
                return new Relation(Relation.RelationType.BPP_REVERSE_INDIRECT_CAUSAL);
            }

        }else if(concurrentRel.getRelation() != com.iise.shudi.exroru.Relation.NEVER){
            if(concurrentRel.getRelation() == com.iise.shudi.exroru.Relation.SOMETIMES){
                return new Relation(Relation.RelationType.BPP_SOMETIMES_CONCURRENT);
            }else{
                return new Relation(Relation.RelationType.BPP_ALWAYS_CONCURRENT);
            }
        }
        return new Relation(Relation.RelationType.BPP_CONFLICT);
    }


    /**
     * Matching Penalty for BP+ acc to "BP+: An Improved Behavioral Profile Metric for Process Models"
     * @param rel1 Relation 1
     * @param rel2 Relation 2
     * @return double similarity of the two relations
     */
    @Override
    public double getRelationSimilarity(Relation rel1, Relation rel2) {
        Relation.RelationType r1 = rel1.getType();
        Relation.RelationType r2 = rel2.getType();
        if(r1 == r2){
            return 1.0;
        }else if((r1 == Relation.RelationType.BPP_DIRECT_CAUSAL && r2 == Relation.RelationType.BPP_INDIRECT_CAUSAL) ||
                (r2 == Relation.RelationType.BPP_DIRECT_CAUSAL && r1 == Relation.RelationType.BPP_INDIRECT_CAUSAL) ||
                (r1 == Relation.RelationType.BPP_REVERSE_DIRECT_CAUSAL && r2 == Relation.RelationType.BPP_REVERSE_INDIRECT_CAUSAL) ||
                (r2 == Relation.RelationType.BPP_REVERSE_DIRECT_CAUSAL && r1 == Relation.RelationType.BPP_REVERSE_INDIRECT_CAUSAL)){
            return 0.75;
        }else if((r1 == Relation.RelationType.BPP_DIRECT_CAUSAL && r2 == Relation.RelationType.BPP_ALWAYS_CONCURRENT) ||
                (r2 == Relation.RelationType.BPP_DIRECT_CAUSAL && r1 == Relation.RelationType.BPP_ALWAYS_CONCURRENT) ||
                (r1 == Relation.RelationType.BPP_REVERSE_DIRECT_CAUSAL && r2 == Relation.RelationType.BPP_ALWAYS_CONCURRENT) ||
                (r2 == Relation.RelationType.BPP_REVERSE_DIRECT_CAUSAL && r1 == Relation.RelationType.BPP_ALWAYS_CONCURRENT)){
            return 0.5;
        }else if((r1 == Relation.RelationType.BPP_INDIRECT_CAUSAL && r2 == Relation.RelationType.BPP_ALWAYS_CONCURRENT) ||
                (r2 == Relation.RelationType.BPP_INDIRECT_CAUSAL && r1 == Relation.RelationType.BPP_ALWAYS_CONCURRENT) ||
                (r1 == Relation.RelationType.BPP_REVERSE_INDIRECT_CAUSAL && r2 == Relation.RelationType.BPP_ALWAYS_CONCURRENT) ||
                (r2 == Relation.RelationType.BPP_REVERSE_INDIRECT_CAUSAL && r1 == Relation.RelationType.BPP_ALWAYS_CONCURRENT)){
            return 0.25;
        }else if((r1 == Relation.RelationType.BPP_ALWAYS_CONCURRENT && r2 == Relation.RelationType.BPP_SOMETIMES_CONCURRENT) ||
                (r2 == Relation.RelationType.BPP_ALWAYS_CONCURRENT && r1 == Relation.RelationType.BPP_SOMETIMES_CONCURRENT)){
            return 0.9;
        }else if((r1 == Relation.RelationType.BPP_DIRECT_CAUSAL && r2 == Relation.RelationType.BPP_SOMETIMES_CONCURRENT) ||
            (r2 == Relation.RelationType.BPP_DIRECT_CAUSAL && r1 == Relation.RelationType.BPP_SOMETIMES_CONCURRENT) ||
            (r1 == Relation.RelationType.BPP_REVERSE_DIRECT_CAUSAL && r2 == Relation.RelationType.BPP_SOMETIMES_CONCURRENT) ||
            (r2 == Relation.RelationType.BPP_REVERSE_DIRECT_CAUSAL && r1 == Relation.RelationType.BPP_SOMETIMES_CONCURRENT)){
        return 0.49;
        }else if((r1 == Relation.RelationType.BPP_INDIRECT_CAUSAL && r2 == Relation.RelationType.BPP_SOMETIMES_CONCURRENT) ||
            (r2 == Relation.RelationType.BPP_INDIRECT_CAUSAL && r1 == Relation.RelationType.BPP_SOMETIMES_CONCURRENT) ||
            (r1 == Relation.RelationType.BPP_REVERSE_INDIRECT_CAUSAL && r2 == Relation.RelationType.BPP_SOMETIMES_CONCURRENT) ||
            (r2 == Relation.RelationType.BPP_REVERSE_INDIRECT_CAUSAL && r1 == Relation.RelationType.BPP_SOMETIMES_CONCURRENT)){
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
                if(n.getLabel().equals(NEW_TS) || n.getLabel().equals(NEW_TE) ){
                    containsArtificalTransition = true;
                    continue;
                }
            }
            //same for net two
            for(Node n : c.getNet2Nodes()){
                if(n.getLabel().equals(NEW_TS) || n.getLabel().equals(NEW_TE)){
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
