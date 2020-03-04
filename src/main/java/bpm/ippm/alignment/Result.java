package bpm.ippm.alignment;

public class Result {
    private double similarity;
    private Alignment alignment;
    private double gap;

    public Result(double objective, Alignment alignment, double gap){
        this.similarity = objective;
        this.alignment = alignment;
        this.gap = gap;
    }

    public Alignment getAlignment() {
        return alignment;
    }

    public double getSimilarity() {
        return similarity;
    }

    public double getGAP() { return gap; }

    @Override
    public String toString() {
        String s = "#######RESULTS###### \n" +
                "Similarity: " +similarity + "\n\n";
        s+= alignment.toString();
        return s;
    }

}
