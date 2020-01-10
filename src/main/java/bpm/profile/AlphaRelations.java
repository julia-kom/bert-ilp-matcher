package bpm.profile;

import org.jbpt.bp.BehaviouralProfile;
import org.jbpt.bp.RelSetType;
import org.jbpt.bp.construct.AbstractRelSetCreator;
import org.jbpt.bp.construct.RelSetCreator;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Node;
import org.jbpt.petri.PetriNet;
import org.jbpt.petri.behavior.ConcurrencyRelation;

import java.util.Collection;

public class AlphaRelations extends AbstractProfile {

    private BehaviouralProfile<NetSystem, Node> relations;
    private static AlphaCreatorNet creator;

    public AlphaRelations(NetSystem net){
        if(creator == null){
            creator = new AlphaCreatorNet();
            relations.setLookAhead(1);
        }
        relations = creator.deriveRelationSet(net);
    }

    @Override
    public Relation getRelationForEntities(Node n1, Node n2) {
        RelSetType rel = relations.getRelationForEntities(n1, n2);
        switch(rel){
            case Order:
                return Relation.ALPHA_ORDER;
            case Exclusive:
                return Relation.ALPHA_EXCLUSIVE;
            case Interleaving:
                return Relation.ALPHA_INTERLEAVING;
            case ReverseOrder:
                return Relation.ALPHA_REVERSE_ORDER;
            case None:
                return Relation.NONE;
        }
        return null;
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
    public String toString(){
        return relations.toString();
    }

    /**
     * Structure as in BPCreatorNet.java in JBPT Lib
     * What has changed? An additional requeirment for a relation is added into the if-else part assigning relations to
     * an entry in the relational matrix: Direct Neighboor porperty must be satisfied too.
     */
    private class AlphaCreatorNet extends AbstractRelSetCreator implements RelSetCreator<NetSystem, Node> {


        private AlphaCreatorNet() {

        }

        public BehaviouralProfile<NetSystem, Node> deriveRelationSet(NetSystem pn) {
            return deriveRelationSet(pn, pn.getNodes());
        }

        public BehaviouralProfile<NetSystem, Node> deriveRelationSet(NetSystem pn, Collection<Node> nodes) {

            /*
             * Check some of the assumptions.
             */
            //if (!PetriNet.StructuralClassChecks.isExtendedFreeChoice(pn)) throw new IllegalArgumentException();
            if (!PetriNet.STRUCTURAL_CHECKS.isWorkflowNet(pn)) throw new IllegalArgumentException();

            BehaviouralProfile<NetSystem, Node> profile = new BehaviouralProfile<NetSystem, Node>(pn,nodes);
            RelSetType[][] matrix = profile.getMatrix();

            ConcurrencyRelation concurrencyRelation = new ConcurrencyRelation(pn);

            for(Node n1 : profile.getEntities()) {
                int index1 = profile.getEntities().indexOf(n1);
                for(Node n2 : profile.getEntities()) {
                    int index2 = profile.getEntities().indexOf(n2);
                    /*
                     * The matrix is symmetric. Therefore, we need to traverse only
                     * half of the entries.
                     */
                    if (index2 > index1)
                        continue;
                    /*
                     * What about the relation of a node to itself?
                     */
                    if (index1 == index2) {
                        if (PetriNet.DIRECTED_GRAPH_ALGORITHMS.hasPath(pn,n1,n2))
                            matrix[index1][index1] = RelSetType.Interleaving;
                        else
                            matrix[index1][index1] = RelSetType.Exclusive;
                    }
                    /*
                     * Check all cases for two distinct nodes of the net
                     */
                    //todo change conditions. That is the only thing to change! Only thing new: check if direct neighbors i.e. only tau transitions are on a path from source to target.
                    else if (PetriNet.DIRECTED_GRAPH_ALGORITHMS.hasPath(pn,n1,n2) && PetriNet.DIRECTED_GRAPH_ALGORITHMS.hasPath(pn,n2,n1)) {
                        super.setMatrixEntry(matrix,index1,index2,RelSetType.Interleaving);
                    }
                    else if (concurrencyRelation.areConcurrent(index1,index2)) {
                        super.setMatrixEntry(matrix,index1,index2,RelSetType.Interleaving);
                    }
                    else if (!concurrencyRelation.areConcurrent(index1,index2) && !PetriNet.DIRECTED_GRAPH_ALGORITHMS.hasPath(pn,n1,n2) && !PetriNet.DIRECTED_GRAPH_ALGORITHMS.hasPath(pn,n2,n1)) {
                        super.setMatrixEntry(matrix,index1,index2,RelSetType.Exclusive);
                    }
                    else if (PetriNet.DIRECTED_GRAPH_ALGORITHMS.hasPath(pn,n1,n2) && !PetriNet.DIRECTED_GRAPH_ALGORITHMS.hasPath(pn,n2,n1)) {
                        super.setMatrixEntryOrder(matrix,index1,index2);
                    }
                    else if (PetriNet.DIRECTED_GRAPH_ALGORITHMS.hasPath(pn,n2,n1) && !PetriNet.DIRECTED_GRAPH_ALGORITHMS.hasPath(pn,n1,n2)) {
                        super.setMatrixEntryOrder(matrix,index2,index1);
                    }
                }
            }

            return profile;
        }
    }



}
