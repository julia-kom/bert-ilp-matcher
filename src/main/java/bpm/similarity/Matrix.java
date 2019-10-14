package bpm.similarity;

import javafx.util.Pair;
import org.jbpt.petri.Node;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Matrix{
    private Word.Similarities wordSimilarityFunction;
    private Node[] nodesNet1;
    private Node[] nodesNet2;
    private Map<Pair<String,String>, Double> similarities;

    /**
     * Use Builder
     */
    private Matrix(){

    }

    /**
     * Create a new Bag of Words Similarity Matrix for given sets of nodes
     */
    private void construct(){
        this.similarities = new HashMap<>();
        LabelSimilarity labelSimilarity = new LabelSimilarity(wordSimilarityFunction);
        // fill the matrix
        for(Node n1 : nodesNet1){
            for (Node n2 : nodesNet2){
                similarities.put(new Pair<>(n1.getLabel(), n2.getLabel()), labelSimilarity.BagOfWords(n1.getLabel(), n2.getLabel()));
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

    public static class Builder{
        Word.Similarities sim = Word.Similarities.LEVENSHTEIN_LIN_MAX;

        public Builder withWordSimilarity(Word.Similarities sim){
            this.sim = sim;
            return this;
        }

        public Matrix build(Collection<Node> nodesNet1, Collection<Node> nodesNet2){
            Matrix res = new Matrix();
            res.nodesNet1 = nodesNet1.toArray(new Node[nodesNet1.size()]);
            res.nodesNet2 = nodesNet2.toArray(new Node[nodesNet2.size()]);
            res.wordSimilarityFunction = this.sim;
            res.construct();
            return res;
        }
    }



}
