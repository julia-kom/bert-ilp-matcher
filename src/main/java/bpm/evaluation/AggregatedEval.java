package bpm.evaluation;

import java.util.List;

public class AggregatedEval {
    private int tp = 0;
    private int fp = 0;
    private int fn = 0;

    // Micro scores (compute scores on summed up confusion values
    private double precisionMicro = 0;
    private double recallMicro = 0;
    private double fscoreMicro = 0;

    // Macro scores (average of the score)
    private double precisionMacro = 0;
    private double recallMacro = 0;
    private double fscoreMacro = 0;


    /**
     * Constructor
     * @param evals
     */
    public AggregatedEval(List<Eval> evals){

        for(Eval e :evals){
            // macro
            precisionMacro += e.getPrecision() / evals.size();
            recallMacro += e.getRecall() / evals.size();
            fscoreMacro += e.getFscore() / evals.size();

            // confusion values
            tp += e.getTP();
            fn += e.getFN();
            fp += e.getFP();
        }

        precisionMicro = Metrics.precision(this.tp,this.fp);
        recallMicro = Metrics.recall(this.tp,this.fp);
        fscoreMicro = Metrics.fscore(this.tp, this.fp, this.fn);
    }

    /**
     * Get TN
     * @return
     */
    public int getFN() {
        return fn;
    }

    /**
     * Get FP
     * @return
     */
    public int getFP() {
        return fp;
    }

    /**
     * Get TP
     * @return
     */
    public int getTP() {
        return tp;
    }

    /**
     * Get Fscore Macro
     * @return
     */
    public double getFscoreMacro() {
        return fscoreMacro;
    }

    /**
     * Get FScore Micro
     * @return
     */
    public double getFscoreMicro() {
        return fscoreMicro;
    }

    /**
     * Get Precision Macro
     * @return
     */
    public double getPrecisionMacro() {
        return precisionMacro;
    }

    /**
     * Get Precision Micro
     * @return
     */
    public double getPrecisionMicro() {
        return precisionMicro;
    }

    /**
     * Get Recall Macro
     * @return
     */
    public double getRecallMacro() {
        return recallMacro;
    }

    /**
     * Get Recall Micro
     * @return
     */
    public double getRecallMicro() {
        return recallMicro;
    }

    /**
     * Evalaution to String
     * @return
     */
    @Override
    public String toString() {
        return "## Aggregated Statistics ## \n" +
                "TP: " +tp + "\n" +
                "FP: " +fp + "\n" +
                "FP: " +fn + "\n" +
                "PRECISION (MACRO): " + precisionMacro + "\n" +
                "PRECISION (MICRO): " + precisionMicro + "\n" +
                "RECALL (MACRO): " + recallMacro + "\n" +
                "RECALL (MICRO): " + recallMicro + "\n" +
                "FSCORE (MACRO): " + fscoreMacro + "\n" +
                "FSCORE (MICRO): " + fscoreMicro + "\n";
    }


}
