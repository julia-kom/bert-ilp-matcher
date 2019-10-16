package bpm.evaluation;

public class Metrics {

    /**
     * Compute precision give tp and fp
     * @param tp
     * @param fp
     * @return
     */
    public static double precision(int tp, int fp){
        return 1.0 * tp /(tp + fp);
    }

    /**
     * Compute Recall given tp and fn
     * @param tp
     * @param fn
     * @return
     */
    public static double recall(int tp, int fn){
        return 1.0 * tp /(tp + fn);
    }

    /**
     * F1Score given tp, fp and fn
     * @param tp
     * @param fp
     * @param fn
     * @return
     */
    public static double fscore(int tp, int fp, int fn){
        return (2.0* tp) /(2.0*tp+fn +fp);
    }


}
