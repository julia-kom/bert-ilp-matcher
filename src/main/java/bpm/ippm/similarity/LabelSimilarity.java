package bpm.ippm.similarity;

/**
 * Interface for a Label similarity measure
 */
public interface LabelSimilarity {

    double sim(String label1, String label2);

    /**
     * Implemented Label similarities
     */
    public enum Similarities{
        BERT,
        BERTFT,
        BOW,
        NORMAL_DISTRIBUTION // used for evaluation only
    }
}
