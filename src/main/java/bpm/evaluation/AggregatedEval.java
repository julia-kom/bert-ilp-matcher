package bpm.evaluation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
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

    // store the evals
    private List<Eval> evals = new LinkedList<>();

    /**
     * Constructor
     * @param evals
     */
    public AggregatedEval(List<Eval> evals){
        this.evals = evals;
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
                "FN: " +fn + "\n" +
                "PRECISION (MACRO): " + precisionMacro + "\n" +
                "PRECISION (MICRO): " + precisionMicro + "\n" +
                "RECALL (MACRO): " + recallMacro + "\n" +
                "RECALL (MICRO): " + recallMicro + "\n" +
                "FSCORE (MACRO): " + fscoreMacro + "\n" +
                "FSCORE (MICRO): " + fscoreMicro + "\n";
    }

    /**
     * Compute the csv summary file of the aggregated eval test.
     * First row: Aggregated Macro Statistics (note that confusion values for macro and micro are always equal)
     * Second row: Aggregated Micro Statistics (note that confusion values for macro and micro are always equal)
     * @param file File where to write the stats to.
     * @throws IOException
     */
    public void toCSV(File file) throws IOException {
        FileWriter csvWriter = new FileWriter(file.getAbsolutePath());
        // column description
        csvWriter.append("Name,").append("TP,").append("FP,").append("FN,").append("PRECISION,").append("RECALL,").append("FSCORE\n");

        // aggregated statistics
        csvWriter.append("Aggregated (MACRO),").append(this.tp+",").append(this.fp+",").append(this.fn+",").append(this.precisionMacro+",").append(this.recallMacro+",").append(this.fscoreMacro+"\n");
        csvWriter.append("Aggregated (MICRO),").append(this.tp+",").append(this.fp+",").append(this.fn+",").append(this.precisionMicro+",").append(this.recallMicro+",").append(this.fscoreMicro+"\n");

        // detailed statistics
        for(Eval e : evals){
                csvWriter.append(e.getName().replace(',',';').replace("\n"," ")+",").append(e.getTP()+",").append(e.getFP()+",").append(e.getFN()+",").append(e.getPrecision()+",").append(e.getRecall()+",").append(e.getFscore()+"\n");
            }

        //properly close everything
        csvWriter.flush();
        csvWriter.close();
    }
}
