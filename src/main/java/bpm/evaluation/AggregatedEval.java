package bpm.evaluation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Aggregated Eval calculates and stores the aggregated evaluation of one batch execution
 */
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

    // Avg Time
    private long overallTimeAvg = 0;
    private long lpTimeAvg = 0;
    private long bpTimeAvg = 0;
    private long labelSimTimeAvg = 0;


    // store the evals
    private List<Eval> evals = new LinkedList<>();

    /**
     * Constructor: Calculates the aggregated evaluation of the given list of single matching evaluations
     * @param evals list of evaluation which should be aggregated
     */
    public AggregatedEval(List<Eval> evals){
        this.evals = evals;
        for(Eval e :evals){
            // macro
            precisionMacro += e.getPrecision();
            recallMacro += e.getRecall();
            fscoreMacro += e.getFscore();

            // confusion values
            tp += e.getTP();
            fn += e.getFN();
            fp += e.getFP();

            // avg time
            overallTimeAvg += (e.getBenchmark().getOverallTime() > 0) ? e.getBenchmark().getOverallTime()/evals.size():0;
            lpTimeAvg += (e.getBenchmark().getLpTime() > 0) ? e.getBenchmark().getLpTime()/evals.size():0;
            bpTimeAvg += (e.getBenchmark().getBPTime() > 0) ? e.getBenchmark().getBPTime()/evals.size():0;
            labelSimTimeAvg += (e.getBenchmark().getLabelSimialrityTime() > 0) ? e.getBenchmark().getLabelSimialrityTime()/evals.size():0;
        }

        precisionMacro = precisionMacro / evals.size();
        recallMacro = recallMacro/ evals.size();
        fscoreMacro = precisionMacro*recallMacro *2 /(precisionMacro+recallMacro); // fscoreMacro/ evals.size();

        precisionMicro = Metrics.precision(this.tp,this.fp, this.fn);
        recallMicro = Metrics.recall(this.tp,this.fn, this.fp);
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
     * get BP Time AVG (Wallclock)
     * @return
     */
    public long getBpTimeAvg() {
        return bpTimeAvg;
    }

    /**
     * get Label Sim Time AVG (Wallclock)
     * @return
     */
    public long getLabelSimTimeAvg() {
        return labelSimTimeAvg;
    }

    /**
     * get overall time AVG (wallclock)
     * @return
     */
    public long getOverallTimeAvg() {
        return overallTimeAvg;
    }

    /**
     * get Lp time AVG (wallclock)
     * @return
     */
    public long getLpTimeAvg() {
        return lpTimeAvg;
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
                "FSCORE (MICRO): " + fscoreMicro + "\n" +
                "OVERALL TIME (AVG): "  + this.overallTimeAvg + "\n" +
                "LP TIME (AVG): " + this.lpTimeAvg + "\n" +
                "LABEL SIMILARITY TIME (AVG): " + this.labelSimTimeAvg + "\n" +
                "RELATIONAL PROFILE TIME (AVG): " + this.bpTimeAvg + "\n";
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
        csvWriter.append("Name,").append("TP,").append("FP,").append("FN,").append("PRECISION,").append("RECALL,").append("FSCORE,").append("OVERALL TIME,").append("LP TIME,").append("LABEL-SIM TIME,").append("BP TIME,").append("SIMILARITY,").append("MIP GAP\n");

        // aggregated statistics
        csvWriter.append("Aggregated (MACRO),").append(this.tp+",").append(this.fp+",").append(this.fn+",").append(this.precisionMacro+",").append(this.recallMacro+",").append(this.fscoreMacro+",").append(this.overallTimeAvg+",").append(this.lpTimeAvg+",").append(this.labelSimTimeAvg+",").append(this.bpTimeAvg+",").append("" + 0 +",").append("" + 0 + "\n");
        csvWriter.append("Aggregated (MICRO),").append(this.tp+",").append(this.fp+",").append(this.fn+",").append(this.precisionMicro+",").append(this.recallMicro+",").append(this.fscoreMicro+",").append(this.overallTimeAvg+",").append(this.lpTimeAvg+",").append(this.labelSimTimeAvg+",").append(this.bpTimeAvg+",").append("" + 0 +",").append("" + 0 + "\n");

        // detailed statistics
        for(Eval e : evals){
                csvWriter.append(e.getName().replace(',',';').replace("\n"," ")+",")
                         .append(e.getTP()+",").append(e.getFP()+",").append(e.getFN()+",").append(e.getPrecision()+",")
                         .append(e.getRecall()+",").append(e.getFscore()+",").append(e.getBenchmark().getOverallTime()+",")
                         .append(e.getBenchmark().getLpTime()+",").append(e.getBenchmark().getLabelSimialrityTime()+",")
                         .append(e.getBenchmark().getBPTime()+",").append(e.getSimilarity()+",").append(e.getGAP()+"\n");
            }

        //properly close everything
        csvWriter.flush();
        csvWriter.close();
    }
}
