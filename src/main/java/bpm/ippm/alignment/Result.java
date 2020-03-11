package bpm.ippm.alignment;

/**
 * Result object of a matching contains:
 * 1) Process Similarity Value
 * 2) A process alignment
 * 3) The ILP gap of the returned solution (when solved via an ILP)
 */
public class Result {
    private double similarity;
    private Alignment alignment;
    private double gap;

    /**
     * Create a new result object
     * @param objective process similarity score computed by the matcher
     * @param alignment alignment computed by the matcher
     * @param gap gap at the end of the time/node limit
     */
    public Result(double objective, Alignment alignment, double gap){
        this.similarity = objective;
        this.alignment = alignment;
        this.gap = gap;
    }

    /**
     * Get the alignment
     * @return Alignment
     */
    public Alignment getAlignment() {
        return alignment;
    }

    /**
     * Get the process simialrity score
     * @return Process similarity score
     */
    public double getSimilarity() {
        return similarity;
    }

    /**
     * Get the GAP (only relevant for ILP based matchers)
     * @return Gap value
     */
    public double getGAP() { return gap; }

    /**
     * Convert to string representation
     * @return string representation of the result
     */
    @Override
    public String toString() {
        String s = "#######RESULTS###### \n" +
                "Similarity: " +similarity + "\n\n";
        s+= alignment.toString();
        return s;
    }

}
