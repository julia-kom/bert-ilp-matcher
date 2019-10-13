package bpm.matcher;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jbpt.petri.Node;


public class LabelSimilarity {
    private Node[] entitiesNet1;
    private Node[] entitiesNet2;
    private Map<Node, Map<Node, Double>> similarities;
    @Deprecated
    LabelSimilarity(Collection<Node> entitiesNet1, Collection<Node> entitiesNet2){
        this.entitiesNet1 = (Node[]) entitiesNet1.toArray();
        this.entitiesNet2 = (Node[]) entitiesNet2.toArray();
        this.similarities = null;
        //todo similarity call and store it
    }
    @Deprecated
    double betweenIndex(int i, int j)  {
        if ( i <0 || i >= entitiesNet1.length ||  j <0 || j >= entitiesNet2.length){
            throw new IndexOutOfBoundsException("Index is out of bound to access entity in Label Similarity");
        }
        return similarities.get(entitiesNet1[i]).get(entitiesNet2[j]);
    }

    @Deprecated
    double between(Node entityNet1, Node entityNet2){
        return similarities.get(entityNet1).get(entityNet2);
    }


    public static double BoWSim(String s1, String s2){
        //todo implement
        return 0.0;
    }



}
