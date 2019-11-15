package bpm.evaluation;

public class Metrics {

    /**
     * Compute precision give tp and fp
     * @param tp
     * @param fp
     * @return
     */
    public static double precision(int tp, int fp, int fn){
        // rare case that the matcher made an empty matching hypothesis
        // https://github.com/dice-group/gerbil/wiki/Precision,-Recall-and-F1-measure
        if(tp == 0 && fp == 0){
            if(fn == 0){
                return 1.0; // goldstandard and matching hypothesis both state that there is no matching
            }else{
                return 0.0; // there actually was a match in the goldstandard but the matching hypothesis was empty
            }
        }else {
            return 1.0 * tp / (tp + fp); // compute precision as usual
        }
    }

    /**
     * Compute Recall given tp and fn
     * @param tp
     * @param fn
     * @return
     */
    public static double recall(int tp, int fn, int fp){
        // rare case that the goldstandard has no matching
        // https://github.com/dice-group/gerbil/wiki/Precision,-Recall-and-F1-measure
        if(tp == 0 && fn == 0){
            if(fp == 0) {
                return 1.0; // goldstandard and matching hypothesis both state that there is no matching
            }else{
                return 0.0; // there is no matching in the goldstandard but the matching hypothesis got one inside
            }
        }else {
            return 1.0 * tp / (tp + fn); //compute recall as usual
        }
    }

    /**
     * F1Score given tp, fp and fn
     * @param tp
     * @param fp
     * @param fn
     * @return
     */
    public static double fscore(int tp, int fp, int fn){
        // https://github.com/dice-group/gerbil/wiki/Precision,-Recall-and-F1-measure
        if(tp == 0 && fp == 0 && fp == 0){
            return 1.0;
        }
        return (2.0* tp) /(2.0*tp+ fn +fp);
    }


}
