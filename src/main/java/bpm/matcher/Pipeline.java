package bpm.matcher;

import java.io.File;

/**
 * Matching Pipeline. Note, use builder to construct.
 */
public class Pipeline {
    private boolean complexMatches;
    private double  similarityWeight;
    private double  postprocessThreshold;

    /**
     * Empty Constructor for the Builder
     */
    private Pipeline(){
    }

    /**
     * Run the matching pipeline for the given two petri nets.
     * @param net1 petri net file path 1
     * @param net2 petri net file path 2
     */
    public void run(File net1, File net2){

    }


    /**
     * Builder for class Pipeline
     */
    public static class Builder{
        boolean complexMatches = false;
        double  similarityWeight = 0.3;
        double  postprocessThreshold = 0.0;

        /**
         * Create a Builder to define a Pipline Object.
         */
        public Builder(){
        }

        /**
         * Enable complex matches.
         * @return Builder
         */
        public Pipeline.Builder withComplexMatches(){
            this.complexMatches = true;
            return this;
        }

        /**
         * Set the weight between profile conformance influence and label similarity influence.
         * 0 means label only, 1 means profile only.
         * @param s between 0 and 1
         * @return Builder
         */
        public Pipeline.Builder atSimilarityWeight(double s){
            if( s < 0 || s > 1){
                throw new NumberFormatException("Value SimilarityWeight is out of range 0 to 1");
            }
            this.similarityWeight = s;
            return this;
        }

        /**
         * Set the post processing min label threshhold.
         * Every match which has lover label similarity than the threshold p is post-pruned.
         * @param p value between 0 and 1
         * @return Builder
         */
        public Pipeline.Builder atPostprocessThreshold(double p){
            if( p < 0 || p > 1){
                throw new NumberFormatException("Value PostprocessThreshold is out of range 0 to 1");
            }
            this.postprocessThreshold = p;
            return this;
        }

        /**
         *Build a Pipeline with the previously assigned arguments.
         * @return Executable Pipeline
         */
        public Pipeline Build(){
            Pipeline pip = new Pipeline();
            pip.similarityWeight = this.similarityWeight;
            pip.complexMatches = this.complexMatches;
            pip.postprocessThreshold = this.postprocessThreshold;
            return pip;
        }

    }


}
