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
        lpTime = System.nanoTime() - lpStart;
    }

    public void startOverallTime(){
        overallStart = System.nanoTime();
    }

    public void stopOverallTime(){
        overallTime = System.nanoTime() - overallStart;
    }

    public void startLabelSimilarityTime(){
        labelSimStart = System.nanoTime();
    }

    public void stopLabelSimilarityTime(){
        labelSimTime = System.nanoTime() - labelSimStart;
    }

    public void startBPTime(){
        bpStart = System.nanoTime();
    }

    public void stopBPTime(){
        bpTime = System.nanoTime() - bpStart;
    }

    public long getLpTime(){
        if (lpStart < 0) {
            throw new Error("Timer was not started in first place");
        }
        return lpTime;
    }

    public long getOverallTime(){
        if (overallStart < 0) {
            throw new Error("Timer was not started in first place");
        }
        return overallTime;
    }

    public long getLabelSimialrityTime(){
        if (labelSimStart < 0) {
            throw new Error("Timer was not started in first place");
        }
        return labelSimTime;
    }

    public long getBPTime(){
        if (bpStart < 0) {
            throw new Error("Timer was not started in first place");
        }
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
