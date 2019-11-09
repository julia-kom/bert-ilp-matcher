package bpm.evaluation;

public class ExecutionTimer {
    private long lpStart = -1;
    private long lpTime = -1;
    private long overallStart = -1;
    private long overallTime = -1;
    private long labelSimStart = -1;
    private long labelSimTime = -1;
    private long bpStart = -1;
    private long bpTime = -1;

    public void startLpTime(){
        lpStart = System.nanoTime();
    }

    public void stopLpTime(){
        if (lpStart < 0) {
            throw new Error("Timer was not started in first place");
        }
        lpTime = System.nanoTime() - lpStart;
    }

    public void startOverallTime(){
        overallStart = System.nanoTime();
    }

    public void stopOverallTime(){
        if (overallStart < 0) {
            throw new Error("Timer was not started in first place");
        }
        overallTime = System.nanoTime() - overallStart;
    }

    public void startLabelSimilarityTime(){
        labelSimStart = System.nanoTime();
    }

    public void stopLabelSimilarityTime(){
        if (labelSimStart < 0) {
            throw new Error("Timer was not started in first place");
        }
        labelSimTime = System.nanoTime() - labelSimStart;
    }

    public void startBPTime(){
        bpStart = System.nanoTime();
    }

    public void stopBPTime(){
        if (bpStart < 0) {
            throw new Error("Timer was not started in first place");
        }
        bpTime = System.nanoTime() - bpStart;
    }

    public long getLpTime(){
        return lpTime;
    }

    public long getOverallTime(){
        return overallTime;
    }

    public long getLabelSimialrityTime(){
        return labelSimTime;
    }

    public long getBPTime(){
        return bpTime;
    }

    @Override
    public String toString() {
        return  "OVERALL TIME: "  + this.overallTime + "\n" +
                "LP TIME: " + this.lpTime + "\n" +
                "LABEL SIMILARITY TIME: " + this.labelSimTime + "\n" +
                "RELATIONAL PROFILE TIME: " + this.bpTime + "\n";
    }
}
