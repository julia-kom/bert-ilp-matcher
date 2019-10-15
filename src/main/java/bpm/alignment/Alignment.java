package bpm.alignment;

import org.jbpt.petri.Node;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Alignment {
    private Set<Correspondence> correspondences;

    /**
     * Use builder
     */
    private Alignment(){ }

    /**
     * Check if the correspondences are mutually exclusive.
     * Meaning if there no two correspondences in the alignment which share at least one transaction.
     * @return
     */
    public boolean isMutuallyExclusive(){
        for(Correspondence c1 : correspondences){
            for(Correspondence c2: correspondences){
                if (c1 != c2){
                    if(!Collections.disjoint(c1.getNet1Nodes(),c2.getNet1Nodes()) ||
                            !Collections.disjoint(c1.getNet2Nodes(),c2.getNet2Nodes())){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Checks if two nodes are in correspondence relation
     * @param n1
     * @param n2
     * @return boolean
     */
    public boolean isMapped(Node n1, Node n2){
        for(Correspondence c : correspondences){
            if(c.getNet1Nodes().contains(n1) && c.getNet2Nodes().contains(n2)){
                return true;
            }
        }
        return false;
    }

    /**
     * Check if the alignment contains complex correspondences
     * @return boolean
     */
    public boolean isComplex(){
        for(Correspondence c : correspondences){
            if(c.isComplexCorrespondence()){
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a set of correspondences where a certain node participates inside
     * @param n node
     * @return set of correspondences
     */
    public Set<Correspondence> getCorrespondenceOfNode(Node n){
        Set<Correspondence> set = new HashSet<>();
        for(Correspondence c : correspondences){
            if(c.getNet1Nodes().contains(n) || c.getNet2Nodes().contains(n)){
                set.add(c);
            }
        }
        return set;
    }

    /**
     * Get all correspondences
     * @return
     */
    public Set<Correspondence> getCorrespondences(){
        return correspondences;
    }



    public static class Builder{

        private Set<Correspondence> correspondences;

        /**
         * Create a new builder for alignments
         */
        public Builder(){
            correspondences = new HashSet<>();
        }

        /**
         * Add a new Correspondence to the alignment
         * @param c the correspondence to add
         * @return Builder
         */
        public Builder addCorrespondence(Correspondence c){
            correspondences.add(c);
            return this;
        }

        /**
         * Build the alignment
         * @return Alignment
         */
        public Alignment build(){
            Alignment a = new Alignment();
            a.correspondences = this.correspondences;
            return a;
        }

    }
}
