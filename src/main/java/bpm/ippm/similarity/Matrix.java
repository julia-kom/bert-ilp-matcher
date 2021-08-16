package bpm.ippm.similarity;


import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jbpt.petri.Transition;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This matrix contains for each pair of transitions of the two to compare petri nets and entry with the BoW Label
 * Similarity.
 */
public class Matrix{
    private Transition[] nodesNet1;
    private Transition[] nodesNet2;
    private Map<Pair<String,String>, Double> similarities;
    private LabelSimilarity labelSimilarity;

    /**
     * Use Builder instead
     */
    private Matrix(){

    }

    /**
     * Create a new Bag of Words Similarity Matrix for given sets of nodes
     */
    private void construct(){
        this.similarities = new HashMap<>();

        // fill the matrix
        for(Transition n1 : nodesNet1){
            for (Transition n2 : nodesNet2){
                //System.out.println(n1.getLabel()+" --"+ n2.getLabel() +":"+ labelSimilarity.sim(n1.getLabel(), n2.getLabel()));
                similarities.put(new ImmutablePair<>(n1.getLabel(), n2.getLabel()), labelSimilarity.sim(n1.getLabel(), n2.getLabel()));
            }
        }
    }

    /**
     * Returns the similarity score of the two nodes indexes
     * @param i matrix index of node in net 1
     * @param j matrix index of node in net 2
     * @return similarity between the two transitions
     */
    public double betweenIndex(int i, int j)  {
        if ( i <0 || i >= nodesNet1.length ||  j <0 || j >= nodesNet2.length){
            throw new IndexOutOfBoundsException("Index is out of bound to access entity in Label Similarity");
        }
        return similarities.get(new ImmutablePair<>(nodesNet1[i].getLabel(), nodesNet2[j].getLabel()));
    }

    /**
     * Get the label similarity between node nodeNet1 and nodeNet2
     * @param nodeNet1 transition in net 1
     * @param nodeNet2 transition in net 2
     * @return similarity between two transitions
     */
    public double between(Transition nodeNet1, Transition nodeNet2){
        return similarities.get(new ImmutablePair<>(nodeNet1.getLabel(),nodeNet2.getLabel()));
    }

    /**
     * Builder class
     */
    public static class Builder{
        Word.Similarities sim = Word.Similarities.LEVENSHTEIN_LIN_MAX;
        LabelSimilarity.Similarities labelSim = LabelSimilarity.Similarities.BOW;

        /**
         * Select Word Simialrity which is used within the BoW Label Similarity
         * @param sim word similarity to be used
         * @return Builder
         */
        public Builder withWordSimilarity(Word.Similarities sim){
            this.sim = sim;
            return this;
        }

        /**
         * Set label similarity function
         * @param labelSimilarity label similarity
         * @return Builder
         */
        public Builder withLabelSimilarity(LabelSimilarity.Similarities labelSimilarity){
            this.labelSim = labelSimilarity;
            return this;
        }

        /**
         * Build the matrix for the two sets of transitions
         * @param nodesNet1 set of transitions of net 1
         * @param nodesNet2 set of transitions of net 2
         * @return Builder
         */
        public Matrix build(Set<Transition> nodesNet1, Set<Transition> nodesNet2){
            Matrix res = new Matrix();
            res.nodesNet1 = nodesNet1.toArray(new Transition[nodesNet1.size()]);
            res.nodesNet2 = nodesNet2.toArray(new Transition[nodesNet2.size()]);

            switch(labelSim){
                case BERT:
                    res.labelSimilarity = new BertSimilarity();
                    break;
                case BOW:
                    res.labelSimilarity = new BagOfWordsSimilarity(this.sim);
                    break;
                case NORMAL_DISTRIBUTION:
                    res.labelSimilarity = new NormalDistributionLabelSimilarity();
                    break;
                    default:
                        throw new Error("This label similarity is not implemented in the Matrix yet!");
            }

            res.construct();
            return res;
        }
    }



}
