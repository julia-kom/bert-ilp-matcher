package bpm.ippm.similarity;

public interface LabelSimilarity {

    double sim(String label1, String label2);

    public enum Similarities{
        BOW,
        NORMAL_DISTRIBUTION
    }
}
