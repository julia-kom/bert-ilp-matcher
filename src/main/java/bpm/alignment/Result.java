package bpm.alignment;

import org.jbpt.petri.Node;

import java.util.Set;

public class Result {
    private double similarity;
    private Alignment alignment;

    public Result(double objective, Alignment alignment){
        this.similarity = objective;
        this.alignment = alignment;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public double getSimilarity() {
        return similarity;
    }

    @Override
    public String toString() {
        String s = "#######RESULTS###### \n" +
                "Similarity: " +similarity + "\n\n";
        s+= alignment.toString();
        return s;
    }
}
