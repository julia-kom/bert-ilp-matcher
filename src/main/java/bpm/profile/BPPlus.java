package bpm.profile;

import com.iise.shudi.exroru.RefinedOrderingRelation;
import com.iise.shudi.exroru.RefinedOrderingRelationsMatrix;
import com.iise.shudi.exroru.Relation;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;
import org.jbpt.petri.Transition;

import java.util.Collection;
import java.util.List;

/**
 * Wrapper Class for BP+
 */
public class BPPlus extends AbstractProfile {
    RefinedOrderingRelationsMatrix rorm;
    NetSystem net;
    public BPPlus(NetSystem net){
        rorm = new RefinedOrderingRelationsMatrix(net);
        this.net = net;
    }


    @Override
    public Relation getRelationBetween(Node n1, Node n2) {
        List<String> tName1 = rorm.gettName();

        //when package is updated change to id
        int i1 = tName1.indexOf(n1.getLabel());
        int i2 = tName1.indexOf(n2.getLabel());

        RefinedOrderingRelation causalRel = rorm.getCausalMatrix()[i1][i2];
        RefinedOrderingRelation invCausalRel = rorm.getInverseCausalMatrix()[i1][i2];
        RefinedOrderingRelation concurrentRel = rorm.getConcurrentMatrix()[i1][i2];

        if(causalRel.getRelation() != com.iise.shudi.exroru.Relation.NEVER) {
            if(causalRel.isAdjacency()){
                return Relation.BPP_DIRECT_CAUSAL;
            }else{
                return Relation.BPP_INDIRECT_CAUSAL;
            }
        }else if(invCausalRel.getRelation() != com.iise.shudi.exroru.Relation.NEVER){
            if(invCausalRel.isAdjacency()){
                return Relation.BPP_REVERSE_DIRECT_CAUSAL;
            }else{
                return Relation.BPP_REVERSE_INDIRECT_CAUSAL;
            }

        }else if(concurrentRel.getRelation() != com.iise.shudi.exroru.Relation.NEVER){
            if(concurrentRel.getRelation() == com.iise.shudi.exroru.Relation.SOMETIMES){
                return Relation.BPP_SOMETIMES_CONCURRENT;
            }else{
                return Relation.BPP_ALWAYS_CONCURRENT;
            }
        }
        return Relation.BPP_CONFLICT; // todo NONE or CONFLICT  ????? CONFLICT NOT EXPLICITLY DEFINED IN CODE.
    }

    @Override
    public double getRelationSimilarity(Relation r1, Relation r2) {
        //todo add table 2 from bp+ paper here
        return 1.0;
    }

    @Override
    public String toString(){
        String res = "";
        for(Transition t1 : net.getTransitions()){
            for(Transition t2 : net.getTransitions()){
                res += " | " + this.getRelationBetween(t1,t2);
            }
            res += "| \n";
        }
        return res;
    }

}
