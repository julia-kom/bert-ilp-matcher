package bpm.alignment;

import org.jbpt.petri.Node;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Alignment {
    private HashSet<Correspondence> correspondences;
    private String name;

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
     * Contains a certain correspondence
     * @param c
     * @return
     */
    public boolean contains(Correspondence c){
        for(Correspondence p : correspondences){
            if( c.equals(p)){
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
    public HashSet<Correspondence> getCorrespondences(){
        return correspondences;
    }

    public String getName(){return name;}

    @Override
    public String toString(){
        String s = "";
        for(Correspondence c : correspondences) {
            s += c.toString();
        }
        return s;
    }



    public static class Builder{

        private HashSet<Correspondence> correspondences;

        /**
         * Create a new builder for alignments
         */
        public Builder(){
            correspondences = new HashSet<>();
        }

        /**
         * Add a new Correspondence to the alignment.
         * Do not use this to create alignments!!! Use add() instead to prevent non-mutally exclusivness
         * @param c the correspondence to add
         * @return Builder
         */
        public Builder addCorrespondence(Correspondence c){
            correspondences.add(c);
            return this;
        }

        /**
         * Adds pair of nodes to correspndence, such that no non-mutual exclusive alignments exist.
         * @param n1 node of net 1
         * @param n2 node of net 2
         * @return
         */
        public Builder add(Node n1, Node n2){
            boolean found = false;
            //check if either node is already in a correspondence and if so add the other node to that correspondece too
            for (Correspondence c : correspondences){
                if(c.getNet1Nodes().contains(n1)){
                    c.addNet2Node(n2);
                    found = true;
                    break;
                }
                if(c.getNet2Nodes().contains(n2)){
                    c.addNet1Node(n1);
                    found = true;
                    break;
                }
            }
            // if not: add a new correspondence
            if(!found){
                this.correspondences.add(new Correspondence.Builder().addNodeFromNet1(n1).addNodeFromNet2(n2).build());
            }
            return this;
        }

        /**
         * Removes complex matches from the alignment that is build up
         * @return
         */
        public Builder removeComplexMatches(){
            for(Correspondence c: correspondences){
                if (c.isComplexCorrespondence())
                    this.correspondences.remove(c);
            }
            return this;
        }

        /**
         * Build the alignment
         * @return Alignment
         */
        public Alignment build(String name){
            Alignment a = new Alignment();
            a.name = name;
            a.correspondences = this.correspondences;
            return a;
        }

    }
}
