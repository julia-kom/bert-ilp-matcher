package bpm.alignment;

import org.jbpt.petri.Node;

import java.util.HashSet;
import java.util.Set;

public class Correspondence {
    private Set<Node> nodesNet1;
    private Set<Node> nodesNet2;

    /**
     * Use the Builder
     */
    private Correspondence(){}

    /**
     * Check if the correspondence is complex (1:n or m:n)
     * @return boolean
     */
    public boolean isComplexCorrespondence(){
        if(nodesNet1.size() > 1 ||nodesNet2.size() > 1){
            return true;
        }else{
            return false;
        }
    }

    /**
     * Get the nodes of net 1 which participate in that correspondence
     * @return HashSet of these nodes
     */
    public Set<Node> getNet1Nodes(){
        return nodesNet1;
    }

    /**
     * Get the nodes of net 2 which participate in that correspondence
     * @return HashSet of these nodes
     */
    public Set<Node> getNet2Nodes(){
        return nodesNet2;
    }

    public class Builder{
        private Set<Node> nodesNet1;
        private Set<Node> nodesNet2;

        public Builder(){
            nodesNet1 = new HashSet<>();
            nodesNet2 = new HashSet<>();
        }

        /**
         * Add a node from net 1 to the correspondence
         * @param nodeNet1 node from the first net
         * @return
         */
        public Builder addNodeFromNet1(Node nodeNet1){
            nodesNet1.add(nodeNet1);
            return this;
        }

        /**
         * Add a node from net 2 to the correspondence
         * @param nodeNet2 node from the second net to add
         * @return
         */
        public Builder addNodeFromNet2(Node nodeNet2) {
            nodesNet2.add(nodeNet2);
            return this;
        }
        
        /**
         * Build the Correspondence
         * @return
         */
        public Correspondence build(){
            //check if valid correspondance
            if(nodesNet1.size() < 1 ||nodesNet2.size() < 1){
                throw new IllegalStateException("Correspondence is of type 0:n");
            }
            Correspondence c = new Correspondence();
            c.nodesNet1 = this.nodesNet1;
            c.nodesNet2 = this.nodesNet2;
            return c;
        }

    }
}
