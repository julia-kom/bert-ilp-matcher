package bpm.similarity;

import javafx.util.Pair;
import org.jbpt.petri.Node;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Matrix{
    private Node[] nodesNet1;
    private Node[] nodesNet2;
    private Map<Pair<String,String>, Double> similarities;

    /**
     * Create a new Bag of Words Similarity Matrix for given sets of nodes
     * @param nodesNet1 nodes of net 1
     * @param nodesNet2 nodes of net 2
     */
    public Matrix(Collection<Node> nodesNet1, Collection<Node> nodesNet2){
        LabelSimilarity sim = new LabelSimilarity();
        this.nodesNet1 = nodesNet1.toArray(new Node[nodesNet1.size()]);
        this.nodesNet2 = nodesNet2.toArray(new Node[nodesNet2.size()]);
        this.similarities = new HashMap<>();

        // fill the matrix
        for(Node n1 : nodesNet1){
            for (Node n2 : nodesNet2){
                similarities.put(new Pair<>(n1.getLabel(), n2.getLabel()), sim.BagOfWords(n1.getLabel(), n2.getLabel()));
            }
        }
    }

    /**
     * Returns the similarity score of the two nodes indexes
     * @param i index of
     * @param j
     * @return
     */
    public double betweenIndex(int i, int j)  {
        if ( i <0 || i >= nodesNet1.length ||  j <0 || j >= nodesNet2.length){
            throw new IndexOutOfBoundsException("Index is out of bound to access entity in Label Similarity");
        }
        return similarities.get(new Pair<>(nodesNet1[i].getLabel(), nodesNet2[j].getLabel()));
    }

    public double between(Node nodeNet1, Node nodeNet2){
        return similarities.get(new Pair<>(nodeNet1.getLabel(),nodeNet2.getLabel()));
    }

}
