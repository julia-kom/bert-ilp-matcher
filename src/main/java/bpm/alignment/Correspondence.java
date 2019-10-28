package bpm.alignment;

import org.jbpt.petri.Node;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * This can even be used to map places. Transistions and Places are both nodes.
 */
public class Correspondence {
    private HashSet<Node> nodesNet1;
    private HashSet<Node> nodesNet2;

    /**
     * Use the Builder
     */
    private Correspondence(){}

    /**
     * Check if the correspondence is complex (1:n or m:n)
     * @return boolean
     */
    public boolean isComplexCorrespondence(){
        if(nodesNet1.size() > 1 || nodesNet2.size() > 1){
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


    /**
     * Add post building a node of net 1 to the correspondence
     * @param n1
     */
    public void addNet1Node(Node n1) {
        nodesNet1.add(n1);
    }

    /**
     * Add post building a node of net 2 to the correspondence
     * @param n2
     */
    public void addNet2Node(Node n2) {
        nodesNet2.add(n2);
    }

    @Override
    public String toString() {
        String s = "[";
        for(Node n1 : nodesNet1){
            s+= n1.getLabel() +"("+n1.getId()+");";
        }
        s = s.substring(0,s.length()-1);
        s += "] to [";
        for(Node n2 : nodesNet2){
            s+= n2.getLabel() +"("+n2.getId()+");";
        }
        s = s.substring(0,s.length()-1);
        s+="] \n";
        return s;
    }

    @Override
    public boolean equals(Object o) {
        // Same type
        if(!(o instanceof  Correspondence)){
            return false;
        }

        // Same size
        Correspondence c = (Correspondence) o;
        if(c.getNet1Nodes().size() != nodesNet1.size() || c.getNet2Nodes().size() != nodesNet2.size()){
            return false;
        }

        // All items equal
        for(Node n1 : nodesNet1){
            if(!c.getNet1Nodes().contains(n1)){
                return false;
            }
        }
        for(Node n2 : nodesNet2){
            if(!c.getNet2Nodes().contains(n2)){
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode(){
        return 1;
    }


    public static class Builder{
        private HashSet<Node> nodesNet1;
        private HashSet<Node> nodesNet2;

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
