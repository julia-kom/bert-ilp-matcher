package bpm.evaluation;

/**
 * Class to track time consumption of different steps of the pipeline.
 * This is used for time evaluation in the analysis.
 */
public class ExecutionTimer {
    private long lpStart = -1;
    private long lpTime = -1;
    private long overallStart = -1;
    private long overallTime = -1;
    private long labelSimStart = -1;
    private long labelSimTime = -1;
    private long bpStart = -1;
    private long bpTime = -1;

    /**
     * start the Linear Program Timer
     */
    public void startLpTime(){
        lpStart = System.nanoTime();
    }

    /**
     * Stop LP timer
     */
    public void stopLpTime(){
        if (lpStart < 0) {
            throw new Error("Timer was not started in first place");
        }
        lpTime = System.nanoTime() - lpStart;
    }

    /**
     * start Overall matching timer
     */
    public void startOverallTime(){
        overallStart = System.nanoTime();
    }

    /**
     * Stop overall matching timer
     */
    public void stopOverallTime(){
        if (overallStart < 0) {
            throw new Error("Timer was not started in first place");
        }
        overallTime = System.nanoTime() - overallStart;
    }

    /**
     * Start Label Similarity Timer
     */
    public void startLabelSimilarityTime(){
        labelSimStart = System.nanoTime();
    }

    /**
     * Stop Label Similarity Timer
     */
    public void stopLabelSimilarityTime(){
        if (labelSimStart < 0) {
            throw new Error("Timer was not started in first place");
        }
        labelSimTime = System.nanoTime() - labelSimStart;
    }

    /**
     * Start Profile Computation Timer
     */
    public void startBPTime(){
        bpStart = System.nanoTime();
    }

    /**
     * Stop Profile computation Timer
     */
    public void stopBPTime(){
        if (bpStart < 0) {
            throw new Error("Timer was not started in first place");
        }
        bpTime = System.nanoTime() - bpStart;
    }

    /**
     * Get LP time
     * @return LP time or -1 if not calculated.
     */
    public long getLpTime(){
        return lpTime;
    }

    /**
     * Get Overall Time.
     * @return overall time or -1 if not calculated.
     */
    public long getOverallTime(){
        return overallTime;
    }

    /**
     * Get Label Similarity Time.
     * @return overall time or -1 if not calculated.
     */
    public long getLabelSimialrityTime(){
        return labelSimTime;
    }

    /**
     * Get Profile computation Time.
     * @return overall time or -1 if not calculated.
     */
    public long getBPTime(){
        return bpTime;
    }

    /**
     * Convert to string
     * @return
     */
    @Override
    public String toString() {
        return  "OVERALL TIME: "  + this.overallTime + "\n" +
                "LP TIME: " + this.lpTime + "\n" +
                "LABEL SIMILARITY TIME: " + this.labelSimTime + "\n" +
                "RELATIONAL PROFILE TIME: " + this.bpTime + "\n";
    }
}
